package at.andreasrohner.spartantimelapserec.settings;

import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import at.andreasrohner.spartantimelapserec.R;

/**
 * Stop Settings
 */
public class StopSettingsActivity extends AbstractSettingsActivity {

	/**
	 * Constructor
	 */
	public StopSettingsActivity() {
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
			super(R.xml.stop_preferences);
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