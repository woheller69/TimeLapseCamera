package at.andreasrohner.spartantimelapserec.camera2;

import android.content.Context;
import android.os.Handler;

import at.andreasrohner.spartantimelapserec.StatusSenderUtil;
import at.andreasrohner.spartantimelapserec.data.SchedulingSettings;

/**
 * Handle Recording
 */
public class Camera2Recorder {

	/**
	 * Context
	 */
	private final Context context;

	/**
	 * Handler
	 */
	private final Handler handler;

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

		handler.postDelayed(() -> timerCallback(), start);
	}

	/**
	 * Timer callback, called in handler thread
	 */
	public void timerCallback() {
//		handler.postDelayed(() -> timerCallback(), start);

		StatusSenderUtil.sendError(handler, "ABC", "Test crash!");
	}

	/**
	 * Stop Recoding
	 */
	public void stop() {
	}
}
