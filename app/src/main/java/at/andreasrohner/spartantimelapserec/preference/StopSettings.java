package at.andreasrohner.spartantimelapserec.preference;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.Preference;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.preference.activity.StopSettingsActivity;
import at.andreasrohner.spartantimelapserec.preference.mainmenu.MainSettingsMenu;

/**
 * Stop Settings
 */
@SuppressWarnings("unused") // Loaded by menu
public class StopSettings implements MainSettingsMenu {

	/**
	 * Constructor
	 */
	public StopSettings() {
	}

	@Override
	public Class<? extends Activity> getActivityClass(SharedPreferences prefs) {
		return StopSettingsActivity.class;
	}

	@Override
	public void updateSummary(Preference pref, Context ctx, SharedPreferences prefs) {
		boolean lowStorage = prefs.getBoolean("pref_stop_low_storage", true);
		boolean lowBattery = prefs.getBoolean("pref_stop_low_battery", true);
		if (!lowStorage && !lowBattery) {
			pref.setSummary(ctx.getString(R.string.pref_stop_info_nostop));
		} else {
			String info = ctx.getString(R.string.pref_stop_info_stop_start) + ' ';

			if (lowStorage) {
				info += ctx.getString(R.string.pref_stop_info_stop_storage);
			}
			if (lowStorage && lowBattery) {
				info += ", ";
			}
			if (lowBattery) {
				info += ctx.getString(R.string.pref_stop_info_stop_battery);
			}
			pref.setSummary(info);
		}
	}
}
