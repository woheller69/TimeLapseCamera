package at.andreasrohner.spartantimelapserec.preference;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

/**
 * Initialize some default values for preferences
 */
public class PreferencesDefaultValues {

	/**
	 * Context
	 */
	private final Context context;

	/**
	 * Constructor
	 *
	 * @param context Context
	 */
	public PreferencesDefaultValues(Context context) {
		this.context = context;
	}

	/**
	 * Set Default values if values are not already set
	 */
	public void setDefaultValues() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		// Write a default interval of 10s
		if (prefs.getInt("pref_capture_rate", -1) == -1) {
			SharedPreferences.Editor editor = prefs.edit();
			editor.putInt("pref_capture_rate", 10000);
			editor.apply();
		}
	}
}
