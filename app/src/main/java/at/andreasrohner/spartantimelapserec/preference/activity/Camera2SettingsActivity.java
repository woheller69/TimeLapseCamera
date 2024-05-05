package at.andreasrohner.spartantimelapserec.preference.activity;

import android.content.SharedPreferences;

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
		protected void updateValues() {
			// Nothing to do
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
			updateValues();
		}
	}
}