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

package at.andreasrohner.spartantimelapserec.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.CamcorderProfile;

import androidx.preference.PreferenceManager;

public class RecSettingsLegacy extends SchedulingSettings {

	private int cameraId;

	private String projectName;

	private RecMode recMode;

	private int frameRate;

	private int captureRate;

	private boolean muteShutter;

	private boolean stopOnLowBattery;

	private boolean stopOnLowStorage;

	private int initDelay;

	private int jpegQuality;

	private int frameWidth;

	private int frameHeight;

	private int recProfile;

	private int exposureCompensation;

	private int zoom;

	private int cameraInitDelay;

	private int cameraTriggerDelay;

	private boolean cameraFlash;

	private int videoEncodingBitRate;

	public static int getInteger(SharedPreferences prefs, String key, int def) {
		try {
			return Integer.parseInt(prefs.getString(key, ""));
		} catch (NumberFormatException e) {
		}
		return def;
	}

	public static RecMode getRecMode(SharedPreferences prefs) {
		return RecSettingsLegacy.getRecMode(prefs, "pref_rec_mode", RecMode.VIDEO_TIME_LAPSE);
	}

	public static RecMode getRecMode(SharedPreferences prefs, String key, RecMode def) {
		try {
			return RecMode.valueOf(prefs.getString(key, ""));
		} catch (Exception e) {
		}
		return def;
	}

	private boolean checkRecProfile(int profile) {
		try {
			if (CamcorderProfile.hasProfile(cameraId, profile)) {
				return true;
			}
		} catch (Exception e) {
		}

		return false;
	}

	private int selectRecVideoProfile() {
		if (checkRecProfile(CamcorderProfile.QUALITY_1080P) && frameHeight == 1080)
			return CamcorderProfile.QUALITY_1080P;
		if (checkRecProfile(CamcorderProfile.QUALITY_720P) && frameHeight == 720)
			return CamcorderProfile.QUALITY_720P;
		if (checkRecProfile(CamcorderProfile.QUALITY_480P) && frameHeight == 480)
			return CamcorderProfile.QUALITY_480P;

		if (checkRecProfile(CamcorderProfile.QUALITY_HIGH) && frameHeight >= 480)
			return CamcorderProfile.QUALITY_HIGH;

		if (checkRecProfile(CamcorderProfile.QUALITY_QVGA) && frameHeight == 240)
			return CamcorderProfile.QUALITY_QVGA;

		if (checkRecProfile(CamcorderProfile.QUALITY_QCIF) && frameHeight == 144)
			return CamcorderProfile.QUALITY_QCIF;

		if (checkRecProfile(CamcorderProfile.QUALITY_LOW))
			return CamcorderProfile.QUALITY_LOW;

		return CamcorderProfile.QUALITY_HIGH;
	}

	private int selectRecVideoTimeLapseProfile() {
		if (checkRecProfile(CamcorderProfile.QUALITY_TIME_LAPSE_1080P) && frameHeight == 1080)
			return CamcorderProfile.QUALITY_TIME_LAPSE_1080P;
		if (checkRecProfile(CamcorderProfile.QUALITY_TIME_LAPSE_720P) && frameHeight == 720)
			return CamcorderProfile.QUALITY_TIME_LAPSE_720P;
		if (checkRecProfile(CamcorderProfile.QUALITY_TIME_LAPSE_480P) && frameHeight == 480)
			return CamcorderProfile.QUALITY_TIME_LAPSE_480P;

		if (checkRecProfile(CamcorderProfile.QUALITY_TIME_LAPSE_HIGH) && frameHeight >= 480)
			return CamcorderProfile.QUALITY_TIME_LAPSE_HIGH;

		if (checkRecProfile(CamcorderProfile.QUALITY_TIME_LAPSE_QVGA) && frameHeight == 240)
			return CamcorderProfile.QUALITY_TIME_LAPSE_QVGA;

		if (checkRecProfile(CamcorderProfile.QUALITY_TIME_LAPSE_QCIF) && frameHeight == 144)
			return CamcorderProfile.QUALITY_TIME_LAPSE_QCIF;

		if (checkRecProfile(CamcorderProfile.QUALITY_TIME_LAPSE_LOW))
			return CamcorderProfile.QUALITY_TIME_LAPSE_LOW;

		return CamcorderProfile.QUALITY_TIME_LAPSE_HIGH;
	}

	/**
	 * Load settings from Context
	 *
	 * @param context Context
	 */
	public void load(Context context) {
		super.load(context);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		videoEncodingBitRate = getInteger(prefs, "pref_video_encoding_br", 0);
		cameraId = getInteger(prefs, "pref_camera", 0);
		projectName = prefs.getString("pref_project_title", "");

		recMode = getRecMode(prefs);

		frameRate = getInteger(prefs, "pref_frame_rate", 30);
		captureRate = prefs.getInt("pref_capture_rate", 1000);
		muteShutter = prefs.getBoolean("pref_mute_shutter", true);
		stopOnLowBattery = prefs.getBoolean("pref_stop_low_battery", true);
		stopOnLowStorage = prefs.getBoolean("pref_stop_low_storage", true);
		initDelay = prefs.getInt("pref_initial_delay", 1000);
		jpegQuality = prefs.getInt("pref_jpeg_quality", 90);
		exposureCompensation = prefs.getInt("pref_exposurecomp", 0);
		zoom = prefs.getInt("pref_zoom", 0);
		cameraInitDelay = prefs.getInt("pref_camera_init_delay", 500);
		cameraTriggerDelay = prefs.getInt("pref_camera_trigger_delay", 1000);
		cameraFlash = prefs.getBoolean("pref_flash", false);

		String[] size = prefs.getString("pref_frame_size", "1920x1080").split("x");
		try {
			frameWidth = Integer.valueOf(size[0]);
			frameHeight = Integer.valueOf(size[1]);
		} catch (NumberFormatException e) {
			frameWidth = 1920;
			frameHeight = 1080;
		}

		if (recMode != RecMode.IMAGE_TIME_LAPSE) {
			if (recMode == RecMode.VIDEO_TIME_LAPSE)
				recProfile = selectRecVideoTimeLapseProfile();
			else
				recProfile = selectRecVideoProfile();
		}
	}

	public boolean shouldUsePowerSaveMode() {
		if (captureRate > 10 * 1000)
			return true;
		return false;
	}

	public int getInitDelay() {
		return initDelay;
	}

	public void setInitDelay(int initDelay) {
		this.initDelay = initDelay;
	}

	public int getCameraId() {
		return cameraId;
	}

	public void setCameraId(int cameraId) {
		this.cameraId = cameraId;
	}

	public RecMode getRecMode() {
		return recMode;
	}

	public void setRecMode(RecMode recMode) {
		this.recMode = recMode;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public int getFrameRate() {
		return frameRate;
	}

	public void setFrameRate(int frameRate) {
		this.frameRate = frameRate;
	}

	public int getCaptureRate() {
		return captureRate;
	}

	public void setCaptureRate(int captureRate) {
		this.captureRate = captureRate;
	}

	public boolean isMuteShutter() {
		return muteShutter;
	}

	public void setMuteShutter(boolean muteShutter) {
		this.muteShutter = muteShutter;
	}

	public int getJpegQuality() {
		return jpegQuality;
	}

	public void setJpegQuality(int jpegQuality) {
		this.jpegQuality = jpegQuality;
	}

	public int getFrameWidth() {
		return frameWidth;
	}

	public void setFrameWidth(int frameWidth) {
		this.frameWidth = frameWidth;
	}

	public int getFrameHeight() {
		return frameHeight;
	}

	public void setFrameHeight(int frameHeight) {
		this.frameHeight = frameHeight;
	}

	public int getRecProfile() {
		return recProfile;
	}

	public void setRecProfile(int recProfile) {
		this.recProfile = recProfile;
	}

	public boolean isStopOnLowBattery() {
		return stopOnLowBattery;
	}

	public void setStopOnLowBattery(boolean stopOnLowBattery) {
		this.stopOnLowBattery = stopOnLowBattery;
	}

	public boolean isStopOnLowStorage() {
		return stopOnLowStorage;
	}

	public void setStopOnLowStorage(boolean stopOnLowStorage) {
		this.stopOnLowStorage = stopOnLowStorage;
	}

	public int getExposureCompensation() {
		return exposureCompensation;
	}

	public int getZoom() {
		return zoom;
	}

	public int getCameraInitDelay() {
		return cameraInitDelay;
	}

	public int getCameraTriggerDelay() {
		return cameraTriggerDelay;
	}

	public boolean getCameraFlash() {
		return cameraFlash;
	}

	public int getVideoEncodingBitRate() {
		return videoEncodingBitRate;
	}
}
