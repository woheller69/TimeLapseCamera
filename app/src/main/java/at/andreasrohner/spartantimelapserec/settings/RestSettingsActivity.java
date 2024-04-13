package at.andreasrohner.spartantimelapserec.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.preference.PreferenceManager;

import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.rest.RestService;

/**
 * REST Settings
 */
public class RestSettingsActivity extends AppCompatActivity {

	/**
	 * Constructor
	 */
	public RestSettingsActivity() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_activity_rest);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().replace(R.id.settings, new SettingsFragment()).commit();
		}
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Settings fragment
	 */
	public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

		/**
		 * Constructor
		 */
		public SettingsFragment() {
		}

		@Override
		public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
			setPreferencesFromResource(R.xml.rest_preferences, rootKey);
		}

		@Override
		public void onResume() {
			super.onResume();
			getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
			updatePort();
		}

		@Override
		public void onPause() {
			getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
			super.onPause();
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
				return;
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