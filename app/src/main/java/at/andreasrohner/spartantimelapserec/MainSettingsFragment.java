package at.andreasrohner.spartantimelapserec;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import at.andreasrohner.spartantimelapserec.data.RecMode;
import at.andreasrohner.spartantimelapserec.data.RecSettings;
import at.andreasrohner.spartantimelapserec.settings.ShowActivityPreference;

/**
 * Main Settings menu
 */
public class MainSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

	/**
	 * Recording mode preference
	 */
	private ListPreference prefRecMode;

	/**
	 * Main scheduling entry
	 */
	private Preference prefMainMenuScheduling;

	/**
	 * Constructor
	 */
	public MainSettingsFragment() {
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.main_preferences, rootKey);

		prefRecMode = (ListPreference) findPreference("pref_rec_mode");
		prefMainMenuScheduling = findPreference("pref_main_menu_scheduling");

		setRecMode(getPreferenceManager().getSharedPreferences());

		updateSummary();
	}

	@Override
	public void onResume() {
		super.onResume();
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

		updateSummary();
		updatePresMode(getPreferenceManager().getSharedPreferences());
	}

	@Override
	public void onPause() {
		getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}

	/**
	 * Update summary texts
	 */
	public void updateSummary() {
		PreferenceScreen prefScreen = getPreferenceScreen();
		int prefCount = prefScreen.getPreferenceCount();
		for (int i = 0; i < prefCount; i++) {
			Preference pref = prefScreen.getPreference(i);

			if (pref instanceof PreferenceCategory) {
				updateSummary((PreferenceCategory) pref);
			}
		}
	}

	/**
	 * Update summary of one preference category
	 *
	 * @param pref PreferenceCategory
	 */
	private void updateSummary(PreferenceCategory pref) {
		int prefCount = pref.getPreferenceCount();
		for (int i = 0; i < prefCount; i++) {
			Preference p = pref.getPreference(i);
			if (!(p instanceof ShowActivityPreference)) {
				continue;
			}

			((ShowActivityPreference) p).updateSummary();
		}
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

	private void setRecMode(SharedPreferences prefs) {
		CharSequence entry = prefRecMode.getEntry();
		if (entry != null) {
			prefRecMode.setSummary(entry);
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		if (key.equals("pref_rec_mode")) {
			setRecMode(prefs);
			updateSummary();
			updatePresMode(prefs);
		}
	}

	/**
	 * Update preferences according to the Mode
	 *
	 * @param prefs Preferences
	 */
	private void updatePresMode(SharedPreferences prefs) {
		// Disable Scheduling Menu in Camera 2 Mode, not yet implemented!
		prefMainMenuScheduling.setVisible(RecSettings.getRecMode(prefs) != RecMode.CAMERA2_TIME_LAPSE);
	}
}
