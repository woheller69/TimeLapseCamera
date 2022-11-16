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
import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.PowerManager.WakeLock;
import android.text.format.DateFormat;
import android.view.SurfaceHolder;
import at.andreasrohner.spartantimelapserec.data.RecSettings;
import at.andreasrohner.spartantimelapserec.sensor.MuteShutter;
import at.andreasrohner.spartantimelapserec.sensor.OrientationSensor;

public abstract class Recorder {
	protected Context mContext;
	protected RecSettings mSettings;
	protected SurfaceHolder mSurfaceHolder;
	protected Camera mCamera;
	protected boolean mCanDisableShutterSound;
	protected Handler mHandler;
	protected int mInitDelay;
	private OrientationSensor mOrientation;
	protected MuteShutter mMute;
	private File mOutputDir;
	private int mFileIndex;

	public static Recorder getInstance(RecSettings settings,
			SurfaceHolder surfaceHolder, Context context, Handler handler,
			WakeLock wakeLock) {
		Recorder recorder;

		switch (settings.getRecMode()) {
		case VIDEO_TIME_LAPSE:
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				recorder = new VideoRecorder(settings, surfaceHolder, context,
						handler);
			} else {
				recorder = new VideoTimeLapseRecorder(settings, surfaceHolder,
						context, handler);
			}
			break;
		case IMAGE_TIME_LAPSE:
			if (settings.shouldUsePowerSaveMode()) {
				recorder = new PowerSavingImageRecorder(settings,
						surfaceHolder, context, handler, wakeLock);
			} else {
				recorder = new ImageRecorder(settings, surfaceHolder, context,
						handler);
			}
			break;
		default:
			recorder = new VideoRecorder(settings, surfaceHolder, context,
					handler);
			break;
		}

		return recorder;
	}

	@SuppressLint("NewApi")
	public Recorder(RecSettings settings, SurfaceHolder surfaceHolder,
			Context context, Handler handler) {
		mContext = context;

		mOrientation = new OrientationSensor(context);
		mOrientation.enable();

		mSettings = settings;
		mSurfaceHolder = surfaceHolder;
		mHandler = handler;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(mSettings.getCameraId(), info);
			mCanDisableShutterSound = info.canDisableShutterSound;
		}

		mMute = new MuteShutter(context);
		mOutputDir = new File(settings.getProjectPath() + "/"
				+ settings.getProjectName() + "/"
				+ DateFormat.format("yyyy-MM-dd", System.currentTimeMillis())
				+ "/");
		mOutputDir.mkdirs();

		mInitDelay = settings.getInitDelay();
	}

	protected void releaseCamera() {
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

	protected void handleError(String tag, String msg) {
		if (mHandler != null) {
			Message m = new Message();
			Bundle b = new Bundle();
			b.putString("status", "error");
			b.putString("tag", tag);
			b.putString("msg", msg);
			m.setData(b);
			m.setTarget(mHandler);
			mHandler.sendMessage(m);
			mHandler = null;
		}
	}

	protected void success() {
		if (mHandler != null) {
			Message m = new Message();
			Bundle b = new Bundle();
			b.putString("status", "success");
			m.setData(b);
			m.setTarget(mHandler);
			mHandler.sendMessage(m);
		}
	}

	protected void disableOrientationSensor() {
		if (mOrientation != null)
			mOrientation.disable();
	}

	protected void enableOrientationSensor() {
		if (mOrientation != null)
			mOrientation.enable();
	}

	public void stop() {
		disableOrientationSensor();
		mOrientation = null;

		releaseCamera();
		unmuteShutter();

		mHandler = null;
		mSurfaceHolder = null;
		mContext = null;
		mMute = null;
	}

	protected abstract void prepareRecord() throws Exception;

	protected abstract void doRecord() throws Exception;

	public void start() {
		long timeDiff = SystemClock.elapsedRealtime();
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					doRecord();
				} catch (Exception e) {
					e.printStackTrace();
					handleError(Recorder.class.getSimpleName(), e.getMessage());
				}
			}
		};

		if (mHandler == null || mSurfaceHolder == null || mContext == null)
			return;

		enableOrientationSensor();

		try {
			prepareRecord();
		} catch (Exception e) {
			e.printStackTrace();
			handleError(getClass().getSimpleName(), e.getMessage());
			return;
		}

		timeDiff = SystemClock.elapsedRealtime() - timeDiff;
		timeDiff = mInitDelay - timeDiff;
		if (timeDiff <= 0)
			mHandler.post(r);
		else
			mHandler.postDelayed(r, timeDiff);

		mInitDelay = 0;
	}

	public File getOutputDir() {
		return mOutputDir;
	}

	protected File getOutputFile(String ext) throws IOException {
		File outFile;
		do {
			outFile = new File(mOutputDir, mSettings.getProjectName()
					+ mFileIndex + "." + ext);
			mFileIndex++;
		} while (outFile.isFile());

		if (!mOutputDir.isDirectory())
			throw new IOException("Could not open directory");

		return outFile;
	}

	protected int getCameraRotation(int cameraId) {
		if (mOrientation != null)
			return mOrientation.getCameraRotation(cameraId);
		return 0;
	}

	protected void setCameraOrientation(int cameraId) {
		Camera.Parameters params = mCamera.getParameters();

		params.setRotation(getCameraRotation(cameraId));

		mCamera.setParameters(params);
	}
	protected void setExposureCompensation() {
		Camera.Parameters params = mCamera.getParameters();
		params.setExposureCompensation(mSettings.getExposureCompensation());
		mCamera.setParameters(params);
	}

	@SuppressLint("NewApi")
	protected void muteShutter() {
		if (mSettings != null && mSettings.isMuteShutter()) {
			if (mCanDisableShutterSound) {
				// don't merge with upper if (to prevent elseif-branch if
				// mCamera == null)
				if (mCamera != null)
					mCamera.enableShutterSound(false);
			} else if (mMute != null) {
				mMute.muteShutter();
			}
		}

	}

	@SuppressLint("NewApi")
	protected void unmuteShutter() {
		if (mSettings != null && mSettings.isMuteShutter()) {
			if (mCanDisableShutterSound) {
				// don't merge with upper if (to prevent elseif-branch if
				// mCamera == null)
				if (mCamera != null)
					mCamera.enableShutterSound(true);
			} else if (mMute != null) {
				mMute.unmuteShutter();
			}
		}
	}
}
