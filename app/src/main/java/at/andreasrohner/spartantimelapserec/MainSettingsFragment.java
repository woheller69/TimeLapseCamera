package at.andreasrohner.spartantimelapserec;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import at.andreasrohner.spartantimelapserec.settings.RestSettingsActivity;
import at.andreasrohner.spartantimelapserec.settings.ShowActivityPreference;

/**
 * Main Settings menu
 */
public class MainSettingsFragment extends PreferenceFragmentCompat {

	/**
	 * Constructor
	 */
	public MainSettingsFragment() {
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.main_preferences, rootKey);

		updateSummary();
	}

	@Override
	public void onResume() {
		super.onResume();

		updateSummary();
	}

	/**
	 * Update summary texts
	 */
	public void updateSummary() {
		PreferenceScreen prefScreen = getPreferenceScreen();
		int prefCount = prefScreen.getPreferenceCount();
		for (int i = 0; i < prefCount; i++) {
			Preference pref = prefScreen.getPreference(i);

			if (pref instanceof PreferenceCategory) {
				updateSummary((PreferenceCategory) pref);
			}
		}
	}

	/**
	 * Update summary of one preference category
	 *
	 * @param pref PreferenceCategory
	 */
	private void updateSummary(PreferenceCategory pref) {
		int prefCount = pref.getPreferenceCount();
		for (int i = 0; i < prefCount; i++) {
			Preference p = pref.getPreference(i);
			if (!(p instanceof ShowActivityPreference)) {
				continue;
			}

			((ShowActivityPreference) p).updateSummary();
		}
	}

	@Override
	public void onDisplayPreferenceDialog(Preference preference) {
		if (preference instanceof ShowActivityPreference) {
			ShowActivityPreference p = (ShowActivityPreference) preference;
			Context ctx = getContext();
			Class<? extends Activity> activityClass = p.getActivityClass();
			if (activityClass == null) {
				return;
			}

			Intent myIntent = new Intent(ctx, activityClass);
			ctx.startActivity(myIntent);
		} else {
			super.onDisplayPreferenceDialog(preference);
		}
	}
}
