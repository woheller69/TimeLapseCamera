package at.andreasrohner.spartantimelapserec.camera2.wrapper;

/**
 * Interface to get notified when the image is taken
 */
public interface ImageTakenListener {

	/**
	 * Combine two listener
	 *
	 * @param l1 Listener 1
	 * @param l2 Listener 2
	 * @return Combined listener
	 */
	static ImageTakenListener combine(ImageTakenListener l1, ImageTakenListener l2) {
		return new ImageTakenListener() {

			@Override
			public void takeImageFinished() {
				l1.takeImageFinished();
				l2.takeImageFinished();
			}
		};
	}

	/**
	 * The image is taken
	 */
	void takeImageFinished();
}