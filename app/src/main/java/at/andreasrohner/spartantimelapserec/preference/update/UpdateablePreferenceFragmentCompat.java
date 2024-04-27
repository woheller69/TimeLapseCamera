package at.andreasrohner.spartantimelapserec.preference.update;

import android.content.SharedPreferences;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

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
	@SuppressWarnings("ConstantConditions")
	public void onResume() {
		super.onResume();
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

		updateSummary();
	}

	@Override
	@SuppressWarnings("ConstantConditions")
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

