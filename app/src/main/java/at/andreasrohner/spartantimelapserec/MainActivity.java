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

import android.Manifest;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import at.andreasrohner.spartantimelapserec.data.RecSettings;
import at.andreasrohner.spartantimelapserec.sensor.MuteShutter;

public class MainActivity extends AppCompatActivity implements ForegroundService.statusListener {

	private static SettingsFragment settingsFragment;
	private static BroadcastReceiver broadcastReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (broadcastReceiver==null) broadcastReceiver = new DeviceStatusReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.ACTION_BATTERY_LOW");
		filter.addAction("android.intent.action.ACTION_DEVICE_STORAGE_LOW");
		filter.addAction("android.intent.action.ACTION_SHUTDOWN");
		//filter.addAction("android.intent.action.AIRPLANE_MODE"); //for testing
		ContextCompat.registerReceiver(getApplicationContext(),broadcastReceiver, filter, ContextCompat.RECEIVER_EXPORTED);

		if (Build.VERSION.SDK_INT<Build.VERSION_CODES.TIRAMISU){  //WRITE_EXTERNAL_STORAGE is deprecated (and is not granted) when targeting Android 13+, in addition POST_NOTIFICATION is needed
			if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED && !shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) ||
					(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED && !shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) ||
					(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED && !shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE))){
				ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE},123);
			}
		} else {
			if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && !shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) ||
					(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && !shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) ||
					(ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED && !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS))) {
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.POST_NOTIFICATIONS}, 123);
			}
		}

		//SCHEDULE_EXACT_ALARM: on Android 12 this permission should usually be automatically granted by the Android system
		//For Android 13+ we use USE_EXACT_ALARM
		if (android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.S) {
			AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			if (!alarmManager.canScheduleExactAlarms()) {
				Intent intent2 = new Intent();
				intent2.setAction(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
				startActivity(intent2);
			}
		}
		Context context = getApplicationContext();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SettingsCommon.setDefaultValues(context, prefs);
		if (GithubStar.shouldShowStarDialog(this)) GithubStar.starDialog(this,"https://github.com/woheller69/timelapsecamera");
	}

	@Override
	protected void onResume() {
		super.onResume();
		ForegroundService.registerStatusListener(this);
		if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
		&& ((ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED))
		&& ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) || Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU)) {
			//PERMISSION POST_NOTIFICATION is required and not tested here

			// Display the fragment as the main content.
			if (settingsFragment==null) {
				settingsFragment = new SettingsFragment();
				settingsFragment.setRetainInstance(true);  //do not recreate if orientation is changed
			}
				getFragmentManager().beginTransaction()
						.replace(android.R.id.content, settingsFragment)
						.commit();

		} else 	Toast.makeText(this, getString(R.string.error_missing_permission), Toast.LENGTH_SHORT).show();

		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
			if (!alarmManager.canScheduleExactAlarms()) {
				Intent intent = new Intent();
				intent.setAction(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
				startActivity(intent);
			}
		}

	}

	public void actionStart(MenuItem item) {
		Intent intent = new Intent(this, ForegroundService.class);
		if (ForegroundService.mIsRunning){
			Toast.makeText(this, getString(R.string.error_already_running), Toast.LENGTH_SHORT).show();
		} else {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
				startForegroundService(intent);
			} else {
				startService(intent);
			}
		}

		invalidateOptionsMenu();

	}

	@Override
	protected void onDestroy() {
		broadcastReceiver = null;
		super.onDestroy();
	}

	public void actionStop(MenuItem item) {
		Intent intent = new Intent(this, ForegroundService.class);
		intent.setAction(ForegroundService.ACTION_STOP_SERVICE);
		startService(intent);

		invalidateOptionsMenu();
	}

	public void actionGallery(MenuItem item) {
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setType("image/*");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	public void actionPreview(MenuItem item) {
		if (!ForegroundService.mIsRunning) {
			Intent intent = new Intent(MainActivity.this, PreviewActivity.class);
			startActivity(intent);
		} else {
			Toast.makeText(this, getString(R.string.info_recording_running), Toast.LENGTH_SHORT).show();
		}
	}

	public void actionUnmuteAllStreams(MenuItem item) {
		MuteShutter mute = new MuteShutter(this);
		mute.maxAllStreams();
	}

	public void actionAbout(MenuItem item) {
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/woheller69/timelapsecamera")));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		if (ForegroundService.mIsRunning){
			menu.findItem(R.id.action_start).setEnabled(false);
			menu.findItem(R.id.action_start).setIcon(ContextCompat.getDrawable(this,R.drawable.ic_radio_button_checked_disabled_24px));
			menu.findItem(R.id.action_preview).setEnabled(false);
			menu.findItem(R.id.action_preview).setIcon(ContextCompat.getDrawable(this,R.drawable.ic_visibility_disabled_24px));

			RecSettings settings = new RecSettings();
			settings.load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
			if (settings.isSchedRecEnabled() && settings.getSchedRecTime() > System.currentTimeMillis()){
				menu.findItem(R.id.action_stop).setEnabled(false);
				menu.findItem(R.id.action_stop).setIcon(ContextCompat.getDrawable(this,R.drawable.ic_stop_circle_disabled_24px));
			}

		} else {
			menu.findItem(R.id.action_stop).setEnabled(false);
			menu.findItem(R.id.action_stop).setIcon(ContextCompat.getDrawable(this,R.drawable.ic_stop_circle_disabled_24px));
		}
		return true;
	}

	@Override
	public void onServiceStatusChange(boolean status) {
		invalidateOptionsMenu();
	}
}
