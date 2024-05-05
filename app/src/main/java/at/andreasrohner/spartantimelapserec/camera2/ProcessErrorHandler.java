package at.andreasrohner.spartantimelapserec.camera2;

/**
 * Log / Show error in timelapse recording
 */
public interface ProcessErrorHandler {

	/**
	 * Log error
	 *
	 * @param msg Message
	 * @param e   Exception
	 */
	void error(String msg, Exception e);
}
