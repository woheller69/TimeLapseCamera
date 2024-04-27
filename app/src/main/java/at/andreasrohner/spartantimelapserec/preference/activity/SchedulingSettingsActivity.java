package at.andreasrohner.spartantimelapserec.preference.activity;

import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.preference.AbstractSettingsFragment;

/**
 * Scheduling Settings
 */
public class SchedulingSettingsActivity extends AbstractSettingsActivity {

	/**
	 * Constructor
	 */
	public SchedulingSettingsActivity() {
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
			super(R.xml.scheduling_preferences);
		}

		@Override
		protected void updateValues() {
			//updatePort();
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
			/*
			TODO !!!!!!!!!!
			if ("pref_restapi_port".equals(key)) {
				EditTextPreference pref = (EditTextPreference) findPreference("pref_restapi_port");
			}
			*/
		}
	}
}