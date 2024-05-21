package at.andreasrohner.spartantimelapserec.camera2.wrapper;

/**
 * Interface to get notified when the image is taken
 */
public interface ImageTakenListener {

	/**
	 * The image is taken
	 */
	void takeImageFinished();
}