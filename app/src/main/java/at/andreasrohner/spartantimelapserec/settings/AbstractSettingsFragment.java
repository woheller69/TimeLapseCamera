package at.andreasrohner.spartantimelapserec.settings;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.XmlRes;
import androidx.preference.PreferenceFragmentCompat;

/**
 * Base class for the settings fragments
 */
public abstract class AbstractSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

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
