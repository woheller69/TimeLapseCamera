package at.andreasrohner.spartantimelapserec.camera2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.data.SchedulingSettings;
import at.andreasrohner.spartantimelapserec.rest.HttpThread;

/**
 * Handle Recording
 */
public class Camera2Recorder implements Runnable, TakePicture.ImageTakenListener {

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
	 * Handler to store image
	 */
	private Handler backgroundHandler;

	/**
	 * Thread to store image
	 */
	private HandlerThread backgroundThread;

	/**
	 * Flag to check if the image was created within the expected time
	 */
	private boolean waitForImage = false;

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

	@Override
	public void takeImageFinished() {
		waitForImage = false;
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

		backgroundThread = new HandlerThread("Picture Thread");
		backgroundThread.start();
		backgroundHandler = new Handler(backgroundThread.getLooper());

		camera = new Camera2Wrapper(context, new FileNameController(prefs));
		camera.open();
		running = true;
		handler.postDelayed(this, start);
	}

	/**
	 * Stop the background Thread
	 */
	protected synchronized void stopBackgroundThread() {
		if (backgroundThread == null) {
			return;
		}
		backgroundThread.quitSafely();
		try {
			backgroundThread.join();
		} catch (InterruptedException e) {
			Log.e(TAG, "Error joining background Thread", e);
		}

		backgroundThread = null;
		backgroundHandler = null;
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

		Log.i(TAG, "Take Image");

		if (waitForImage) {
			// TODO Error handling
			Log.e(TAG, "Still waiting for the last image!");
		}

		waitForImage = true;
		camera.takePicture(backgroundHandler, this);
		// TODO Take image
		//StatusSenderUtil.sendError(handler, "ABC", "Test crash!");
	}

	/**
	 * Stop Recoding
	 */
	public void stop() {
		running = false;
		handler.removeCallbacks(this);
		stopBackgroundThread();
	}
}
