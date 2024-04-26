package at.andreasrohner.spartantimelapserec.settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.Preference;
import at.andreasrohner.spartantimelapserec.OledScreensaverActivity;
import at.andreasrohner.spartantimelapserec.R;

/**
 * Show status
 */
public class StatusScreen implements MainSettingsMenu {

	/**
	 * Constructor
	 */
	public StatusScreen() {
	}

	@Override
	public Class<? extends Activity> getActivityClass(SharedPreferences prefs) {
		return null;
	}

	@Override
	public void updateSummary(Preference pref, Context ctx, SharedPreferences prefs) {
		pref.setSummary("TODO: Status");
	}
}
