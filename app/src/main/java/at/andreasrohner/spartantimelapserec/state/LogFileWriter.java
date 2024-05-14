package at.andreasrohner.spartantimelapserec.state;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import androidx.preference.PreferenceManager;

import static android.os.Environment.DIRECTORY_PICTURES;

/**
 * Class to write the logfile
 */
public class LogFileWriter {

	/**
	 * Log Level
	 */
	private static int level;

	/**
	 * Logging enabled?
	 */
	private static boolean enabled = false;

	/**
	 * Date format for Files
	 */
	private static final SimpleDateFormat FILE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * Simple Date format
	 */
	private static DateFormat TIME_FORMAT = SimpleDateFormat.getTimeInstance(SimpleDateFormat.MEDIUM);

	/**
	 * Utility class
	 */
	private LogFileWriter() {
	}

	/**
	 * Load the log level / enabled flag
	 *
	 * @param context Context
	 */
	public static void loadLogConfig(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		level = prefs.getInt("log_file_level", LogLevel.INFO.LEVEL);
		setEnabled(prefs.getBoolean("log_file_enabled", false));
	}

	/**
	 * @param level Log Level
	 */
	public static void setLevel(int level) {
		LogFileWriter.level = level;
	}

	/**
	 * @param enabled Logging enabled?
	 */
	public static synchronized void setEnabled(boolean enabled) {
		LogFileWriter.enabled = enabled;
	}

	/**
	 * Log an entry
	 *
	 * @param logLevel Log level
	 * @param logEntry Log Entry
	 * @param name     Class Name
	 */
	public static synchronized void log(LogLevel logLevel, Logger.LogEntry logEntry, String name) {
		if (!enabled) {
			return;
		}

		if (logLevel.LEVEL > level) {
			return;
		}

		String dateNow = FILE_DATE_FORMAT.format(System.currentTimeMillis());

		String projectPath = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES).getPath();
		File folder = new File(projectPath, "TimeLapseCam-Logs");
		File outputFile = new File(folder, dateNow + ".log");
		if (!folder.exists()) {
			// If it failes, it will catch the exception later, cannot not do anything...
			// Even if the folder is a file or so...
			folder.mkdirs();
		}

		StringBuilder b = new StringBuilder(128);

		b.append(TIME_FORMAT.format(System.currentTimeMillis()));
		b.append("\t[");
		b.append(Thread.currentThread().getName());
		b.append("]\t");
		b.append(logLevel.MARKER);
		b.append("\t");
		b.append(logEntry.getMessage());
		b.append("\t");
		b.append(name);
		b.append('\n');

		Throwable exception = logEntry.getException();
		if (exception != null) {
			String output;
			try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); PrintStream printStream = new PrintStream(outputStream)) {
				exception.printStackTrace(printStream);
				b.append(outputStream.toString());
			} catch (IOException e) {
				Log.e("Log", "Could not prepare exception for log");
				b.append("ERROR: Could not prepare StackTrace!\n");
				b.append(e);
			}
		}

		try (FileOutputStream out = new FileOutputStream(outputFile, true)) {
			out.write(b.toString().getBytes(StandardCharsets.UTF_8));
		} catch (Exception ex) {
			Log.e("Log", "Could not create Logfile!", ex);
			StateLogEntry entry = new StateLogEntry();
			entry.setLevel(LogLevel.ERROR);
			entry.setHeader("Fatal Log Error");
			entry.setLine("Could not write Logfile «" + outputFile + "» " + String.valueOf(ex));
			StateLog.addEntry(entry);
			// Do not log with Logger, this will create an endless loop!
			return;
		}
	}
}
