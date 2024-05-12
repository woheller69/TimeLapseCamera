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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

/**
 * Receive states, stop on low storage / low battery
 */
public class DeviceStatusReceiver extends BroadcastReceiver {

	/**
	 * Constructor
	 */
	public DeviceStatusReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		boolean stopOnLowBattery = prefs.getBoolean("pref_stop_low_battery", true);
		boolean stopOnLowStorage = prefs.getBoolean("pref_stop_low_storage", true);

		ServiceHelper serviceHelper = new ServiceHelper(context);
		/*
		 * battery or storage is low if we don't stop the recording the mp4
		 * files get corrupted and are not playable any more
		 */
		if (stopOnLowBattery && intent.getAction().equals(Intent.ACTION_BATTERY_LOW)) {
			// Mark this as error, battery empty is an error
			serviceHelper.stop("Battery low", true);
		}
		if (stopOnLowStorage && intent.getAction().equals(Intent.ACTION_DEVICE_STORAGE_LOW)) {
			// Mark this as error, storage full is an error
			serviceHelper.stop("Storage low", true);
		}
		if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
			serviceHelper.stop("ACTION_SHUTDOWN", false);
		}

		// for testing: Activate filter in MainActivity
		// if (intent.getAction().equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) Log.d("DeviceStatusReceiver","ACTION_AIRPLANE_MODE_CHANGED");
	}
}
