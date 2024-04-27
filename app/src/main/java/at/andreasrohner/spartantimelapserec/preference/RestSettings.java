package at.andreasrohner.spartantimelapserec.preference;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.net.InetAddress;

import androidx.preference.Preference;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.preference.activity.RestSettingsActivity;
import at.andreasrohner.spartantimelapserec.preference.mainmenu.MainSettingsMenu;
import at.andreasrohner.spartantimelapserec.rest.RestService;

/**
 * REST Settings
 */
@SuppressWarnings("unused") // Loaded by menu
public class RestSettings implements MainSettingsMenu {

	/**
	 * Constructor
	 */
	public RestSettings() {
	}

	@Override
	public Class<? extends Activity> getActivityClass(SharedPreferences prefs) {
		return RestSettingsActivity.class;
	}

	@Override
	public void updateSummary(Preference pref, Context ctx, SharedPreferences prefs) {
		InetAddress addr = RestService.getLocalInetAddress(ctx);
		if (addr == null) {
			pref.setSummary(ctx.getString(R.string.error_no_ip_refresh));
		} else {
			pref.setSummary("http:/" + addr + ":" + RestService.getPort(ctx));
		}

		if (prefs.getBoolean("pref_restapi_enabled", false)) {
			pref.setTitle(ctx.getString(R.string.pref_restapi_connectioninfo_title_on));
		} else {
			pref.setTitle(ctx.getString(R.string.pref_restapi_connectioninfo_title_off));
		}
	}
}
