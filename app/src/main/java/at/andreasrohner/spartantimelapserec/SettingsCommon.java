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

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import at.andreasrohner.spartantimelapserec.data.RecMode;
import at.andreasrohner.spartantimelapserec.data.RecSettings;
import at.andreasrohner.spartantimelapserec.preference.CameraInformation;
import at.andreasrohner.spartantimelapserec.preference.DateTimePreference;
import at.andreasrohner.spartantimelapserec.preference.IntervalPickerPreference;
import at.andreasrohner.spartantimelapserec.preference.IpInformation;
import at.andreasrohner.spartantimelapserec.preference.SeekBarPreference;
import at.andreasrohner.spartantimelapserec.preference.StopInformation;
import at.andreasrohner.spartantimelapserec.sensor.CameraSettings;
import at.andreasrohner.spartantimelapserec.settings.RestControlUtil;

public class SettingsCommon implements OnSharedPreferenceChangeListener {

	private Context context;

	private ListPreference prefRecMode;

	private CameraInformation prefCameraInformation;

	private IpInformation prefIpInformation;

	private StopInformation prefStopInformation;

	private void setRecMode(SharedPreferences prefs) {

		CharSequence entry = prefRecMode.getEntry();
		if (entry != null) {
			prefRecMode.setSummary(entry);
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		if (key.equals("pref_rec_mode")) {
			setRecMode(prefs);
		} else if (key.equals("pref_restapi_port")) {
			prefIpInformation.updateData();
		} else if (key.equals("pref_stop_low_battery") || key.equals("pref_stop_low_storage")) {
			prefStopInformation.updateData();
		}
	}

	public void onCreate(Context context, PreferenceScreen screen) {
		this.context = context;
		SharedPreferences prefs = screen.getSharedPreferences();
		prefRecMode = (ListPreference) screen.findPreference("pref_rec_mode");

		prefCameraInformation = (CameraInformation) screen.findPreference("pref_camera_settings_information");
		prefIpInformation = (IpInformation) screen.findPreference("pref_restapi_information");
		prefStopInformation = (StopInformation) screen.findPreference("pref_stop_settings_information");

		setRecMode(prefs);
	}

	public void onResume(PreferenceScreen screen) {
		screen.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		prefCameraInformation.updateData();
		prefIpInformation.updateData();
		prefStopInformation.updateData();
	}

	public void onPause(PreferenceScreen screen) {
		screen.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
}
