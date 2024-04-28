package at.andreasrohner.spartantimelapserec;

import androidx.annotation.NonNull;

/**
 * Hold the state and the reason why the service is in the current state
 */
public class ServiceState {

	/**
	 * State Enum
	 */
	public enum State {

		/**
		 * Initialized, but not yet started
		 */
		INIT,

		/**
		 * Scheduled start
		 */
		SCHEDULED,

		/**
		 * Currently running / recording
		 */
		RUNNING,

		/**
		 * Recording stopped
		 */
		STOPPED
	}

	/**
	 * Current state
	 */
	private final State state;

	/**
	 * Reason why the current state was set
	 */
	private final String reason;

	/**
	 * Constructor
	 *
	 * @param state  Current state
	 * @param reason Reason why the current state was set
	 */
	public ServiceState(State state, String reason) {
		this.state = state;
		this.reason = reason;
	}

	/**
	 * @return Current state
	 */
	public State getState() {
		return state;
	}

	/**
	 * @return Reason why the current state was set
	 */
	public String getReason() {
		return reason;
	}

	@NonNull
	@Override
	public String toString() {
		return getState() + " " + getReason();
	}
}
