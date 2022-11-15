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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.ErrorCallback;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.view.SurfaceHolder;
import at.andreasrohner.spartantimelapserec.data.RecSettings;

public class ImageRecorder extends Recorder implements Runnable,
		Camera.PictureCallback, ErrorCallback, AutoFocusCallback {
	private static final int CONTINUOUS_CAPTURE_THRESHOLD = 3000;
	private static final int RELEASE_CAMERA_THRESHOLD = 2000;
	protected long mEndTime;
	protected long mStartPreviewTime;
	protected boolean mUseAutoFocus;

	public ImageRecorder(RecSettings settings, SurfaceHolder surfaceHolder,
			Context context, Handler handler) {
		super(settings, surfaceHolder, context, handler);

		if (settings.getStopRecAfter() > 0)
			mEndTime = System.currentTimeMillis() + settings.getInitDelay()
					+ settings.getStopRecAfter();

		if (mCanDisableShutterSound)
			mMute = null;
	}

	@Override
	public void stop() {
		if (mHandler != null)
			mHandler.removeCallbacks(this);

		muteShutter();

		super.stop();
	}

	protected void scheduleNextPicture() {
		long diffTime = SystemClock.elapsedRealtime() - mStartPreviewTime;
		long delay = mSettings.getCaptureRate() - diffTime;

		if (delay >= RELEASE_CAMERA_THRESHOLD
				&& mSettings.getCaptureRate() >= CONTINUOUS_CAPTURE_THRESHOLD)
			releaseCamera();

		if (delay <= 0)
			mHandler.post(this);
		else
			mHandler.postDelayed(this, delay);
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		try {
			File file = getOutputFile("jpg");
			FileOutputStream out = new FileOutputStream(file);
			out.write(data);
			out.close();

			scheduleNextPicture();
		} catch (Exception e) {
			handleError(getClass().getSimpleName(), e.getMessage());
		}
	}

	@Override
	public void onAutoFocus(boolean success, Camera camera) {
		try {
			muteShutter();

			camera.takePicture(null, null, this);
		} catch (Exception e) {
			e.printStackTrace();
			handleError(getClass().getSimpleName(), e.getMessage());
		}
	}

	@Override
	public void run() {
		try {
			if (mEndTime > 0 && mEndTime < System.currentTimeMillis()) {
				success();
				stop();
				return;
			}

			mStartPreviewTime = SystemClock.elapsedRealtime();

			if (mCamera == null)
				prepareRecord();

			setCameraOrientation(mSettings.getCameraId());

			mCamera.startPreview();

			if (mUseAutoFocus)
				mCamera.autoFocus(this);
			else
				onAutoFocus(true, mCamera);
		} catch (Exception e) {
			e.printStackTrace();
			handleError(getClass().getSimpleName(), e.getMessage());
		}
	}

	protected void setWhiteBalance(Camera.Parameters params,
			Set<String> suppModes) {
		if (suppModes.contains(Camera.Parameters.WHITE_BALANCE_AUTO)) {
			params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
		}
	}

	@SuppressLint("InlinedApi")
	protected void setFocusMode(Camera.Parameters params, Set<String> suppModes) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
				&& mSettings.getCaptureRate() < CONTINUOUS_CAPTURE_THRESHOLD
				&& suppModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
			params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		} else if (suppModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)
				&& mSettings.getCaptureRate() < CONTINUOUS_CAPTURE_THRESHOLD) {
			params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
		} else if (suppModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
			params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			mUseAutoFocus = true;
		}
	}

	protected void setCameraParams() throws IOException {
		Camera.Parameters params = mCamera.getParameters();

		/*
		 * params.set("cam_mode", 1); hack is not necessary for pictures
		 */

		List<String> suppList = params.getSupportedWhiteBalance();
		if (suppList != null) {
			Set<String> suppModes = new HashSet<String>();
			suppModes.addAll(suppList);

			setWhiteBalance(params, suppModes);
		}

		suppList = params.getSupportedFocusModes();
		if (suppList != null) {
			Set<String> suppModes = new HashSet<String>();
			suppModes.addAll(suppList);

			setFocusMode(params, suppModes);
		}

		params.setPictureFormat(ImageFormat.JPEG);

		params.setPictureSize(mSettings.getFrameWidth(),
				mSettings.getFrameHeight());

		params.setJpegQuality(mSettings.getJpegQuality());

		mCamera.setParameters(params);

		mCamera.setErrorCallback(this);
	}

	protected void prepareRecord() throws IOException {
		releaseCamera();

		mCamera = Camera.open(mSettings.getCameraId());

		setCameraParams();

		mCamera.setPreviewDisplay(mSurfaceHolder);
	}

	protected void doRecord() {
		run();
	}

	@Override
	public void onError(int error, Camera camera) {
		switch (error) {
		case Camera.CAMERA_ERROR_SERVER_DIED:
			handleError(getClass().getSimpleName(), "Cameraserver died");
			break;
		default:
			handleError(getClass().getSimpleName(),
					"Unkown error occured while recording");
			break;
		}
	}
}
