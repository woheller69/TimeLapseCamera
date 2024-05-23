package at.andreasrohner.spartantimelapserec.camera2.wrapper;

/**
 * State handler
 */
public enum CameraAfState {

	/**
	 * Camera state: Showing camera preview.
	 */
	STATE_PREVIEW,

	/**
	 * Camera state: Waiting for the focus to be locked.
	 */
	STATE_WAITING_LOCK,

	/**
	 * Camera state: Waiting for the exposure to be precapture state.
	 */
	STATE_WAITING_PRECAPTURE,

	/**
	 * Camera state: Waiting for the exposure state to be something other than precapture.
	 */
	STATE_WAITING_NON_PRECAPTURE,

	/**
	 * Camera state: Picture was taken.
	 */
	STATE_PICTURE_TAKEN,

	/**
	 * Take image ended
	 */
	STATE_ENDED
}