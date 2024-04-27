package at.andreasrohner.spartantimelapserec;

/**
 * Listener if the state changed
 */
public interface ServiceStatusListener {

	/**
	 * The state has been changed
	 *
	 * @param status New state
	 */
	void onServiceStatusChange(ServiceState status);
}