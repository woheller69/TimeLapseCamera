package at.andreasrohner.spartantimelapserec.camera2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

import androidx.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.StatusSenderUtil;
import at.andreasrohner.spartantimelapserec.camera2.wrapper.ImageTakenListener;
import at.andreasrohner.spartantimelapserec.data.SchedulingSettings;
import at.andreasrohner.spartantimelapserec.state.Logger;

/**
 * Base class for image recorder
 */
public abstract class BaseRecorder implements Runnable, ImageTakenListener, ProcessErrorHandler {

	/**
	 * Logger
	 */
	protected Logger logger = new Logger(getClass());

	/**
	 * Context
	 */
	protected final Context context;

	/**
	 * Handler
	 */
	protected final Handler handler;

	/**
	 * Interval time in ms
	 */
	private int captureIntervalTime;

	/**
	 * Running State
	 */
	private boolean running = true;

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
	public BaseRecorder(Context context, Handler handler) {
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

		initRecording();

		running = true;
		handler.postDelayed(this, start);
	}

	/**
	 * Initialize Recording
	 */
	protected void initRecording() {
		// Can be overwritten by extending classes
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
			missedImages++;
			logger.warn("Still waiting for the last image! missed count: {}", missedImages);
			if (missedImages >= 3) {
				error("Could not create the last 3 images! Probably the interval is to short, choose a longer one.", null);
			}
		} else {
			missedImages = 0;
		}

		waitForImage = true;

		takePicture();
	}

	/**
	 * Take a picture
	 */
	protected abstract void takePicture();

	/**
	 * Stop Recoding
	 */
	public void stop() {
		running = false;
		handler.removeCallbacks(this);
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
