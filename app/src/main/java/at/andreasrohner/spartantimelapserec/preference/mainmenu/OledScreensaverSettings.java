package at.andreasrohner.spartantimelapserec.preference.mainmenu;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.Preference;
import at.andreasrohner.spartantimelapserec.OledScreensaverActivity;
import at.andreasrohner.spartantimelapserec.R;

/**
 * OLED Screensaver
 */
@SuppressWarnings("unused") // Loaded by menu
public class OledScreensaverSettings implements MainSettingsMenu {

	/**
	 * Constructor
	 */
	public OledScreensaverSettings() {
	}

	@Override
	public Class<? extends Activity> getActivityClass(SharedPreferences prefs) {
		return OledScreensaverActivity.class;
	}

	@Override
	public void updateSummary(Preference pref, Context ctx, SharedPreferences prefs) {
		pref.setSummary(ctx.getString(R.string.show_oled_screensaver_summary));
	}
}
