/*
 * Spartan Time Lapse Recorder - Minimalistic android time lapse recording app
 * Copyright (C) 2014  Andreas Rohner
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.andreasrohner.spartantimelapserec.recorder;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import at.andreasrohner.spartantimelapserec.data.RecSettings;

public class VideoRecorder extends Recorder implements OnInfoListener,
		OnErrorListener {
	protected MediaRecorder mMediaRecorder;
	protected int mRate;

	public VideoRecorder(RecSettings settings,
			Context context, Handler handler) {
		super(settings, context, handler);
	}

	protected int getFrameRate() {
		Camera.Parameters params = mCamera.getParameters();

		List<int[]> ranges = params.getSupportedPreviewFpsRange();
		if (ranges == null)
			return -1;

		int fps = mSettings.getFrameRate() * 1000;
		int selFps = ranges.get(ranges.size() - 1)[1];

		for (int i = 0; i < ranges.size(); ++i) {
			int[] range = ranges.get(i);

			if (fps >= range[0] && fps <= range[1]) {
				selFps = fps / 1000;
				break;
			}
		}

		return selFps;
	}

	private void releaseMediaRecorder() {
		if (mMediaRecorder == null)
			return;

		try {
			mMediaRecorder.stop();
			mMediaRecorder.reset();
			mMediaRecorder.release();
		} catch (Exception e) {
			e.printStackTrace();
		}
		mMediaRecorder = null;
	}

	@Override
	public void stop() {
		muteShutter();
		releaseMediaRecorder();

		super.stop();
	}

	protected void setCameraParams() throws IOException {
		Camera.Parameters params = mCamera.getParameters();
		double ratio = (double) mSettings.getFrameWidth()
				/ mSettings.getFrameHeight();
		/*
		 * hack necessary for samsung phones to enable 16:9 recordings otherwise
		 * the camera will record 4:3 image and stretch it to 16:9
		 */
		if (Math.abs(ratio - (16D / 9D)) < 0.01)
			params.set("cam_mode", 1);

		List<String> suppList = params.getSupportedWhiteBalance();
		if (suppList != null) {
			Set<String> suppModes = new HashSet<String>();
			suppModes.addAll(suppList);

			if (suppModes.contains(Camera.Parameters.WHITE_BALANCE_AUTO)) {
				params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
			}
		}

		suppList = params.getSupportedFocusModes();
		if (suppList != null) {
			Set<String> suppModes = new HashSet<String>();
			suppModes.addAll(suppList);

			if (suppModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
				params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
			}
		}
		params.setExposureCompensation(mSettings.getExposureCompensation());
		params.setZoom(mSettings.getZoom());
		mCamera.setParameters(params);
	}

	@Override
	protected void prepareRecord() throws IOException {
		releaseMediaRecorder();

		releaseCamera();

		mCamera = Camera.open(mSettings.getCameraId());

		setCameraParams();

		mRate = getFrameRate();

		SurfaceTexture surfaceTexture = new SurfaceTexture(10);
		mCamera.setPreviewTexture(surfaceTexture);
		//mCamera.setPreviewDisplay(mSurfaceHolder);

		muteShutter();

		mCamera.unlock();

		mMediaRecorder = new MediaRecorder();
		mMediaRecorder.setCamera(mCamera);
	}

	@Override
	protected void doRecord() throws IllegalStateException, IOException {
		mMediaRecorder.setOrientationHint(getCameraRotation(mSettings.getCameraId()));
		// no need for more sensor data
		disableOrientationSensor();

		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

		CamcorderProfile p = CamcorderProfile.get(mSettings.getCameraId(),
				mSettings.getRecProfile());
		p.videoFrameWidth = mSettings.getFrameWidth();
		p.videoFrameHeight = mSettings.getFrameHeight();
		mMediaRecorder.setProfile(p);

		if (mRate != -1)
			mMediaRecorder.setVideoFrameRate(mRate);
		mMediaRecorder.setOutputFile(getOutputFile("mp4").getAbsolutePath());
		mMediaRecorder.setVideoSize(mSettings.getFrameWidth(), mSettings.getFrameHeight());

		if (mSettings.getStopRecAfter() > 0) {
			mMediaRecorder.setMaxDuration(mSettings.getStopRecAfter());
			mMediaRecorder.setOnInfoListener(this);
		}

		Log.i(getClass().getSimpleName(), "Starting video recording");
		mMediaRecorder.setOnErrorListener(this);
		if (mSettings.getVideoEncodingBitRate()>0) mMediaRecorder.setVideoEncodingBitRate(mSettings.getVideoEncodingBitRate());
		mMediaRecorder.prepare();

		mMediaRecorder.start();
	}

	@Override
	protected void muteShutter() {
		// mCamera.enableShutterSound(false); does not work for MediaRecorder
		// (on Samsung Galaxy S3)
		if (mSettings != null && mSettings.isMuteShutter() && mMute != null)
			mMute.muteShutter();
	}

	@Override
	protected void unmuteShutter() {
		// mCamera.enableShutterSound(true); does not work for MediaRecorder (on
		// Samsung Galaxy S3)
		if (mSettings != null && mSettings.isMuteShutter() && mMute != null)
			mMute.unmuteShutter();
	}

	@Override
	public void onInfo(MediaRecorder mr, int what, int extra) {
		switch (what) {
		case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
			success();  //tell service to stop
			break;
		}
	}

	@Override
	public void onError(MediaRecorder mr, int what, int extra) {
		switch (what) {
		case MediaRecorder.MEDIA_ERROR_SERVER_DIED:
			handleError(getClass().getSimpleName(), "Mediaserver died");
			break;
		default:
			handleError(getClass().getSimpleName(),
					"Unkown error occured while recording");
			break;
		}
	}
}
