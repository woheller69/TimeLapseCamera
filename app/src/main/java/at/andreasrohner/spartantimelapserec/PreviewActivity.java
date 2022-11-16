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

package at.andreasrohner.spartantimelapserec;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import at.andreasrohner.spartantimelapserec.data.RecSettings;

public class PreviewActivity extends Activity implements ErrorCallback,
		AutoFocusCallback {
	private Preview mPreview;
	private Camera mCamera;
	private RecSettings mSettings;
	private boolean mUseAutoFocus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Hide the window title.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mSettings = new RecSettings();
		mSettings.load(getApplicationContext(), PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext()));

		// Create a RelativeLayout container that will hold a SurfaceView,
		// and set it as the content of our activity.
		mPreview = new Preview(this, mSettings);
		setContentView(mPreview);
	}

	private void releaseCamera() {
		if (mCamera == null)
			return;

		try {
			mCamera.reconnect();
			mCamera.release();
		} catch (Exception e) {
			e.printStackTrace();
		}
		mCamera = null;
	}

	private void setWhiteBalance(Camera.Parameters params, Set<String> suppModes) {
		if (suppModes.contains(Camera.Parameters.WHITE_BALANCE_AUTO)) {
			params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
		}
	}

	@SuppressLint("InlinedApi")
	private void setFocusMode(Camera.Parameters params, Set<String> suppModes) {
		if (suppModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
			params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
		} else if (suppModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
			params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			mUseAutoFocus = true;
		}
	}

	private static void setCameraDisplayOrientation(Activity activity,
			int cameraId, Camera camera) {
		CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(cameraId, info);
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		camera.setDisplayOrientation(result);
	}

	private void setCameraParams() throws IOException {
		Camera.Parameters params = mCamera.getParameters();

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
		params.setExposureCompensation(mSettings.getExposureCompensation());

		mCamera.setParameters(params);

		setCameraDisplayOrientation(this, mSettings.getCameraId(), mCamera);

		mCamera.setErrorCallback(this);
	}

	private void preparePreview() throws IOException {
		releaseCamera();

		mCamera = Camera.open(mSettings.getCameraId());

		setCameraParams();

		mPreview.setCamera(mCamera);
	}

	private void startPreview() {
		try {
			if (mCamera == null)
				preparePreview();

			mCamera.startPreview();

			if (mUseAutoFocus)
				mCamera.autoFocus(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		startPreview();
	}

	@Override
	public void onPause() {
		super.onPause();

		// Because the Camera object is a shared resource, it's very
		// important to release it when the activity is paused.
		if (mCamera != null) {
			mPreview.setCamera(null);
			releaseCamera();
		}
	}

	@Override
	public void onAutoFocus(boolean arg0, Camera arg1) {
	}

	@Override
	public void onError(int error, Camera camera) {
		String msg;

		switch (error) {
		case Camera.CAMERA_ERROR_SERVER_DIED:
			msg = "Cameraserver died";
			break;
		default:
			msg = "Unkown error occured while recording";
			break;
		}

		final Context context = getApplicationContext();
		final String text = getClass().getSimpleName() + " Error: " + msg;

		mPreview.post(new Runnable() {
			@Override
			public void run() {
				Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
				toast.show();
			}
		});
	}
}

/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
/**
 * A simple wrapper around a Camera and a SurfaceView that renders a centered
 * preview of the Camera to the surface. We need to center the SurfaceView
 * because not all devices have cameras that support preview sizes at the same
 * aspect ratio as the device's display.
 */
class Preview extends ViewGroup implements SurfaceHolder.Callback {
	private final String TAG = "Preview";

	private SurfaceView mSurfaceView;
	private SurfaceHolder mHolder;
	private Camera mCamera;
	private RecSettings mSettings;
	private Size mPreviewSize;
	private List<Size> mSupportedPreviewSizes;

	@SuppressWarnings("deprecation")
	Preview(Context context, RecSettings settings) {
		super(context);

		mSettings = settings;

		mSurfaceView = new SurfaceView(context);
		addView(mSurfaceView);

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = mSurfaceView.getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void setCamera(Camera camera) {
		mCamera = camera;
		if (mCamera != null) {
			mSupportedPreviewSizes = mCamera.getParameters()
					.getSupportedPreviewSizes();

			if (mSupportedPreviewSizes != null) {
				mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes,
						mSettings.getFrameWidth(), mSettings.getFrameHeight());
			}
			requestLayout();
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (changed && getChildCount() > 0) {
			final View child = getChildAt(0);

			final int width = r - l;
			final int height = b - t;

			int previewWidth = width;
			int previewHeight = height;
			if (mPreviewSize != null) {
				previewWidth = mPreviewSize.width;
				previewHeight = mPreviewSize.height;
			}

			int rotation = ((Activity) getContext()).getWindowManager()
					.getDefaultDisplay().getRotation();
			switch (rotation) {
			case Surface.ROTATION_0:
			case Surface.ROTATION_180:
				if (mPreviewSize != null) {
					previewWidth = mPreviewSize.height;
					previewHeight = mPreviewSize.width;
				}
				break;
			}

			// Center the child SurfaceView within the parent.
			if (width * previewHeight > height * previewWidth) {
				final int scaledChildWidth = previewWidth * height
						/ previewHeight;
				child.layout((width - scaledChildWidth) / 2, 0,
						(width + scaledChildWidth) / 2, height);
			} else {
				final int scaledChildHeight = previewHeight * width
						/ previewWidth;
				child.layout(0, (height - scaledChildHeight) / 2, width,
						(height + scaledChildHeight) / 2);
			}
		}
	}

	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		try {
			if (mCamera != null) {
				mCamera.setPreviewDisplay(holder);
			}
		} catch (IOException exception) {
			Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		if (mCamera != null) {
			mCamera.stopPreview();
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		requestLayout();

		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);

		mCamera.setParameters(parameters);

		mCamera.startPreview();
	}

}