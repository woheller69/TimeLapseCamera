package at.andreasrohner.spartantimelapserec.preference;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.Preference;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.camera2.CameraTiming;
import at.andreasrohner.spartantimelapserec.camera2.SetupExternalStorageActivity;
import at.andreasrohner.spartantimelapserec.data.RecMode;
import at.andreasrohner.spartantimelapserec.data.RecSettingsLegacy;
import at.andreasrohner.spartantimelapserec.preference.mainmenu.MainSettingsMenu;

/**
 * Camera Settings
 */
@SuppressWarnings({"unused", "deprecated"}) // Loaded by menu / legacy classes, will not be updated
public class ExternalStorageSetupSettings implements MainSettingsMenu {

	/**
	 * Camera timing values
	 */
	private static CameraTiming timing;

	/**
	 * Constructor
	 */
	public ExternalStorageSetupSettings() {
	}

	@Override
	public Class<? extends Activity> getActivityClass(SharedPreferences prefs) {
		return SetupExternalStorageActivity.class;
	}

	@Override
	public void updateSummary(Preference pref, Context ctx, SharedPreferences prefs) {
		boolean enabled = prefs.getBoolean("external_storage_enabled", false);
		if (!enabled) {
			pref.setSummary(ctx.getString(R.string.external_storage_info_disabled));
			return;
		}

		if (RecSettingsLegacy.getRecMode(prefs) == RecMode.CAMERA2_TIME_LAPSE) {
			pref.setSummary(ctx.getString(R.string.external_storage_info_enabled));
		} else {
			pref.setSummary(ctx.getString(R.string.external_storage_info_not_possible_with_camera1));
		}
	}
}
