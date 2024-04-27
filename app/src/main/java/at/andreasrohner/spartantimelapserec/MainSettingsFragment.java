package at.andreasrohner.spartantimelapserec;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import at.andreasrohner.spartantimelapserec.settings.ShowActivityPreference;
import at.andreasrohner.spartantimelapserec.updateableprefs.UpdateablePreferenceFragmentCompat;

/**
 * Main Settings menu
 */
public class MainSettingsFragment extends UpdateablePreferenceFragmentCompat {

	/**
	 * Recording mode preference
	 */
	private ListPreference prefRecMode;

	/**
	 * Constructor
	 */
	public MainSettingsFragment() {
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.main_preferences, rootKey);

		prefRecMode = (ListPreference) findPreference("pref_rec_mode");

		setRecMode(getPreferenceManager().getSharedPreferences());

		updateSummary();
	}

	@Override
	public void onDisplayPreferenceDialog(Preference preference) {
		if (preference instanceof ShowActivityPreference) {
			ShowActivityPreference p = (ShowActivityPreference) preference;
			Context ctx = getContext();
			Class<? extends Activity> activityClass = p.getActivityClass(getPreferenceManager().getSharedPreferences());
			if (activityClass == null) {
				return;
			}

			Intent myIntent = new Intent(ctx, activityClass);
			ctx.startActivity(myIntent);
		} else {
			super.onDisplayPreferenceDialog(preference);
		}
	}

	/**
	 * Set Recording Mode
	 *
	 * @param prefs Preferences
	 */
	private void setRecMode(SharedPreferences prefs) {
		CharSequence entry = prefRecMode.getEntry();
		if (entry != null) {
			prefRecMode.setSummary(entry);
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		// Update on all pref changes
		//if (key.equals("pref_rec_mode")) {
		setRecMode(prefs);
		updateSummary();
		//}
	}

	/**
	 * Update state Display
	 */
	public void updateStateDisplay() {
		updateSummary();
	}
}
