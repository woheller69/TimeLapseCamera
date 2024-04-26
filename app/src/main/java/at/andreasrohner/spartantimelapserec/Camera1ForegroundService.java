package at.andreasrohner.spartantimelapserec;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Message;
import android.util.Log;

import java.io.File;

import androidx.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.data.RecSettings;
import at.andreasrohner.spartantimelapserec.recorder.Recorder;

public class Camera1ForegroundService extends BaseForegroundService  {

	private RecSettings settings;

	private Recorder recorder;

	/**
	 * Constructor
	 */
	public Camera1ForegroundService() {
	}

	@Override
	protected void startupService() {
		settings = new RecSettings();
		settings.load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

		if (settings.isSchedRecEnabled() && settings.getSchedRecTime() > System.currentTimeMillis() + 10000) {
			AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			Intent newintent = new Intent(getApplicationContext(), ScheduleReceiver.class);
			PendingIntent alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, newintent, PendingIntent.FLAG_IMMUTABLE);
			alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, settings.getSchedRecTime(), alarmIntent);

		} else {

			initWakeLock();
			initHandler();


			Context context = getApplicationContext();
			recorder = Recorder.getInstance(settings, context, handler, wakeLock);

			handler.post(new Runnable() {
				@Override
				public void run() {
					recorder.start();
				}
			});
			updateNotification();
		}
	}

	@Override
	protected void shutdownService() {
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(this, ScheduleReceiver.class);
		PendingIntent alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
		alarmManager.cancel(alarmIntent);

		stop();
	}

	/**
	 * Stop recording
	 */
	protected void stop() {

		File projectDir = null;

		if (recorder != null) {
			recorder.stop();
			projectDir = recorder.getOutputDir();
			recorder = null;
		}

		super.stop();

		if (projectDir != null && projectDir.exists()) {
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(projectDir)));
		}

		fireStateChanged(false);
		stopForeground(true);
		stopSelf();
	}

	@Override
	public boolean handleMessage(Message m) {
		String status = m.getData().getString("status");
		String tag = m.getData().getString("tag");
		String msg = m.getData().getString("msg");

		if ("error".equals(status)) {
			Log.e(tag, "Error: " + msg);
			stop();
		} else if ("success".equals(status)) {
			Log.e(tag, "Success");
			stop();
		}

		return true;
	}
}

