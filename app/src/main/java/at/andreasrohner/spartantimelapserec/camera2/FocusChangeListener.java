package at.andreasrohner.spartantimelapserec.camera2;

/**
 * Listener for focus changes
 */
public interface FocusChangeListener {

	public enum FocusState {
		/**
		 * Currently focusing / not yet finished
		 */
		FOCUSSING,

		/**
		 * Focus was success
		 */
		FOCUS_SUCCESS,

		/**
		 * Focus failed
		 */
		FOCUS_FAILED
	}

	/**
	 * Focus has been changed
	 *
	 * @param state FocusState
	 */
	void focusChanged(FocusState state);
}