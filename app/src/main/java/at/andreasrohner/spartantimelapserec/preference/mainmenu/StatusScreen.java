package at.andreasrohner.spartantimelapserec.preference.mainmenu;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.Preference;
import at.andreasrohner.spartantimelapserec.BaseForegroundService;
import at.andreasrohner.spartantimelapserec.ServiceState;

/**
 * Show status
 */
@SuppressWarnings("unused") // Loaded by menu
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
		ServiceState status = BaseForegroundService.getStatus();
		pref.setSummary(status.toString());
	}
}
