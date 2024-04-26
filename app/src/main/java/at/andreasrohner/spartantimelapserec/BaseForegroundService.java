package at.andreasrohner.spartantimelapserec;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;

import java.util.ArrayList;
import java.util.List;

import androidx.core.app.NotificationCompat;

/**
 * Base class for Service implementation
 */
public abstract class BaseForegroundService extends Service implements Handler.Callback {

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
	 * Running flag
	 */
	private static boolean running = false;

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
	 * Constructor
	 */
	public BaseForegroundService() {
	}

	/**
	 * @return true if the service is currently running
	 */
	public static boolean isRunning() {
		return running;
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
	protected void fireStateChanged(boolean status) {
		synchronized (listener) {
			running = status;
			for (ServiceStatusListener l : listener) {
				l.onServiceStatusChange(status);
			}
		}
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null || !ACTION_STOP_SERVICE.equals(intent.getAction())) {
			initNotification();
			startupService();
			fireStateChanged(true);
			return START_STICKY;
		} else {
			shutdownService();
			return START_NOT_STICKY;
		}
	}

	/**
	 * Service startup
	 */
	protected abstract void startupService();

	/**
	 * Service shutdown
	 */
	protected abstract void shutdownService();

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
	 */
	protected void stop() {
		if (wakeLock != null && wakeLock.isHeld()) {
			wakeLock.release();
		}
/*
		if (projectDir != null && projectDir.exists()) {
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(projectDir)));
		}
*/
	}
}