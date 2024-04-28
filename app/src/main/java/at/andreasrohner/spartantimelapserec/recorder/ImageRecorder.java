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
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.ErrorCallback;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import at.andreasrohner.spartantimelapserec.ImageRecorderState;
import at.andreasrohner.spartantimelapserec.data.RecSettingsLegacy;

public class ImageRecorder extends Recorder implements Runnable, Camera.PictureCallback, ErrorCallback, AutoFocusCallback {

	private static final int CONTINUOUS_CAPTURE_THRESHOLD = 3000;

	private static final int RELEASE_CAMERA_THRESHOLD = 2000;

	protected long mEndTime;

	protected long mStartPreviewTime;

	protected boolean mUseAutoFocus;

	protected Camera.PictureCallback pictureCallback;

	protected AutoFocusCallback autoFocusCallback;

	protected boolean mWaitCamReady;

	public ImageRecorder(RecSettingsLegacy settings, Context context, Handler handler, File outputDir) {
		super(settings, context, handler, outputDir);

		if (settings.getStopRecAfter() > 0) {
			mEndTime = System.currentTimeMillis() + settings.getInitDelay() + settings.getStopRecAfter();
		}

		if (mCanDisableShutterSound) {
			mMute = null;
		}

		pictureCallback = this;
		autoFocusCallback = this;
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
		if (delay >= RELEASE_CAMERA_THRESHOLD && mSettings.getCaptureRate() >= CONTINUOUS_CAPTURE_THRESHOLD) {
			releaseCamera();
		}

		if (delay <= 0)
			mHandler.post(this);
		else
			mHandler.postDelayed(this, delay);
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		try {
			File file = getOutputFile("jpg");
			ImageRecorderState.setCurrentImage(file);
			FileOutputStream out = new FileOutputStream(file);
			out.write(data);
			out.close();
			mWaitCamReady = false;
			scheduleNextPicture();
		} catch (Exception e) {
			handleError(getClass().getSimpleName(), e.getMessage());
		}
	}

	@Override
	public void onAutoFocus(boolean success, Camera camera) {
		try {
			muteShutter();
			new Handler(Looper.getMainLooper()).postDelayed(() -> {
				if (mCamera != null) {
					camera.takePicture(null, null, pictureCallback);
				}
			}, mWaitCamReady ? mSettings.getCameraTriggerDelay() : 0);

		} catch (Exception e) {
			e.printStackTrace();
			handleError(getClass().getSimpleName(), e.getMessage());
		}
	}

	@Override
	public void run() {
		try {
			if (mEndTime > 0 && mEndTime < System.currentTimeMillis()) {
				success();  //tell service to stop
				return;
			}

			mStartPreviewTime = SystemClock.elapsedRealtime();

			if (mCamera == null)
				prepareRecord();

			Log.d("Camera", "Wait:" + mWaitCamReady);

			new Handler(Looper.getMainLooper()).postDelayed(() -> {
				if (mCamera != null) {
					mCamera.startPreview();
					if (mUseAutoFocus) {
						mCamera.autoFocus(autoFocusCallback);
					} else {
						onAutoFocus(true, mCamera);
					}
				}
			}, mWaitCamReady ? mSettings.getCameraInitDelay() : 0);

		} catch (Exception e) {
			Log.e("Error", "startPreview");
			e.printStackTrace();
			handleError(getClass().getSimpleName(), e.getMessage());
		}
	}

	protected void setWhiteBalance(Camera.Parameters params, Set<String> suppModes) {
		if (suppModes.contains(Camera.Parameters.WHITE_BALANCE_AUTO)) {
			params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
		}
	}

	protected void setFocusMode(Camera.Parameters params, Set<String> suppModes) {
		if (mSettings.getCaptureRate() < CONTINUOUS_CAPTURE_THRESHOLD && suppModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
			params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		} else if (suppModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO) && mSettings.getCaptureRate() < CONTINUOUS_CAPTURE_THRESHOLD) {
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

		params.setPictureSize(mSettings.getFrameWidth(), mSettings.getFrameHeight());

		params.setJpegQuality(mSettings.getJpegQuality());
		params.setRotation(getCameraRotation(mSettings.getCameraId()));
		params.setExposureCompensation(mSettings.getExposureCompensation());
		params.setZoom(mSettings.getZoom());
		params.setFlashMode(mSettings.getCameraFlash() ? Camera.Parameters.FLASH_MODE_ON : Camera.Parameters.FLASH_MODE_OFF);
		mCamera.setParameters(params);

		mCamera.setErrorCallback(this);
	}

	protected void prepareRecord() throws IOException {
		mWaitCamReady = mCamera == null;
		releaseCamera();

		mCamera = Camera.open(mSettings.getCameraId());

		setCameraParams();

		SurfaceTexture surfaceTexture = new SurfaceTexture(10);
		mCamera.setPreviewTexture(surfaceTexture);

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
				handleError(getClass().getSimpleName(), "Unkown error occured while recording");
				break;
		}
	}
}
