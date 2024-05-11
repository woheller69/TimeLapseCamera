package at.andreasrohner.spartantimelapserec.state;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import at.andreasrohner.spartantimelapserec.BaseForegroundService;
import at.andreasrohner.spartantimelapserec.ServiceState;
import at.andreasrohner.spartantimelapserec.ServiceStatusListener;

/**
 * Keep the last states, so they can viewed
 */
public class StateLog implements ServiceStatusListener {

	/**
	 * Max log entries
	 */
	public static final int MAX_LOG_LINES = 40;

	/**
	 * Single Instance
	 */
	private static StateLog instance = new StateLog();

	/**
	 * Log list
	 */
	private LinkedList<StateLogEntry> log = new LinkedList<>();

	/**
	 * @return Singleton
	 */
	public static StateLog getInstance() {
		return instance;
	}

	/**
	 * Singleton
	 */
	private StateLog() {
		BaseForegroundService.registerStatusListener(this);
	}

	/**
	 * @return A copy of the log list
	 */
	public List<StateLogEntry> getLog() {
		synchronized (this) {
			return new ArrayList<>(log);
		}
	}

	/**
	 * Add a log entry
	 *
	 * @param header Log header
	 * @param line   Log line
	 */
	public static void addEntry(String header, String line) {
		StateLogEntry entry = new StateLogEntry();
		entry.setHeader(header);
		entry.setLine(line);

		addEntry(entry);
	}

	/**
	 * Add a log entry
	 *
	 * @param entry Log Entry
	 */
	public static void addEntry(StateLogEntry entry) {
		synchronized (instance) {
			instance.log.add(entry);
			while (instance.log.size() > MAX_LOG_LINES) {
				instance.log.removeFirst();
			}
		}
	}

	@Override
	public void onServiceStatusChange(ServiceState status) {
		StateLogEntry entry = new StateLogEntry();
		entry.setHeader(String.valueOf(status.getState()));
		entry.setLine(status.getReason());

		addEntry(entry);
	}
}
