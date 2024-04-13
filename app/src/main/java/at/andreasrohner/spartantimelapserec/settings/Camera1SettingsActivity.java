package at.andreasrohner.spartantimelapserec.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import at.andreasrohner.spartantimelapserec.R;

/**
 * REST Settings
 */
public class Camera1SettingsActivity extends AbstractSettingsActivity {

	/**
	 * Constructor
	 */
	public Camera1SettingsActivity() {
		super(new SettingsFragment());
	}

	/**
	 * Settings fragment
	 */
	public static class SettingsFragment extends AbstractSettingsFragment {

		/**
		 * Constructor
		 */
		public SettingsFragment() {
			super(R.xml.camera1_preferences);
		}

		@Override
		protected void updateValues() {
			// TODO !!!!!!!!!!!!!!!
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
			if ("pref_restapi_port".equals(key)) {
				// TODO !!!!!!!!!!!!!!!
			}
		}
	}
}