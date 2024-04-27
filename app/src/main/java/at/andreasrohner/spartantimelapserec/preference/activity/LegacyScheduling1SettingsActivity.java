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

package at.andreasrohner.spartantimelapserec.preference.activity;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import at.andreasrohner.spartantimelapserec.preference.LegacyScheduling1SettingsFragment;

/**
 * Legacy scheduling settings of Camera1 interface
 */
public class LegacyScheduling1SettingsActivity extends AppCompatActivity {

	private static LegacyScheduling1SettingsFragment settingsFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Display the fragment as the main content.
		if (settingsFragment == null) {
			settingsFragment = new LegacyScheduling1SettingsFragment();
			settingsFragment.setRetainInstance(true);  //do not recreate if orientation is changed
		}
		getFragmentManager().beginTransaction().replace(android.R.id.content, settingsFragment).commit();

		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			if (!alarmManager.canScheduleExactAlarms()) {
				Intent intent = new Intent();
				intent.setAction(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
				startActivity(intent);
			}
		}
	}
}
