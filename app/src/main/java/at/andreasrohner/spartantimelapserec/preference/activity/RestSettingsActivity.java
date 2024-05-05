package at.andreasrohner.spartantimelapserec.preference.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.preference.AbstractSettingsFragment;
import at.andreasrohner.spartantimelapserec.rest.RestControlUtil;

/**
 * REST Settings
 */
public class RestSettingsActivity extends AbstractSettingsActivity {

	/**
	 * Constructor
	 */
	public RestSettingsActivity() {
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
			super(R.xml.rest_preferences);
		}

		@Override
		protected void updateValues() {
			updatePort();
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
			if ("pref_restapi_port".equals(key)) {
				EditTextPreference pref = (EditTextPreference) findPreference("pref_restapi_port");
				validatePort(pref);
				updatePort();
			}

			RestControlUtil.startStopRestApiServer(getContext());
		}

		/**
		 * Validate the port, if the port is not valid, set it to the default port
		 *
		 * @param pref Port Preference
		 */
		@SuppressWarnings("ConstantConditions")
		private void validatePort(EditTextPreference pref) {
			Context ctx = getContext();

			int port;
			try {
				port = Integer.parseInt(pref.getText());
			} catch (Exception e) {
				// Port is not valid
				Toast.makeText(ctx, ctx.getString(R.string.error_port_invalid), Toast.LENGTH_SHORT).show();
				pref.setText("8085");
				return;
			}

			if (port < 1024 || port > 65535) {
				Toast.makeText(ctx, ctx.getString(R.string.error_port_invalid), Toast.LENGTH_SHORT).show();
				pref.setText("8085");
			}
		}

		/**
		 * Show the current port value as summary
		 */
		private void updatePort() {
			EditTextPreference pref = (EditTextPreference) findPreference("pref_restapi_port");
			pref.setSummary(pref.getText());
		}
	}
}