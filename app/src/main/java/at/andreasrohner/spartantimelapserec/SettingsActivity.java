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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.R;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class SettingsActivity extends PreferenceActivity {
	SettingsCommon settCommon;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		final Context context = getApplicationContext();

		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);

		settCommon = new SettingsCommon();
		settCommon.onCreate(context, getPreferenceScreen());

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if (prefs.getBoolean("pref_settings_shown", false) == false)
			prefs.edit().putBoolean("pref_settings_shown", true).commit();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onResume() {
		super.onResume();
		settCommon.onResume(getPreferenceScreen());
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onPause() {
		super.onPause();
		settCommon.onPause(getPreferenceScreen());
	}
}
