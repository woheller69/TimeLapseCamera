package at.andreasrohner.spartantimelapserec.state;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import androidx.preference.PreferenceManager;
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
	 * Log Level
	 */
	private static int level = LogLevel.INFO.LEVEL;

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
	 * Load the log level
	 *
	 * @param context Context
	 */
	public static void loadLogLevel(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		level = prefs.getInt("log_display_level", LogLevel.INFO.LEVEL);
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
	 * @param entry Log Entry
	 */
	public static void addEntry(StateLogEntry entry) {
		synchronized (instance) {
			if (entry.getLevel().LEVEL > level) {
				return;
			}

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
		if (status.isErrorStop()) {
			entry.setLevel(LogLevel.ERROR);
		} else {
			entry.setLevel(LogLevel.INFO);
		}

		addEntry(entry);
	}
}
