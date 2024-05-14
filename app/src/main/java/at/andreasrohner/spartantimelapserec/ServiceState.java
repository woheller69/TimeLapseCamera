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
	 * Stopped because of error
	 */
	private final boolean errorStop;

	/**
	 * Constructor
	 *
	 * @param state     Current state
	 * @param reason    Reason why the current state was set
	 * @param errorStop Stopped because of error
	 */
	public ServiceState(State state, String reason, boolean errorStop) {
		this.state = state;
		this.reason = reason;
		this.errorStop = errorStop;
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

	/**
	 * @return Stopped because of error
	 */
	public boolean isErrorStop() {
		return errorStop;
	}

	@NonNull
	@Override
	public String toString() {
		return getState() + " " + getReason();
	}
}
