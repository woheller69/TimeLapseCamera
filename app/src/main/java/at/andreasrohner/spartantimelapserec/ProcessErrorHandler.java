package at.andreasrohner.spartantimelapserec;

import android.util.Log;

import java.io.IOException;

/**
 * Log / Show error in timelapse recording
 */
public class ProcessErrorHandler {

	/**
	 * Constructor
	 */
	public ProcessErrorHandler() {
	}

	public static void error(String msg, Exception e) {
		Log.e("LogicError", msg, e);
	}
}
