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

import androidx.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.preference.DateTimePreference;

/**
 * Load and parse scheduling settings
 */
public class SchedulingSettings {

	/**
	 * Initial delay
	 */
	private int initDelay;

	/**
	 * Recoding start timestamp
	 */
	private long schedRecTime;

	/**
	 * Scheduled recording is enabled
	 */
	private boolean schedRecEnabled;

	/**
	 * Stop record after (-1 to disabled)
	 */
	private int stopRecAfter;

	/**
	 * Load settings from Context
	 *
	 * @param context Context
	 */
	public void load(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		stopRecAfter = prefs.getInt("pref_stop_recording_after", 60 * 48 /* two days */);

		// negative value disables the limit
		if (stopRecAfter >= 47 * 60) {
			stopRecAfter = -1;
		} else {
			// convert to milli seconds
			stopRecAfter *= 60 * 1000;
		}

		String schedRecValue = prefs.getString("pref_schedule_recording", null);
		if (schedRecValue != null) {
			schedRecEnabled = DateTimePreference.parseEnabled(schedRecValue);
			if (schedRecEnabled) {
				schedRecTime = DateTimePreference.parseTime(schedRecValue);
			}
		} else {
			schedRecEnabled = false;
		}
		
		initDelay = prefs.getInt("pref_initial_delay", 1000);
	}

	/**
	 * @return Recoding start timestamp
	 */
	public long getSchedRecTime() {
		return schedRecTime;
	}

	/**
	 * @return Scheduled recording is enabled
	 */
	public boolean isSchedRecEnabled() {
		return schedRecEnabled;
	}

	/**
	 * @return Stop record after (-1 to disabled)
	 */
	public int getStopRecAfter() {
		return stopRecAfter;
	}

	/**
	 * @return Initial delay
	 */
	public int getInitDelay() {
		return initDelay;
	}

	/**
	 * @param initDelay Initial delay
	 */
	public void setInitDelay(int initDelay) {
		this.initDelay = initDelay;
	}
}
