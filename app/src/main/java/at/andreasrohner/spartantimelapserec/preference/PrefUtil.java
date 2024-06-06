package at.andreasrohner.spartantimelapserec.preference;

import android.content.SharedPreferences;

/**
 * Preferences Util
 */
public final class PrefUtil {

	/**
	 * AF Mode
	 */
	public enum AfMode {
		/**
		 * Auto focus, no special focus handling, just use the camera default settings
		 */
		AUTO,

		/**
		 * Use a special AF Field
		 */
		FIELD
	}

	/**
	 * Utility class
	 */
	private PrefUtil() {
	}

	/**
	 * Get AF Mode
	 *
	 * @param prefs Preferences
	 * @return AF Mode
	 */
	public static AfMode getAfMode(SharedPreferences prefs) {
		String afMode = prefs.getString("pref_camera_af_mode", "auto");

		if ("field".equals(afMode)) {
			return AfMode.FIELD;
		}

		return AfMode.AUTO;
	}
}
