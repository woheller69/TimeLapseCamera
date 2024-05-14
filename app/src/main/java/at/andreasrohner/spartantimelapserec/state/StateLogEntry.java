package at.andreasrohner.spartantimelapserec.state;

/**
 * Entry in the log table
 */
public class StateLogEntry {

	/**
	 * Log timestamp
	 */
	private final long timestamp = System.currentTimeMillis();

	/**
	 * Header
	 */
	private String header;

	/**
	 * Log Line
	 */
	private String line;

	/**
	 * Log level
	 */
	private LogLevel level;

	/**
	 * Constructor
	 */
	public StateLogEntry() {
	}

	/**
	 * @return Log timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * @param header Header
	 */
	public void setHeader(String header) {
		this.header = header;
	}

	/**
	 * @return Header
	 */
	public String getHeader() {
		return header;
	}

	/**
	 * @param line Log Line
	 */
	public void setLine(String line) {
		this.line = line;
	}

	/**
	 * @return Log Line
	 */
	public String getLine() {
		return line;
	}

	/**
	 * @param level Log level
	 */
	public void setLevel(LogLevel level) {
		this.level = level;
	}

	/**
	 * @return Log level
	 */
	public LogLevel getLevel() {
		return level;
	}
}
