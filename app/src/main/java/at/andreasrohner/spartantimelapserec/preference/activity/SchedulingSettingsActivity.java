package at.andreasrohner.spartantimelapserec.preference.activity;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.ServiceHelper;
import at.andreasrohner.spartantimelapserec.preference.AbstractSettingsFragment;
import at.andreasrohner.spartantimelapserec.preference.preftype.DatePreference;
import at.andreasrohner.spartantimelapserec.preference.preftype.DatePreferenceDialogFragment;
import at.andreasrohner.spartantimelapserec.preference.preftype.DialogDisplayPreference;

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
			if (preference instanceof DialogDisplayPreference) {
				((DialogDisplayPreference) preference).showDialog();
			} else if (preference instanceof DatePreference) {
				final DialogFragment f;
				f = DatePreferenceDialogFragment.newInstance(preference.getKey());
				f.setTargetFragment(this, 0);
				f.show(getFragmentManager(), null);
			} else {
				super.onDisplayPreferenceDialog(preference);
			}
		}

		@Override
		protected void updateValues() {
			updateSummary();
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
			if ("pref_schedule_recording_enabled".equals(key)) {
				ServiceHelper h = new ServiceHelper(getContext());
				h.startStopIfSchedulingIsActive();
			}
			updateSummary();
		}
	}
}