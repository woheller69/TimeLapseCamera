package at.andreasrohner.spartantimelapserec.preference.activity;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.preference.AbstractSettingsFragment;
import at.andreasrohner.spartantimelapserec.preference.preftype.TimeSpanPreference;

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
		public void onDisplayPreferenceDialog(@NonNull Preference preference) {
			if (preference instanceof TimeSpanPreference) {
				((TimeSpanPreference) preference).showDialog();
				return;
			}
			super.onDisplayPreferenceDialog(preference);
		}

		@Override
		protected void updateValues() {
			updateSummary();
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
			updateSummary();
		}
	}
}