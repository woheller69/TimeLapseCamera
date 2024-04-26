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

public class DeviceStatusReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		boolean stopOnLowBattery = prefs.getBoolean("pref_stop_low_battery", true);
		boolean stopOnLowStorage = prefs.getBoolean("pref_stop_low_storage", true);
		/*
		 * battery or storage is low if we don't stop the recording the mp4
		 * files get corrupted and are not playable any more
		 */
		if ((stopOnLowBattery && intent.getAction().equals(Intent.ACTION_BATTERY_LOW)) || (stopOnLowStorage && intent.getAction().equals(Intent.ACTION_DEVICE_STORAGE_LOW)) || intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
			Intent stopintent = new Intent(context, Camera1ForegroundService.class);
			stopintent.setAction(Camera1ForegroundService.ACTION_STOP_SERVICE);
			context.startService(stopintent);
		}

		//if (intent.getAction().equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) Log.d("DeviceStatusReceiver","ACTION_AIRPLANE_MODE_CHANGED");  //for testing: Activate filter in MainActivity

	}
}
