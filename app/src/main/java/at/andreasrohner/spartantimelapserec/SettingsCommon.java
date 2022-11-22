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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import at.andreasrohner.spartantimelapserec.data.RecMode;
import at.andreasrohner.spartantimelapserec.data.RecSettings;
import at.andreasrohner.spartantimelapserec.preference.DateTimePreference;
import at.andreasrohner.spartantimelapserec.preference.SeekBarPreference;
import at.andreasrohner.spartantimelapserec.sensor.CameraSettings;

public class SettingsCommon implements OnSharedPreferenceChangeListener,
		SeekBarPreference.OnFormatOutputValueListener {
	private Context context;
	private CameraSettings cameraSettings;
	private ListPreference prefFrameSize;
	private ListPreference prefFrameRate;
	private ListPreference prefCamera;
	private ListPreference prefRecMode;
	private SeekBarPreference prefInitialDelay;
	private SeekBarPreference prefCaptureRate;
	private SeekBarPreference prefJpegQuality;
	private DateTimePreference prefScheduleRec;
	private SeekBarPreference prefStopRecAfter;
	private SeekBarPreference prefExposureComp;
	private SeekBarPreference prefZoom;

	private int calcGcd(int a, int b) {
		if (b == 0)
			return a;
		return calcGcd(b, a % b);
	}

	private void setFrameSizes(final SharedPreferences prefs) {
		int camId = RecSettings.getInteger(prefs, "pref_camera", 0);
		String defSize = prefs.getString("pref_frame_size", "1920x1080");

		final List<int[]> sizes;
		switch (RecSettings.getRecMode(prefs, "pref_rec_mode", RecMode.VIDEO_TIME_LAPSE)) {
		case IMAGE_TIME_LAPSE:
			sizes = cameraSettings.getPictureSizes(prefs, camId);
			break;
		case VIDEO:
			sizes = cameraSettings.getFrameSizes(prefs, camId, false);
			break;
		default:
			sizes = cameraSettings.getFrameSizes(prefs, camId, true);
			break;
		}

		final ArrayList<String> sizesList = new ArrayList<String>();
		final ArrayList<String> sizesListVal = new ArrayList<String>();
		int defInd = sizes.size() - 1;

		for (int[] size : sizes) {
			int gcd = calcGcd(size[0], size[1]);
			String value = size[0] + "x" + size[1];
			if (defSize.equals(value))
				defInd = sizesListVal.size();

			sizesList.add(value + " (" + (size[0] / gcd) + ":"
					+ (size[1] / gcd) + ")");
			sizesListVal.add(value);

		}

		final int index = defInd;
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				prefFrameSize.setEntries(sizesList.toArray(new String[sizesList.size()]));
				prefFrameSize.setEntryValues(sizesListVal.toArray(new String[sizesListVal.size()]));

				if (index >= 0 && sizesList.size() > 0) {
					prefFrameSize.setValueIndex(index);
					prefFrameSize.setSummary(sizesList.get(index));
				}

				updatePrefStatus(prefs);
			}
		});
	}

	private void setFrameRates(final SharedPreferences prefs) {
		int camId = RecSettings.getInteger(prefs, "pref_camera", 0);
		String defFps = prefs.getString("pref_frame_rate", "30");

		final List<Integer> fpsIntList = cameraSettings.getFrameRates(prefs, camId);
		final ArrayList<String> fpsList = new ArrayList<String>();
		final ArrayList<String> fpsListVal = new ArrayList<String>();
		int defInd = fpsIntList.size() - 1;

		for (Integer fpsInt : fpsIntList) {
			String fps = fpsInt.toString();
			if (defFps.equals(fps))
				defInd = fpsList.size();

			fpsListVal.add(fps);
			fpsList.add(fps + " fps");
		}

		final int index = defInd;
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				prefFrameRate.setEntries(fpsList.toArray(new String[fpsList.size()]));
				prefFrameRate.setEntryValues(fpsListVal.toArray(new String[fpsListVal.size()]));

				if (index >= 0 && fpsList.size() > 0) {
					prefFrameRate.setValueIndex(index);
					prefFrameRate.setSummary(fpsList.get(index));
				}
				updatePrefStatus(prefs);
			}
		});
	}

	private void delayedInit(final SharedPreferences prefs) {
		prefFrameRate.setEnabled(false);
		prefFrameSize.setEnabled(false);

		new Thread(new Runnable() {
			@Override
			public void run() {
				setFrameRates(prefs);
				setFrameSizes(prefs);
			}
		}).start();
	}

	private void setCameras(SharedPreferences prefs) {
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		int cameraCount = Camera.getNumberOfCameras();

		String[] camList = new String[cameraCount];
		String[] camListInd = new String[cameraCount];

		for (int i = 0; i < cameraCount; ++i) {
			Camera.getCameraInfo(i, cameraInfo);
			String item = context.getString(R.string.pref_camera_camera) + " "
					+ i + " (";

			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
				item += context.getString(R.string.pref_camera_front);
			else
				item += context.getString(R.string.pref_camera_back);
			item += ")";

			camList[i] = item;
			camListInd[i] = String.valueOf(i);
		}

		prefCamera.setEntries(camList);
		prefCamera.setEntryValues(camListInd);

		int camId = RecSettings.getInteger(prefs, "pref_camera", 0);
		prefCamera.setSummary(camList[camId]);
	}

	private void updatePrefStatus(SharedPreferences prefs) {
		switch (RecSettings.getRecMode(prefs, "pref_rec_mode", RecMode.VIDEO_TIME_LAPSE)) {
		case IMAGE_TIME_LAPSE:
			prefFrameRate.setEnabled(false);
			prefCaptureRate.setEnabled(true);
			prefJpegQuality.setEnabled(true);
			break;
		case VIDEO:
			prefFrameRate.setEnabled(true);
			prefCaptureRate.setEnabled(false);
			prefJpegQuality.setEnabled(false);
			break;
		default:
			prefFrameRate.setEnabled(true);
			prefCaptureRate.setEnabled(true);
			prefJpegQuality.setEnabled(false);
			break;
		}

		// disable the empty lists
		if (prefFrameRate.getEntries() == null
				|| prefFrameRate.getEntries().length == 0) {
			prefFrameRate.setSummary(null);
			prefFrameRate.setEnabled(false);
		}

		if (prefFrameSize.getEntries() == null
				|| prefFrameSize.getEntries().length == 0) {
			prefFrameSize.setSummary(null);
			prefFrameSize.setEnabled(false);
		} else {
			prefFrameSize.setEnabled(true);
		}

		if (prefCamera.getEntries() == null
				|| prefCamera.getEntries().length == 0) {
			prefCamera.setSummary(null);
			prefCamera.setEnabled(false);
		}
	}

	private void setRecMode(SharedPreferences prefs) {

		CharSequence entry = prefRecMode.getEntry();
		if (entry != null) {
			prefRecMode.setSummary(entry);
		}
	}

	private String formatTime(int millis) {
		if (millis < 1000)
			return millis + " ms";

		double secs = ((double) (millis % 60000)) / 1000;
		String formatSec = context.getString(R.string.time_format_sec);
		String formatSecs = context.getString(R.string.time_format_secs);
		DecimalFormat df = new DecimalFormat("#.##");

		if (millis >= 1000 && millis < 60000)
			return df.format(secs) + ((secs == 1) ? formatSec : formatSecs);

		int intSecs = millis % 60000 / 1000;
		int mins = (millis % 3600000) / 1000 / 60;
		int hours = (millis / 1000 / 60 / 60);

		String formatMin = context.getString(R.string.time_format_min);
		String formatMins = context.getString(R.string.time_format_mins);
		String formatHour = context.getString(R.string.time_format_hour);
		String formatHours = context.getString(R.string.time_format_hours);
		String res = "";
		if (hours == 1)
			res += hours + formatHour;
		else if (hours > 0)
			res += hours + formatHours;

		if (mins == 1)
			res += " " + mins + formatMin;
		else if (mins > 0)
			res += " " + mins + formatMins;

		if (intSecs == 1)
			res += " " + intSecs + formatSec;
		else if (intSecs > 0)
			res += " " + intSecs + formatSecs;

		return res;
	}

	@Override
	public String onFormatOutputValue(int value, String suffix) {
		if ("ms".equals(suffix))
			return formatTime(value);
		else if ("min".equals(suffix)) {
			if (value >= 47 * 60)
				return context.getString(R.string.pref_infinite);
			return formatTime(value * 1000 * 60);
		}
		return null;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		if (key.equals("pref_camera")) {
			CharSequence entry = prefCamera.getEntry();
			if (entry != null) {
				prefCamera.setSummary(entry);
			}
			setFrameRates(prefs);
			setFrameSizes(prefs);
			setExposureCompRange(prefs);
			setZoomRange(prefs);
			prefs.edit().putInt("pref_exposurecomp",0).apply();
		} else if (key.equals("pref_mute_shutter")) {
			boolean mute = prefs.getBoolean(key,false);
			//ToDo Show dialog and explain that this is needed to allow muting the phone
			NotificationManager notificationManager =(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			if (!notificationManager.isNotificationPolicyAccessGranted() && mute) {
				Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			}
		} else if (key.equals("pref_rec_mode")) {
			setRecMode(prefs);
			setFrameSizes(prefs);
		} else if (key.equals("pref_capture_rate")) {
			int value = prefs.getInt(key, -1);
			if (value != -1)
				prefCaptureRate.setSummary(formatTime(value));
		} else if (key.equals("pref_initial_delay")) {
			int value = prefs.getInt(key, -1);
			if (value != -1)
				prefInitialDelay.setSummary(formatTime(value));
		} else if (key.equals("pref_jpeg_quality")) {
			int value = prefs.getInt(key, -1);
			if (value != -1)
				prefJpegQuality.setSummary(value + " %");
		} else if (key.equals("pref_frame_size")) {
			prefFrameSize.setSummary(prefFrameSize.getEntry());
		} else if (key.equals("pref_frame_rate")) {
			prefFrameRate.setSummary(prefFrameRate.getEntry());
		} else if (key.equals("pref_schedule_recording")) {
			prefScheduleRec.setSummary(prefScheduleRec.formatDateTime());
		} else if (key.equals("pref_stop_recording_after")) {
			int value = prefs.getInt(key, -1);
			if (value != -1)
				prefStopRecAfter.setSummary(onFormatOutputValue(value, "min"));
		}

		updatePrefStatus(prefs);
	}

	public void onCreate(Context context, PreferenceScreen screen) {
		this.context = context;
		SharedPreferences prefs = screen.getSharedPreferences();
		cameraSettings = new CameraSettings();
		cameraSettings.prefetch(prefs);

		prefFrameSize = (ListPreference) screen.findPreference("pref_frame_size");
		prefFrameRate = (ListPreference) screen.findPreference("pref_frame_rate");
		prefCamera = (ListPreference) screen.findPreference("pref_camera");
		prefRecMode = (ListPreference) screen.findPreference("pref_rec_mode");
		prefCaptureRate = (SeekBarPreference) screen.findPreference("pref_capture_rate");
		prefJpegQuality = (SeekBarPreference) screen.findPreference("pref_jpeg_quality");
		prefInitialDelay = (SeekBarPreference) screen.findPreference("pref_initial_delay");
		prefScheduleRec = (DateTimePreference) screen.findPreference("pref_schedule_recording");
		prefStopRecAfter = (SeekBarPreference) screen.findPreference("pref_stop_recording_after");
		prefExposureComp = (SeekBarPreference) screen.findPreference("pref_exposurecomp");
		prefZoom = (SeekBarPreference) screen.findPreference("pref_zoom");
		setZoomRange(prefs);
		setExposureCompRange(prefs);
		setCameras(prefs);
		setRecMode(prefs);
		// Opening the camera object to
		// fetch the params can take some time
		delayedInit(prefs);

		prefCaptureRate.setOnFormatOutputValueListener(this);
		int value = prefs.getInt("pref_capture_rate", -1);
		if (value != -1)
			prefCaptureRate.setSummary(formatTime(value));

		prefInitialDelay.setOnFormatOutputValueListener(this);
		value = prefs.getInt("pref_initial_delay", -1);
		if (value != -1)
			prefInitialDelay.setSummary(formatTime(value));

		value = prefs.getInt("pref_jpeg_quality", -1);
		if (value != -1)
			prefJpegQuality.setSummary(value + " %");

		prefScheduleRec.setSummary(prefScheduleRec.formatDateTime());

		prefStopRecAfter.setOnFormatOutputValueListener(this);
		value = prefs.getInt("pref_stop_recording_after", -1);
		if (value != -1)
			prefStopRecAfter.setSummary(onFormatOutputValue(value, "min"));

		updatePrefStatus(prefs);
	}

	private void setZoomRange(SharedPreferences prefs) {
		int camId = RecSettings.getInteger(prefs, "pref_camera", 0);
		prefZoom.setMinValue(0);
		prefZoom.setMaxValue(cameraSettings.getMaxZoom(camId));
	}

	private void setExposureCompRange(SharedPreferences prefs) {
		int camId = RecSettings.getInteger(prefs, "pref_camera", 0);
		prefExposureComp.setMinValue(cameraSettings.getMinExposureCompensation(camId));
		prefExposureComp.setMaxValue(cameraSettings.getMaxExposureCompensation(camId));
	}

	public void onResume(PreferenceScreen screen) {
		screen.getSharedPreferences().registerOnSharedPreferenceChangeListener(
				this);
	}

	public void onPause(PreferenceScreen screen) {
		screen.getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	static public void setDefaultValues(Context context, SharedPreferences prefs) {
		PreferenceManager.setDefaultValues(context, R.xml.preferences, false);

	}
}
