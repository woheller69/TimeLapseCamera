package at.andreasrohner.spartantimelapserec.settings;

import android.content.Context;

import java.text.DecimalFormat;

import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.preference.SeekBarPreference;

/**
 * Base methods for the legacy settings
 */
public class BaseLegacySettingsCommon implements SeekBarPreference.OnFormatOutputValueListener {

	protected Context context;

	/**
	 * Constructor
	 */
	public BaseLegacySettingsCommon() {
	}

	protected String formatTime(int millis) {
		if (millis < 1000)
			return millis + " " + context.getString(R.string.time_format_msec);

		double secs = ((double) (millis % 60000)) / 1000;
		String formatSec = " " + context.getString(R.string.time_format_sec);
		String formatSecs = " " + context.getString(R.string.time_format_secs);
		DecimalFormat df = new DecimalFormat("#.##");

		if (millis >= 1000 && millis < 60000)
			return df.format(secs) + ((secs == 1) ? formatSec : formatSecs);

		int intSecs = millis % 60000 / 1000;
		int mins = (millis % 3600000) / 1000 / 60;
		int hours = (millis / 1000 / 60 / 60);

		String formatMin = " " + context.getString(R.string.time_format_min);
		String formatMins = " " + context.getString(R.string.time_format_mins);
		String formatHour = " " + context.getString(R.string.time_format_hour);
		String formatHours = " " + context.getString(R.string.time_format_hours);
		String res = "";
		if (hours == 1)
			res += hours + formatHour;
		else if (hours > 0)
			res += hours + formatHours;

		if (mins == 1)
			res += " " + mins + formatMin;
		else if (mins > 0)
			res += " " + mins + formatMins;

		if (intSecs == 1)
			res += " " + intSecs + formatSec;
		else if (intSecs > 0)
			res += " " + intSecs + formatSecs;

		return res;
	}

	@Override
	public String onFormatOutputValue(int value, String suffix) {
		if ("ms".equals(suffix))
			return formatTime(value);
		else if ("min".equals(suffix)) {
			if (value >= 47 * 60)
				return context.getString(R.string.pref_infinite);
			return formatTime(value * 1000 * 60);
		}
		return null;
	}
}
