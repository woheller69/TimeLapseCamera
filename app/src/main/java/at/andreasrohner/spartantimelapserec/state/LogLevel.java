package at.andreasrohner.spartantimelapserec.state;

import at.andreasrohner.spartantimelapserec.camera2.pupcfg.IdData;

/**
 * Log level
 */
public enum LogLevel implements IdData {

	/**
	 * Debug logs
	 */
	DEBUG(4, 'D'),

	/**
	 * Info message
	 */
	INFO(3, 'I'),

	/**
	 * Warning messages
	 */
	WARN(2, '"'),
	/**
	 * Error messages
	 */
	ERROR(1, 'E'),

	/**
	 * Special Info Marker
	 */
	MARK(0, '*');

	/**
	 * Numerical level
	 */
	public final int LEVEL;

	/**
	 * Numerical level
	 */
	public final char MARKER;

	/**
	 * Constructor
	 *
	 * @param level  Level
	 * @param marker Log marker
	 */
	LogLevel(int level, char marker) {
		this.LEVEL = level;
		this.MARKER = marker;
	}

	@Override
	public String getId() {
		return String.valueOf(LEVEL);
	}
}
