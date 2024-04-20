package at.andreasrohner.spartantimelapserec.settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.Preference;

/**
 * Legacy settings of Camera1 interface
 */
public class LegacyCamera1Settings implements MainSettingsMenu {

	/**
	 * Constructor
	 */
	public LegacyCamera1Settings() {
	}

	@Override
	public Class<? extends Activity> getActivityClass() {
		return LegacyCamera1SettingsActivity.class;
	}

	@Override
	public void updateSummary(Preference pref, Context ctx, SharedPreferences prefs) {
		// TODO !!!!!!!!!!!
	}
}
