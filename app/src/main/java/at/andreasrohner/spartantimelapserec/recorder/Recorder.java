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
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import at.andreasrohner.spartantimelapserec.StatusSenderUtil;
import at.andreasrohner.spartantimelapserec.data.RecSettingsLegacy;
import at.andreasrohner.spartantimelapserec.sensor.MuteShutter;
import at.andreasrohner.spartantimelapserec.sensor.OrientationSensor;

public abstract class Recorder {

	protected Context mContext;

	protected RecSettingsLegacy mSettings;

	protected Camera mCamera;

	protected boolean mCanDisableShutterSound;

	protected Handler mHandler;

	protected int mInitDelay;

	private OrientationSensor mOrientation;

	protected MuteShutter mMute;

	private File mOutputDir;

	private int mFileIndex;

	public static Recorder getInstance(RecSettingsLegacy settings, Context context, Handler handler, WakeLock wakeLock, File outputDir) {
		Recorder recorder;

		switch (settings.getRecMode()) {
			case VIDEO_TIME_LAPSE:
				recorder = new VideoTimeLapseRecorder(settings, context, handler, outputDir);
				break;
			case IMAGE_TIME_LAPSE:
				if (settings.shouldUsePowerSaveMode()) {
					recorder = new PowerSavingImageRecorder(settings, context, handler, wakeLock, outputDir);
				} else {
					recorder = new ImageRecorder(settings, context, handler, outputDir);
				}
				break;
			default:
				recorder = new VideoRecorder(settings, context, handler, outputDir);
				break;
		}

		return recorder;
	}

	public Recorder(RecSettingsLegacy settings, Context context, Handler handler, File outputDir) {
		mContext = context;

		mOrientation = new OrientationSensor(context);
		mOrientation.enable();

		mSettings = settings;
		mHandler = handler;

		CameraInfo info = new CameraInfo();
		Camera.getCameraInfo(mSettings.getCameraId(), info);
		mCanDisableShutterSound = info.canDisableShutterSound;

		mMute = new MuteShutter(context);
		mOutputDir = outputDir;

		if (!mOutputDir.exists() && !mOutputDir.mkdirs()) {
			Log.e("TimeLapseCamera", "Failed to make directory: " + mOutputDir.toString());
			return;
		}

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
		StatusSenderUtil.sendError(mHandler, tag, msg);
		mHandler = null;
	}

	protected void success() {
		StatusSenderUtil.sendSuccess(mHandler);
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

		if (mHandler == null || mContext == null)
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
			outFile = new File(mOutputDir, mSettings.getProjectName() + mFileIndex + "." + ext);
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
