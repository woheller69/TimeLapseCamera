package at.andreasrohner.spartantimelapserec.camera2.wrapper;

/**
 * Callback to get informed when the camera is open
 */
public interface CameraOpenCallback {

	/**
	 * Gets called when the camea was opened
	 *
	 * @param success true on succes
	 */
	void cameraOpened(boolean success);
}