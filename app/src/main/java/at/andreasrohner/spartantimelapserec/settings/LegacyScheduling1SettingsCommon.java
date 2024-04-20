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

package at.andreasrohner.spartantimelapserec.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import java.text.Normalizer;

import at.andreasrohner.spartantimelapserec.ForegroundService;
import at.andreasrohner.spartantimelapserec.data.RecSettings;
import at.andreasrohner.spartantimelapserec.preference.DateTimePreference;
import at.andreasrohner.spartantimelapserec.preference.SeekBarPreference;

public class LegacyScheduling1SettingsCommon extends BaseLegacySettingsCommon implements OnSharedPreferenceChangeListener {

	private SeekBarPreference prefInitialDelay;

	private SeekBarPreference prefStopRecAfter;

	private DateTimePreference prefScheduleRec;

	public void onCreate(Context context, PreferenceScreen screen) {
		this.context = context;

		SharedPreferences prefs = screen.getSharedPreferences();

		prefInitialDelay = (SeekBarPreference) screen.findPreference("pref_initial_delay");
		prefScheduleRec = (DateTimePreference) screen.findPreference("pref_schedule_recording");
		prefStopRecAfter = (SeekBarPreference) screen.findPreference("pref_stop_recording_after");

		prefInitialDelay.setOnFormatOutputValueListener(this);
		int value = prefs.getInt("pref_initial_delay", -1);
		if (value != -1) {
			prefInitialDelay.setSummary(FormatUtil.formatTime(value, context));
		}

		prefScheduleRec.setSummary(prefScheduleRec.formatDateTime());

		prefStopRecAfter.setOnFormatOutputValueListener(this);
		value = prefs.getInt("pref_stop_recording_after", -1);
		if (value != -1) {
			prefStopRecAfter.setSummary(onFormatOutputValue(value, "min"));
		}
	}

	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		if (key.equals("pref_initial_delay")) {
			prefInitialDelay.setSummary(FormatUtil.formatTime(prefInitialDelay.getmValue(), context));
		} else if (key.equals("pref_schedule_recording")) {
			prefScheduleRec.setSummary(prefScheduleRec.formatDateTime());
			RecSettings settings = new RecSettings();
			settings.load(context, PreferenceManager.getDefaultSharedPreferences(context));
			if (settings.isSchedRecEnabled() && settings.getSchedRecTime() > System.currentTimeMillis()) {
				Intent intent = new Intent(context, ForegroundService.class);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					context.startForegroundService(intent);
				} else {
					context.startService(intent);
				}
			} else {
				Intent intent = new Intent(context, ForegroundService.class);
				intent.setAction(ForegroundService.ACTION_STOP_SERVICE);
				context.startService(intent);
			}
		} else if (key.equals("pref_stop_recording_after")) {
			prefStopRecAfter.setSummary(onFormatOutputValue(prefStopRecAfter.getmValue(), "min"));
		}
	}

	public void onResume(PreferenceScreen screen) {
		screen.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	public void onPause(PreferenceScreen screen) {
		screen.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
}
