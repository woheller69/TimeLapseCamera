package at.andreasrohner.spartantimelapserec.camera2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.StatusSenderUtil;
import at.andreasrohner.spartantimelapserec.camera2.filename.AbstractFileNameController;
import at.andreasrohner.spartantimelapserec.camera2.wrapper.Camera2Wrapper;
import at.andreasrohner.spartantimelapserec.camera2.wrapper.ImageTakenListener;
import at.andreasrohner.spartantimelapserec.data.SchedulingSettings;
import at.andreasrohner.spartantimelapserec.state.Logger;

/**
 * Handle Recording
 */
public class Camera2Recorder implements Runnable, ImageTakenListener, ProcessErrorHandler {

	/**
	 * Logger
	 */
	private Logger logger = new Logger(getClass());

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
	 * Count of missed images
	 */
	private int missedImages = 0;

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

		camera = new Camera2Wrapper(context, AbstractFileNameController.createInstance(context), this);
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
		} catch (Exception e) {
			logger.error("Error joining background Thread", e);
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
			logger.info("Stop now because of running flag");
			return;
		}
		// Schedule next image
		handler.postDelayed(this, captureIntervalTime);

		logger.debug("Scheduled Image in {}ms", captureIntervalTime);

		if (waitForImage) {
			logger.warn("Still waiting for the last image! missed count: {}", missedImages);
			missedImages++;
			if (missedImages >= 3) {
				error("Could not create the last 3 images!", null);
			}
		} else {
			missedImages = 0;
		}

		waitForImage = true;
		camera.takePicture(backgroundHandler, this);
	}

	/**
	 * Stop Recoding
	 */
	public void stop() {
		running = false;
		handler.removeCallbacks(this);
		stopBackgroundThread();
	}

	@Override
	public void error(String msg, Exception e) {
		logger.error(msg, e);
		StringBuilder b = new StringBuilder();
		b.append(msg);
		if (e != null) {
			b.append('\n');
			b.append(e.getClass().getSimpleName());
			b.append(' ');
			if (e.getMessage() != null) {
				b.append(e.getMessage());
			}
		}
		StatusSenderUtil.sendError(handler, b.toString());
	}
}
