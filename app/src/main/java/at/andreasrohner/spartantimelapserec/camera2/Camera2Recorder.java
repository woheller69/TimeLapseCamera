package at.andreasrohner.spartantimelapserec.camera2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import androidx.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.data.SchedulingSettings;
import at.andreasrohner.spartantimelapserec.rest.HttpThread;

/**
 * Handle Recording
 */
public class Camera2Recorder implements Runnable {

	/**
	 * Log Tag
	 */
	private static final String TAG = HttpThread.class.getSimpleName();

	/**
	 * Context
	 */
	private final Context context;

	/**
	 * Handler
	 */
	private final Handler handler;

	/**
	 * Camera wrapper
	 */
	private Camera2Wrapper camera;

	/**
	 * Interval time in ms
	 */
	private int captureIntervalTime;

	/**
	 * Running State
	 */
	private boolean running = true;

	/**
	 * Constructor
	 *
	 * @param context Context
	 * @param handler Handler
	 */
	public Camera2Recorder(Context context, Handler handler) {
		this.context = context;
		this.handler = handler;
	}

	/**
	 * Start Recording
	 */
	public void start() {
		SchedulingSettings settings = new SchedulingSettings();
		settings.load(context);

		int start = settings.getInitDelay();
		if (start < 100) {
			// Add forced initial delay to make sure all listener are attached...
			start = 100;
		}
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		captureIntervalTime = prefs.getInt("pref_capture_rate", 1000);

		camera = new Camera2Wrapper(context);
		camera.open();
		running = true;
		handler.postDelayed(this, start);
	}

	/**
	 * Timer callback, called in handler thread
	 */
	@Override
	public void run() {
		if (!running) {
			Log.i(TAG, "Stop now because of running flag");
			return;
		}
		// Schedule next image
		handler.postDelayed(this, captureIntervalTime);

		Log.e(TAG, "Test take image");

		//	camera.takeImage();
		// TODO Take image
		//StatusSenderUtil.sendError(handler, "ABC", "Test crash!");
	}

	/**
	 * Stop Recoding
	 */
	public void stop() {
		running = false;
		handler.removeCallbacks(this);
	}
}
