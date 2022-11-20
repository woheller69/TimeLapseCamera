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

import java.io.File;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;

import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import at.andreasrohner.spartantimelapserec.data.RecSettings;
import at.andreasrohner.spartantimelapserec.recorder.Recorder;

public class BackgroundService extends Service implements
		SurfaceHolder.Callback, Handler.Callback {
	private static final String TAG = "TimeLapse:BackgroundService";
	private static final int NOTIFICATION_ID = 95430432;
	private static boolean created;
	private SurfaceView surfaceView;
	private WakeLock wakeLock;
	private RecSettings settings;
	private Recorder recorder;
	private HandlerThread handlerThread;

	public static boolean isCreated() {
		return created;
	}

	private void createNotification(int id) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				this,getString(R.string.app_name)).setSmallIcon(settings.getNotificationIcon())
				.setContentTitle(getString(R.string.app_name))
				.setContentText(getString(R.string.notification_content_text));

		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, MainActivity.class);
		// The stack builder object will contain an artificial back stack for
		// the started Activity. This ensures that navigating backward from the
		// Activity leads out of your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(MainActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
				PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(resultPendingIntent);

		Notification notification = builder.build();
		// Start foreground service to avoid unexpected kill
		startForeground(id, notification);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null
				&& intent.getBooleanExtra("powerSavingCallback", false)
				&& recorder != null && wakeLock != null && settings != null
				&& settings.shouldUsePowerSaveMode() && handlerThread != null
				&& handlerThread.isAlive()) {
			wakeLock.acquire();
			Handler handler = new Handler(handlerThread.getLooper());
			handler.post(new Runnable() {
				@Override
				public void run() {
					recorder.start();
				}
			});
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate() {
		created = true;

		settings = new RecSettings();
		settings.load(getApplicationContext(), PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext()));

		createNotification(NOTIFICATION_ID);

		if (settings.isSchedRecEnabled()
				&& settings.getSchedRecTime() > System.currentTimeMillis() + 10000) {
			AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			Intent intent = new Intent(getApplicationContext(),
					ScheduleReceiver.class);
			PendingIntent alarmIntent = PendingIntent.getBroadcast(
					getApplicationContext(), 0, intent, 0);

			alarmMgr.set(AlarmManager.RTC_WAKEUP, settings.getSchedRecTime(),
					alarmIntent);

			stopSelf();
			return;
		}

		// Create new SurfaceView, set its size to 1x1, move
		// it to the top left corner and set this service as a callback
		WindowManager winMgr = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		surfaceView = new SurfaceView(this);
		// deprecated setting, but required on Android versions prior to 3.0
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
			surfaceView.getHolder().setType(
					SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		surfaceView.getHolder().addCallback(this);

		LayoutParams layoutParams = new WindowManager.LayoutParams(1, 1,
				WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
				WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
				PixelFormat.TRANSLUCENT);
		layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
		winMgr.addView(surfaceView, layoutParams);

		PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		wakeLock.setReferenceCounted(false);
		wakeLock.acquire();
	}

	@Override
	public void surfaceCreated(final SurfaceHolder surfaceHolder) {
		handlerThread = new HandlerThread("recordingVideo");
		handlerThread.start();

		Context context = getApplicationContext();
		Handler handler = new Handler(handlerThread.getLooper(), this);

		recorder = Recorder.getInstance(settings, context,
				handler, wakeLock);

		handler.post(new Runnable() {
			@Override
			public void run() {
				recorder.start();
			}
		});
	}

	@Override
	public void onDestroy() {
		created = false;
		File projectDir = null;

		final Handler handler = new Handler(handlerThread.getLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (recorder != null)
					recorder.stop();
				handlerThread.quit();
			}
		});

		try {
			handlerThread.join(5000);
			if (handlerThread.isAlive()) {
				handlerThread.quit();
				handlerThread.join(2000);
			}
		} catch (Exception e) {
		}

		if (recorder != null) {
			projectDir = recorder.getOutputDir();
			recorder = null;
		}

		if (surfaceView != null) {
			WindowManager winMgr = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
			winMgr.removeView(surfaceView);
		}

		if (wakeLock != null)
			wakeLock.release();

		if (projectDir != null && projectDir.exists())
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
					Uri.fromFile(projectDir)));
	}

	@Override
	public boolean handleMessage(Message m) {
		String status = m.getData().getString("status");
		String tag = m.getData().getString("tag");
		String msg = m.getData().getString("msg");

		String name = getString(R.string.app_name);

		if ("error".equals(status)) {
			Log.e(tag, "Error: " + msg);

			final Context context = getApplicationContext();
			final String text = name + " Error: " + msg;

			surfaceView.post(new Runnable() {
				@Override
				public void run() {
					Toast toast = Toast.makeText(context, text,
							Toast.LENGTH_LONG);
					toast.show();
				}
			});
		}

		stopSelf();
		return true;
	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int format,
			int width, int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
