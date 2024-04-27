package at.andreasrohner.spartantimelapserec.preference;

import android.os.Bundle;

import androidx.annotation.XmlRes;
import at.andreasrohner.spartantimelapserec.preference.update.UpdateablePreferenceFragmentCompat;

/**
 * Base class for the settings fragments
 */
public abstract class AbstractSettingsFragment extends UpdateablePreferenceFragmentCompat {

	/**
	 * Preferences Resource ID
	 */
	private final int preferencesResId;

	/**
	 * Constructor
	 *
	 * @param preferencesResId Preferences Resource ID
	 */
	public AbstractSettingsFragment(@XmlRes int preferencesResId) {
		this.preferencesResId = preferencesResId;
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(preferencesResId, rootKey);
	}

	@Override
	public void onResume() {
		super.onResume();
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		updateValues();
	}

	/**
	 * Update values from preferences
	 */
	protected abstract void updateValues();

	@Override
	public void onPause() {
		getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}
}
