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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import at.andreasrohner.spartantimelapserec.sensor.MuteShutter;

public class MainActivity extends AppCompatActivity  {

	private static SettingsFragment settingsFragment;
	private static BroadcastReceiver broadcastReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (broadcastReceiver==null) broadcastReceiver = new DeviceStatusReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.ACTION_BATTERY_LOW");
		filter.addAction("android.intent.action.ACTION_DEVICE_STORAGE_LOW");
		//filter.addAction("android.intent.action.AIRPLANE_MODE"); //for testing
		ContextCompat.registerReceiver(getApplicationContext(),broadcastReceiver, filter, ContextCompat.RECEIVER_EXPORTED);

		if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED && !shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) ||
			(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED && !shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) ||
			(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED && !shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE))){
			ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE},123);
		}

		Context context = getApplicationContext();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SettingsCommon.setDefaultValues(context, prefs);
		if (GithubStar.shouldShowStarDialog(this)) GithubStar.starDialog(this,"https://github.com/woheller69/timelapsecamera");
	}

	@Override
	protected void onResume() {
		super.onResume();
		if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
		&& ((ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED))
		&& ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED))) {
			// Display the fragment as the main content.
			if (settingsFragment==null) {
				settingsFragment = new SettingsFragment();
				settingsFragment.setRetainInstance(true);  //do not recreate if orientation is changed
			}
				getFragmentManager().beginTransaction()
						.replace(android.R.id.content, settingsFragment)
						.commit();

		} else 	Toast.makeText(this, getString(R.string.error_missing_permission), Toast.LENGTH_SHORT).show();

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
		if (broadcastReceiver!=null && broadcastReceiver.isOrderedBroadcast()) unregisterReceiver(broadcastReceiver);
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
		Intent intent = new Intent(MainActivity.this, PreviewActivity.class);
		startActivity(intent);
	}

	public void actionUnmuteAllStreams(MenuItem item) {
		MuteShutter mute = new MuteShutter(this);
		mute.maxAllStreams();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
