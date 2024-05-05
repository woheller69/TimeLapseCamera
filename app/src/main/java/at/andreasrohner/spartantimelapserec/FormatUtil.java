package at.andreasrohner.spartantimelapserec;

import android.content.Context;

import java.text.DecimalFormat;
import java.util.Locale;

/**
 * Utility class for formatting
 */
public final class FormatUtil {

	/**
	 * Utility class
	 */
	private FormatUtil() {
	}

	/**
	 * Format int 1234 as Time 12:34
	 *
	 * @param time Int
	 * @return Formatted String
	 */
	public static String formatTimeFromInt(int time) {
		return String.format(Locale.getDefault(), "%02d:%02d", time / 100, time % 100);
	}

	/**
	 * Format time
	 *
	 * @param min     Minutes
	 * @param context Context
	 * @return Formatted Time
	 */
	public static String formatTimeMin(int min, Context context) {
		return formatTime(1000L * min * 60, context);
	}

	/**
	 * Format time
	 *
	 * @param millis  Millis
	 * @param context Context
	 * @return Formatted Time
	 */
	public static String formatTime(long millis, Context context) {
		if (millis < 1000) {
			return millis + " " + context.getString(R.string.time_format_msec);
		}

		double secs = ((double) (millis % 60000)) / 1000;
		String formatSec = " " + context.getString(R.string.time_format_sec);
		String formatSecs = " " + context.getString(R.string.time_format_secs);
		DecimalFormat df = new DecimalFormat("#.##");

		if (millis < 60000) {
			return df.format(secs) + ((secs == 1) ? formatSec : formatSecs);
		}

		long intSecs = millis % 60000 / 1000;
		long mins = (millis % 3600000) / 1000 / 60;
		long hours = (millis / 1000 / 60 / 60);

		String formatMin = " " + context.getString(R.string.time_format_min);
		String formatMins = " " + context.getString(R.string.time_format_mins);
		String formatHour = " " + context.getString(R.string.time_format_hour);
		String formatHours = " " + context.getString(R.string.time_format_hours);
		String res = "";
		if (hours == 1) {
			res += hours + formatHour;
		} else if (hours > 0) {
			res += hours + formatHours;
		}

		if (mins == 1) {
			res += " " + mins + formatMin;
		} else if (mins > 0) {
			res += " " + mins + formatMins;
		}

		if (intSecs == 1) {
			res += " " + intSecs + formatSec;
		} else if (intSecs > 0) {
			res += " " + intSecs + formatSecs;
		}

		return res;
	}
}
