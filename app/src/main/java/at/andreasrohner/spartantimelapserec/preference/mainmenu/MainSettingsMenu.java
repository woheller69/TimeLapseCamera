package at.andreasrohner.spartantimelapserec.preference.mainmenu;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.Preference;

/**
 * Interface for a main settings menu, get the activity and create the summary text
 */
public interface MainSettingsMenu {

	/**
	 * Get the activity
	 *
	 * @param prefs Preferences
	 * @return The activity class of this settings
	 */
	Class<? extends Activity> getActivityClass(SharedPreferences prefs);

	/**
	 * Create summary from current values
	 *
	 * @param pref  Preference to update
	 * @param ctx   Context
	 * @param prefs Preferences
	 */
	void updateSummary(Preference pref, Context ctx, SharedPreferences prefs);
}
