package at.andreasrohner.spartantimelapserec.state;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Log messages to android and optional to a file
 */
public class Logger {

	/**
	 * Class name
	 */
	private String name;

	/**
	 * Constructor
	 *
	 * @param cls Logging Class
	 */
	public Logger(Class<?> cls) {
		name = cls.getSimpleName();
	}

	/**
	 * Prepare a log entry
	 *
	 * @param message Message, with optional Placeholder {}
	 * @param param   Placeholder Values, Exception
	 * @return Prepared Line / Exception
	 */
	private LogEntry prepare(String message, Object[] param) {
		LogEntry e = new LogEntry();

		LinkedList<Object> stack = new LinkedList<>(Arrays.asList(param));

		StringBuilder b = new StringBuilder();
		for (String p : splitByPlaceholder(message)) {
			if ("{}".equals(p)) {
				b.append(stack.removeFirst());
			} else {
				b.append(p);
			}
		}

		e.message = b.toString();
		if (!stack.isEmpty()) {
			Object ex = stack.getFirst();
			if (ex instanceof Throwable) {
				e.exception = (Throwable) ex;
				stack.removeFirst();
			}
		}

		if (!stack.isEmpty()) {
			error("Error Message with wrong parameter count: «{}», {}", message, Arrays.toString(param));
		}

		return e;
	}

	/**
	 * Split a message by Placeholder
	 *
	 * @param message Message
	 * @return Splitted parts
	 */
	public static List<String> splitByPlaceholder(String message) {
		int pos = message.indexOf("{}");
		if (pos == -1) {
			return Arrays.asList(message);
		}

		List<String> parts = new ArrayList<>();
		int last = 0;
		for (; pos != -1; pos = message.indexOf("{}", pos + 1)) {
			String p = message.substring(last, pos);
			if (!p.isEmpty()) {
				parts.add(p);
			}
			parts.add(message.substring(pos, pos + 2));
			last = pos + 2;
		}

		String p = message.substring(last);
		if (!p.isEmpty()) {
			parts.add(p);
		}

		return parts;
	}

	/**
	 * Log a debug message
	 *
	 * @param message Message, with optional Placeholder {}
	 * @param param   Placeholder Values, Exception
	 */
	public void debug(String message, Object... param) {
		LogEntry e = prepare(message, param);
		if (e.exception != null) {
			Log.d(name, e.message, e.exception);
		} else {
			Log.d(name, e.message);
		}

		log(e, LogLevel.DEBUG);
	}

	/**
	 * Log an info message
	 *
	 * @param message Message, with optional Placeholder {}
	 * @param param   Placeholder Values, Exception
	 */
	public void info(String message, Object... param) {
		LogEntry e = prepare(message, param);
		if (e.exception != null) {
			Log.i(name, e.message, e.exception);
		} else {
			Log.i(name, e.message);
		}

		log(e, LogLevel.INFO);
	}

	/**
	 * Log a special info marker, like Application start etc.
	 *
	 * @param message Message, with optional Placeholder {}
	 * @param param   Placeholder Values, Exception
	 */
	public void mark(String message, Object... param) {
		LogEntry e = prepare(message, param);
		if (e.exception != null) {
			Log.i(name, ">> " + e.message, e.exception);
		} else {
			Log.i(name, ">> " + e.message);
		}

		log(e, LogLevel.MARK);
	}

	/**
	 * Log a warning message
	 *
	 * @param message Message, with optional Placeholder {}
	 * @param param   Placeholder Values, Exception
	 */
	public void warn(String message, Object... param) {
		LogEntry e = prepare(message, param);
		if (e.exception != null) {
			Log.w(name, e.message, e.exception);
		} else {
			Log.w(name, e.message);
		}

		log(e, LogLevel.WARN);
	}

	/**
	 * Log an error message
	 *
	 * @param message Message, with optional Placeholder {}
	 * @param param   Placeholder Values, Exception
	 */
	public void error(String message, Object... param) {
		LogEntry e = prepare(message, param);
		if (e.exception != null) {
			Log.e(name, e.message, e.exception);
		} else {
			Log.e(name, e.message);
		}

		log(e, LogLevel.ERROR);
	}

	/**
	 * Log the message internal
	 *
	 * @param e        Log entry
	 * @param logLevel Log level
	 */
	private void log(LogEntry e, LogLevel logLevel) {
		StateLogEntry sl = new StateLogEntry();
		sl.setHeader(name);
		sl.setLine(e.message);
		sl.setLevel(logLevel);
		StateLog.addEntry(sl);

		LogFileWriter.log(logLevel, e, name);
	}

	/**
	 * Log entry with message and log line
	 */
	public static class LogEntry {

		/**
		 * Message
		 */
		private String message;

		/**
		 * Throwable
		 */
		private Throwable exception;

		/**
		 * Constructor
		 */
		public LogEntry() {
		}

		/**
		 * @return Message
		 */
		public String getMessage() {
			return message;
		}

		/**
		 * @return Throwable
		 */
		public Throwable getException() {
			return exception;
		}
	}
}
