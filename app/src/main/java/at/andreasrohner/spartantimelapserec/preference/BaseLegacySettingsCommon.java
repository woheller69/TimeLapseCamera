package at.andreasrohner.spartantimelapserec.preference;

import android.content.Context;

import at.andreasrohner.spartantimelapserec.FormatUtil;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.legacypreference.SeekBarPreference;

/**
 * Base methods for the legacy settings
 */
public class BaseLegacySettingsCommon implements SeekBarPreference.OnFormatOutputValueListener {

	/**
	 * Context
	 */
	protected Context context;

	/**
	 * Constructor
	 */
	public BaseLegacySettingsCommon() {
	}

	@Override
	public String onFormatOutputValue(int value, String suffix) {
		if ("ms".equals(suffix)) {
			return FormatUtil.formatTime(value, context.getApplicationContext());
		} else if ("min".equals(suffix)) {
			if (value >= 47 * 60) {
				return context.getString(R.string.pref_infinite);
			}
			return FormatUtil.formatTimeMin(value, context.getApplicationContext());
		}
		return null;
	}
}
