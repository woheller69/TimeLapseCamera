package at.andreasrohner.spartantimelapserec.updateableprefs;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import at.andreasrohner.spartantimelapserec.settings.ShowActivityPreference;

/**
 * Base class for Preferences with update capabilities
 */
public abstract class UpdateablePreferenceFragmentCompat extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

	/**
	 * Constructor
	 */
	public UpdateablePreferenceFragmentCompat() {
	}

	@Override
	public void onResume() {
		super.onResume();
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

		updateSummary();
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
			if (!(p instanceof SummaryPreference)) {
				continue;
			}

			((SummaryPreference) p).updateSummary();
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		// Update on all pref changes
		updateSummary();
	}
}

