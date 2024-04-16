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
import android.os.Bundle;
import android.preference.PreferenceFragment;

import at.andreasrohner.spartantimelapserec.R;

/**
 * Legacy settings of Camera1 interface
 */
public class LegacyCamera1SettingsFragment extends PreferenceFragment {

	LegacyCamera1SettingsCommon settCommon;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Context context = getActivity().getApplicationContext();

		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.camera1_preferences);

		settCommon = new LegacyCamera1SettingsCommon();
		settCommon.onCreate(context, getPreferenceScreen());
	}

	@Override
	public void onResume() {
		super.onResume();
		settCommon.onResume(getPreferenceScreen());
	}

	@Override
	public void onPause() {
		super.onPause();
		settCommon.onPause(getPreferenceScreen());
	}
}
