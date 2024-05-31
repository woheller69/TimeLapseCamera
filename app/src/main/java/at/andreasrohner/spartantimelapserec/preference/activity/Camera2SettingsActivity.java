package at.andreasrohner.spartantimelapserec.preference.activity;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.preference.AbstractSettingsFragment;
import at.andreasrohner.spartantimelapserec.preference.preftype.DialogDisplayPreference;
import at.andreasrohner.spartantimelapserec.preference.preftype.ShowCameraInfoPreference;

/**
 * Camera 2 Settings
 */
public class Camera2SettingsActivity extends AbstractSettingsActivity {

	/**
	 * Constructor
	 */
	public Camera2SettingsActivity() {
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
			super(R.xml.camera2_preferences);
		}

		@Override
		public void onDisplayPreferenceDialog(@NonNull Preference preference) {
			if (preference instanceof DialogDisplayPreference) {
				((DialogDisplayPreference) preference).showDialog();
				return;
			}
			if (preference instanceof ShowCameraInfoPreference) {
				return;
			}

			super.onDisplayPreferenceDialog(preference);
		}

		@Override
		public void onCreate(@Nullable Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			updateIntervalWarning();
		}

		@Override
		protected void updateValues() {
			// Nothing to do
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
			updateValues();

			if ("pref_capture_rate".equals(key)) {
				updateIntervalWarning();
			}
		}

		/**
		 * Show interval warning, if time is short
		 */
		private void updateIntervalWarning() {
			SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
			int rateMs = prefs.getInt("pref_capture_rate", 10000);
			findPreference("pref_capture_rate_warning").setVisible(rateMs < 10000);
		}
	}
}