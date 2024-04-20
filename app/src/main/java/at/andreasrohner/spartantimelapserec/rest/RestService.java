

package at.andreasrohner.spartantimelapserec.rest;

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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import androidx.core.app.NotificationCompat;
import at.andreasrohner.spartantimelapserec.MainActivity;
import at.andreasrohner.spartantimelapserec.R;

/**
 * HTTP REST API Service, for remote Control
 * <p>
 * Inspirited by SwiFTP https://github.com/ppareit/swiftp/blob/master/app/src/main/java/be/ppareit/swiftp/FsService.java
 * <p>
 * (c) Andreas Butti, 2024
 */
public class RestService extends Service implements Runnable {

	/**
	 * Log Tag
	 */
	private static final String TAG = RestService.class.getSimpleName();

	/**
	 * Notification ID
	 */
	public static final int NOTIF_ID = 1002;

	/**
	 * REST Notification ID
	 */
	private static final String CHANNEL_ID = "TimeLapseID_REST";

	/**
	 * Server started
	 */
	static public final String ACTION_STARTED = "at.andreasrohner.spartantimelapserec.rest.RestService.RESTAPI_STARTED";

	/**
	 * Server Stoped
	 */
	static public final String ACTION_STOPPED = "at.andreasrohner.spartantimelapserec.rest.RestService.RESTAPI_STOPPED";

	/**
	 * Server failed to start
	 */
	static public final String ACTION_FAILEDTOSTART = "at.andreasrohner.spartantimelapserec.rest.RestService.RESTAPI_FAILEDTOSTART";

	/**
	 * The server thread will check this often to look for incoming
	 * connections. We are forced to use non-blocking accept() and polling
	 * because we cannot wait forever in accept() if we want to be able
	 * to receive an exit signal and cleanly exit.
	 */
	public static final int WAKE_INTERVAL_MS = 1000;

	/**
	 * Server Thread
	 */
	protected static Thread serverThread = null;

	/**
	 * Flag which is set if the server is currently stopped
	 */
	protected boolean shouldExit = false;

	/**
	 * Listening Server Socket
	 */
	protected ServerSocket listenSocket;

	/**
	 * Make sure WiFi will be enabled all the time
	 */
	private WifiLock wifiLock = null;

	/**
	 * Prevent Sleep
	 */
	private WakeLock wakeLock;

	/**
	 * TCP Listener Logic
	 */
	private TcpListener wifiListener = null;

	/**
	 * Current open HTTP Sessions
	 */
	private final List<HttpThread> sessionThreads = new ArrayList<>();

	/**
	 * Constructor
	 */
	public RestService() {
	}

	/**
	 * Check to see if the FTP Server is up and running
	 *
	 * @return true if the FTP Server is up and running
	 */
	public static boolean isRunning() {
		// return true if and only if a server Thread is running
		if (serverThread == null) {
			Log.d(TAG, "Server is not running (null serverThread)");
			return false;
		}
		if (!serverThread.isAlive()) {
			Log.d(TAG, "serverThread non-null but !isAlive()");
		} else {
			Log.d(TAG, "Server is alive");
		}
		return true;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		setupNotification();

		//https://developer.android.com/reference/android/app/Service.html
		// if there are not any pending start commands to be delivered to the service, it will be called with a null intent object,
		if (intent != null && intent.getAction() != null) {
			Log.d(TAG, "onStartCommand called with action: " + intent.getAction());
		}

		shouldExit = false;
		int attempts = 10;
		// The previous server thread may still be cleaning up, wait for it to finish.
		while (serverThread != null) {
			Log.w(TAG, "Won't start, server thread exists");
			if (attempts > 0) {
				attempts--;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// Ignore
				}
			} else {
				Log.w(TAG, "Server thread already exists");
				return START_STICKY;
			}
		}
		Log.d(TAG, "Creating server thread");
		serverThread = new Thread(this);
		serverThread.start();
		return START_STICKY;
	}

	/**
	 * Setup the Notification
	 */
	private void setupNotification() {
		Log.d(TAG, "Setting up the notification");

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			notificationManager.createNotificationChannel(new NotificationChannel(CHANNEL_ID, "TimeLapse", NotificationManager.IMPORTANCE_DEFAULT));
		}
		Intent intent = new Intent(this, MainActivity.class);
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		PendingIntent pi = PendingIntent.getActivity(this, NOTIF_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

		// For N and below
		Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID).setSilent(true).setOnlyAlertOnce(true).setPriority(NotificationCompat.PRIORITY_DEFAULT) // For N and below
				.setContentIntent(pi).setSmallIcon(R.drawable.ic_camera).setAutoCancel(false).setOngoing(true).setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setContentText(getString(R.string.notification_restapi)).setContentTitle(getString(R.string.app_name)).build();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			startForeground(NOTIF_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA);
		} else {
			startForeground(NOTIF_ID, notification);
		}
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy() Stopping server");
		shouldExit = true;

		if (serverThread == null) {
			Log.w(TAG, "Stopping with null serverThread");
			return;
		}

		serverThread.interrupt();

		try {
			serverThread.join(10000); // wait 10 sec for server thread to finish
		} catch (InterruptedException ignored) {
			// Ignore exception
		}

		if (serverThread.isAlive()) {
			Log.w(TAG, "Server thread failed to exit");
			// it may still exit eventually if we just leave the shouldExit flag set
		} else {
			Log.d(TAG, "serverThread join()ed ok");
			serverThread = null;
		}

		try {
			if (listenSocket != null) {
				Log.i(TAG, "Closing listenSocket");
				listenSocket.close();
				listenSocket = null;
			}
		} catch (IOException ignored) {
			// Close failed, ignore
		}

		if (wifiLock != null) {
			Log.d(TAG, "onDestroy: Releasing wifi lock");
			wifiLock.release();
			wifiLock = null;
		}

		if (wakeLock != null) {
			Log.d(TAG, "onDestroy: Releasing wake lock");
			wakeLock.release();
			wakeLock = null;
		}

		Log.d(TAG, "RestService.onDestroy() finished");
	}

	@Override
	public void run() {
		Log.d(TAG, "Server thread running");

		if (!isConnectedToLocalNetwork(getApplicationContext())) {
			Log.w(TAG, "run: There is no local network, bailing out");
			stopSelf();
			sendBroadcast(new Intent(ACTION_FAILEDTOSTART));
			return;
		}

		// Initialization of wifi, set up the socket
		try {
			setupListener();
		} catch (IOException e) {
			Log.w(TAG, "run: Unable to open port, bailing out.", e);
			stopSelf();
			sendBroadcast(new Intent(ACTION_FAILEDTOSTART));
			return;
		}

		takeWifiLock();
		takeWakeLock();

		// A socket is open now, so the FTP server is started, notify rest of world
		Log.i(TAG, "HTTP Server up and running, broadcasting ACTION_STARTED");
		sendBroadcast(new Intent(ACTION_STARTED));

		while (!shouldExit) {
			if (wifiListener != null) {
				if (!wifiListener.isAlive()) {
					Log.d(TAG, "Joining crashed wifiListener thread");
					try {
						wifiListener.join();
					} catch (InterruptedException ignored) {
					}
					wifiListener = null;
				}
			}
			if (wifiListener == null) {
				// Either our wifi listener hasn't been created yet, or has crashed,
				// so spawn it
				wifiListener = new TcpListener(listenSocket, this);
				wifiListener.start();
			}

			try {
				// the main socket to send an exit signal
				Thread.sleep(WAKE_INTERVAL_MS);
			} catch (InterruptedException e) {
				Log.d(TAG, "Thread interrupted");
			}
		}

		terminateAllSessions();

		if (wifiListener != null) {
			wifiListener.quit();
			wifiListener = null;
		}
		shouldExit = false; // we handled the exit flag, so reset it to acknowledge
		Log.d(TAG, "Exiting cleanly, returning from run()");

		stopSelf();
		sendBroadcast(new Intent(ACTION_STOPPED));
	}

	/**
	 * Takes the wake lock
	 * <p>
	 * Many devices seem to not properly honor a PARTIAL_WAKE_LOCK, which should prevent
	 * CPU throttling. For these devices, we have a option to force the phone into a full
	 * wake lock.
	 */
	private void takeWakeLock() {
		if (wakeLock == null) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			if (true /* currently not configurable FsSettings.shouldTakeFullWakeLock() */) {
				Log.d(TAG, "takeWakeLock: Taking full wake lock");
				wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, TAG);
			} else {
				Log.d(TAG, "maybeTakeWakeLock: Taking partial wake lock");
				wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
			}
			wakeLock.setReferenceCounted(false);
		}
		wakeLock.acquire();
	}

	/**
	 * Make sure WiFi will be enabled all the time
	 */
	private void takeWifiLock() {
		Log.d(TAG, "takeWifiLock: Taking wifi lock");
		if (wifiLock == null) {
			WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
			wifiLock = manager.createWifiLock(TAG);
			wifiLock.setReferenceCounted(false);
		}
		wifiLock.acquire();
	}

	/**
	 * This opens a listening socket on all interfaces.
	 *
	 * @throws IOException
	 */
	private void setupListener() throws IOException {
		listenSocket = new ServerSocket();
		listenSocket.setReuseAddress(true);

		listenSocket.bind(new InetSocketAddress(getPort(getApplicationContext())));
	}

	/**
	 * Read the Port from Settings
	 *
	 * @param ctx Context
	 * @return Port
	 */
	public static int getPort(Context ctx) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		int port = 8085;
		try {
			port = Integer.parseInt(prefs.getString("pref_restapi_port", "8085"));
		} catch (Exception e) {
			// Ignore invalid value
			Log.d(TAG, "Invalid port", e);
		}
		if (port < 1024 || port > 65535) {
			Log.w(TAG, "Port invalid: " + port);
			Toast.makeText(ctx, ctx.getString(R.string.error_port_invalid), Toast.LENGTH_SHORT).show();
			port = 8085;
		}

		return port;
	}

	/**
	 * The RestService must know about all running session threads so they can be
	 * terminated on exit. Called when a new session is created.
	 */
	public void registerSessionThread(HttpThread newSession) {
		// Before adding the new session thread, clean up any finished session
		// threads that are present in the list.

		// Since we're not allowed to modify the list while iterating over
		// it, we construct a list in toBeRemoved of threads to remove
		// later from the sessionThreads list.
		synchronized (this) {
			for (Iterator<HttpThread> it = sessionThreads.iterator(); it.hasNext(); ) {
				HttpThread sessionThread = it.next();

				if (!sessionThread.isAlive()) {
					Log.d(TAG, "Cleaning up finished session...");
					try {
						sessionThread.join();
						Log.d(TAG, "Thread joined");
						it.remove();
						sessionThread.close(); // make sure socket closed
					} catch (InterruptedException e) {
						Log.d(TAG, "Interrupted while joining");
						// We will try again in the next loop iteration
					}
				}
			}

			// Cleanup is complete. Now actually add the new thread to the list.
			sessionThreads.add(newSession);
		}
		Log.d(TAG, "Registered session thread");
	}

	/**
	 * Terminate all running HTTP Sessions
	 */
	private void terminateAllSessions() {
		Log.i(TAG, "Terminating " + sessionThreads.size() + " session thread(s)");
		synchronized (this) {
			for (HttpThread sessionThread : sessionThreads) {
				if (sessionThread != null) {
					sessionThread.close();
				}
			}
		}
	}

	/**
	 * Gets the local ip address
	 *
	 * @param context Context
	 * @return local ip address or null if not found
	 */
	public static InetAddress getLocalInetAddress(Context context) {
		InetAddress returnAddress = null;
		if (!isConnectedToLocalNetwork(context)) {
			Log.e(TAG, "getLocalInetAddress called and no connection");
			return null;
		}
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			if (interfaces == null) {
				Log.e(TAG, "Could not get Network interfaces!");
				return null;
			}
			List<NetworkInterface> networkInterfaces = Collections.list(interfaces);
			for (NetworkInterface networkInterface : networkInterfaces) {
				// only check network interfaces that give local connection
				if (!networkInterface.getName().matches("^(eth|wlan).*")) {
					continue;
				}
				for (InetAddress address : Collections.list(networkInterface.getInetAddresses())) {
					if (!address.isLoopbackAddress() && !address.isLinkLocalAddress() && address instanceof Inet4Address) {
						if (returnAddress != null) {
							Log.w(TAG, "Found more than one valid address local inet address, why???");
						}
						returnAddress = address;
					}
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "Failed to get local IP", e);
		}
		return returnAddress;
	}

	/**
	 * Checks to see if we are connected to a local network, for instance wifi or ethernet
	 *
	 * @param context Context
	 * @return true if connected to a local network
	 */
	public static boolean isConnectedToLocalNetwork(Context context) {
		boolean connected = false;
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		connected = ni != null && ni.isConnected() && (ni.getType() & (ConnectivityManager.TYPE_WIFI | ConnectivityManager.TYPE_ETHERNET)) != 0;
		if (!connected) {
			Log.d(TAG, "isConnectedToLocalNetwork: see if it is an WIFI AP");
			WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
			try {
				Method method = wm.getClass().getDeclaredMethod("isWifiApEnabled");
				connected = (Boolean) method.invoke(wm);
			} catch (Exception e) {
				Log.e(TAG, "Failed to check WiFi connection", e);
			}
		}
		if (!connected) {
			Log.d(TAG, "isConnectedToLocalNetwork: see if it is an USB AP");
			try {
				ArrayList<NetworkInterface> networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
				for (NetworkInterface netInterface : networkInterfaces) {
					if (netInterface.getDisplayName().startsWith("rndis")) {
						connected = true;
					}
				}
			} catch (Exception e) {
				Log.e(TAG, "Failed to check Ethernet connection", e);
			}
		}
		return connected;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		super.onTaskRemoved(rootIntent);
		Log.d(TAG, "user has removed my activity, we got killed! restarting...");
		Intent restartService = new Intent(getApplicationContext(), this.getClass());
		restartService.setPackage(getPackageName());
		PendingIntent restartServicePI = PendingIntent.getService(getApplicationContext(), 1, restartService, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
		AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 2000, restartServicePI);
	}
}