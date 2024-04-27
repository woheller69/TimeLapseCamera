package at.andreasrohner.spartantimelapserec.camera2;

import android.os.Handler;

import at.andreasrohner.spartantimelapserec.StatusSenderUtil;

/**
 * Handle Recording
 */
public class Camera2Recorder {

	/**
	 * Handler
	 */
	private final Handler handler;

	/**
	 * Constructor
	 *
	 * @param handler Handler
	 */
	public Camera2Recorder(Handler handler) {
		this.handler = handler;
	}

	/**
	 * Start Recording
	 */
	public void start() {
		StatusSenderUtil.sendError(handler, "ABC", "Test crash!");
	}

	/**
	 * Stop Recoding
	 */
	public void stop() {
	}
}
