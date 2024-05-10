package at.andreasrohner.spartantimelapserec;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.text.format.DateFormat;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.data.SchedulingSettings;
import at.andreasrohner.spartantimelapserec.state.Logger;

import static android.os.Environment.DIRECTORY_PICTURES;

/**
 * Base class for Service implementation
 */
public abstract class BaseForegroundService extends Service implements Handler.Callback {

	/**
	 * Logger
	 */
	private Logger logger = new Logger(getClass());

	/**
	 * Action to stop the service
	 */
	public static final String ACTION_STOP_SERVICE = "TimeLapse.action.STOP_SERVICE";

	/**
	 * Notification ID
	 */
	private static final int NOTIF_ID = 123;

	/**
	 * Channel ID
	 */
	private static final String CHANNEL_ID = "TimeLapseID";

	/**
	 * Listener for state changes
	 */
	private static final List<ServiceStatusListener> listener = new ArrayList<>();

	/**
	 * Current state of the service
	 */
	private static ServiceState status = new ServiceState(ServiceState.State.INIT, "Initialized");

	/**
	 * Wake lock, make sure the application don't get killed while recording...
	 */
	protected PowerManager.WakeLock wakeLock;

	/**
	 * Handler Thread
	 */
	private HandlerThread handlerThread;

	/**
	 * Handler
	 */
	protected Handler handler;

	/**
	 * Notification Manager
	 */
	private NotificationManager notificationManager;

	/**
	 * Output dir for images / videos
	 */
	private File outputDir;

	/**
	 * Constructor
	 */
	public BaseForegroundService() {
	}

	/**
	 * @return Current status of the service
	 */
	public static ServiceState getStatus() {
		return status;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * Register listener
	 *
	 * @param l Listener
	 */
	public static void registerStatusListener(ServiceStatusListener l) {
		synchronized (listener) {
			// make sure the listener is added just once
			listener.remove(l);
			listener.add(l);
		}
	}

	/**
	 * Inform the listener about the state change
	 *
	 * @param status New state
	 */
	protected void fireStateChanged(ServiceState status) {
		synchronized (listener) {
			BaseForegroundService.status = status;
			for (ServiceStatusListener l : listener) {
				l.onServiceStatusChange(status);
			}
		}
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null || !ACTION_STOP_SERVICE.equals(intent.getAction())) {
			initNotification();
			initWakeLock();
			initHandler();

			if (startupScheduled()) {
				return START_STICKY;
			}

			String projectPath = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES).getPath();

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String projectName = prefs.getString("pref_project_title", "NO_NAME");
			this.outputDir = new File(projectPath, projectName + "/" + DateFormat.format("yyyy-MM-dd", System.currentTimeMillis()) + "/");
			logger.mark("Project Folder: «{}}»", this.outputDir);

			startupService();
			updateNotification();
			fireStateChanged(new ServiceState(ServiceState.State.RUNNING, "onStartCommand"));
			return START_STICKY;
		} else {
			String reason = intent.getStringExtra("reason");
			if (reason == null) {
				reason = "No reason received with intent";
			}
			shutdownService(reason);
			return START_NOT_STICKY;
		}
	}

	/**
	 * Scheduled startup. Do not start now, just plan startup
	 *
	 * @return true if scheduled start is enabled
	 */
	private boolean startupScheduled() {
		SchedulingSettings settings = new SchedulingSettings();
		settings.load(getApplicationContext());

		if (!settings.isSchedRecEnabled()) {
			return false;
		}

		if (settings.getSchedRecTime() > System.currentTimeMillis() + 10000) {
			AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			Intent newintent = new Intent(getApplicationContext(), ScheduleReceiver.class);
			PendingIntent alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, newintent, PendingIntent.FLAG_IMMUTABLE);
			alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, settings.getSchedRecTime(), alarmIntent);
			java.text.DateFormat f = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM, SimpleDateFormat.SHORT);

			String startDate = f.format(settings.getSchedRecTime());
			logger.mark("Start scheduled for {}", startDate);
			fireStateChanged(new ServiceState(ServiceState.State.SCHEDULED, startDate));
			return true;
		}

		return false;
	}

	/**
	 * @return Output dir for images / videos
	 */
	public File getOutputDir() {
		return outputDir;
	}

	/**
	 * Service startup
	 */
	protected abstract void startupService();

	/**
	 * Service shutdown
	 *
	 * @param reason Reason
	 */
	protected void shutdownService(String reason) {
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(this, ScheduleReceiver.class);
		PendingIntent alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
		alarmManager.cancel(alarmIntent);

		stop(reason);
	}

	/**
	 * Aquire / init wake lock
	 */
	protected void initWakeLock() {
		PowerManager mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
		wakeLock.acquire();
	}

	/**
	 * Initialize handler / handler thread
	 */
	protected void initHandler() {
		handlerThread = new HandlerThread("recordingVideo");
		handlerThread.start();
		handler = new Handler(handlerThread.getLooper(), this);
	}

	/**
	 * Initialize the notification
	 */
	protected void initNotification() {
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			notificationManager.createNotificationChannel(new NotificationChannel(CHANNEL_ID, "TimeLapse", NotificationManager.IMPORTANCE_DEFAULT));
		}
		Intent intent = new Intent(this, MainActivity.class);
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		PendingIntent pi = PendingIntent.getActivity(this, NOTIF_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

		// For N and below
		Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID).setSilent(true).setOnlyAlertOnce(true).setPriority(NotificationCompat.PRIORITY_DEFAULT) // For N and below
				.setContentIntent(pi).setSmallIcon(R.drawable.ic_camera).setAutoCancel(false).setOngoing(true).setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setContentText(getString(R.string.notification_preparing)).setContentTitle(getString(R.string.app_name)).build();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			startForeground(NOTIF_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA);
		} else {
			startForeground(NOTIF_ID, notification);
		}
	}

	/**
	 * Update notification
	 */
	protected void updateNotification() {
		Intent intent = new Intent(this, MainActivity.class);
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);

		PendingIntent pi = PendingIntent.getActivity(this, NOTIF_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
		Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID).setSilent(true).setOnlyAlertOnce(true).setPriority(NotificationCompat.PRIORITY_DEFAULT) // For N and below
				.setContentIntent(pi).setSmallIcon(R.drawable.ic_camera).setAutoCancel(false).setOngoing(true).setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setContentText(getString(R.string.info_recording_running)).setContentTitle(getString(R.string.app_name)).build();
		notificationManager.notify(NOTIF_ID, notification);
	}

	/**
	 * Stop recording
	 *
	 * @param reason Reason to stop
	 */
	protected void stop(String reason) {
		stopRecording();

		if (wakeLock != null && wakeLock.isHeld()) {
			wakeLock.release();
		}

		if (outputDir != null && outputDir.exists()) {
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(outputDir)));
		}

		fireStateChanged(new ServiceState(ServiceState.State.STOPPED, reason));
		stopForeground(true);
		stopSelf();
	}

	/**
	 * Stop recording
	 */
	protected abstract void stopRecording();

	@Override
	public boolean handleMessage(Message m) {
		String status = m.getData().getString("status");
		String msg = m.getData().getString("msg");

		if ("error".equals(status)) {
			stop("Error: " + msg);
		} else if ("success".equals(status)) {
			stop("Ended with success");
		}

		return true;
	}
}