package at.andreasrohner.spartantimelapserec.settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.Preference;

/**
 * Legacy scheduling settings of Camera1 interface
 */
public class LegacyScheduling1Settings implements MainSettingsMenu {

	/**
	 * Constructor
	 */
	public LegacyScheduling1Settings() {
	}

	@Override
	public Class<? extends Activity> getActivityClass() {
		return LegacyScheduling1SettingsActivity.class;
	}

	@Override
	public void updateSummary(Preference pref, Context ctx, SharedPreferences prefs) {
		// TODO !!!!!!!!!!!
	}
}
