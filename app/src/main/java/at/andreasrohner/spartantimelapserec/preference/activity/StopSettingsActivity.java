package at.andreasrohner.spartantimelapserec.preference.activity;

import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.preference.AbstractSettingsFragment;

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
			// Nothing to do
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
			// Nothing to do here
		}
	}
}