package at.andreasrohner.spartantimelapserec.rest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Toast;

import androidx.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.R;

/**
 * Utility class to start / stop webserver
 */
public final class RestControlUtil {

	/**
	 * Utility class
	 */
	private RestControlUtil() {
	}

	/**
	 * Start or stop REST API Server, as set by the preference 'pref_restapi_enabled'.
	 * If already in the correct state, it does nothing.
	 */
	public static void startStopRestApiServer(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		boolean restApiEnabled = prefs.getBoolean("pref_restapi_enabled", false);
		if (RestService.isRunning() == restApiEnabled) {
			// The service and settings are in the same state - nothing to do
			return;
		}

		if (restApiEnabled) {
			Intent intent = new Intent(context, RestService.class);
			if (!RestService.isRunning()) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					context.startForegroundService(intent);
				} else {
					context.startService(intent);
				}
			}

			Toast.makeText(context, context.getText(R.string.info_restapi_started), Toast.LENGTH_SHORT).show();
		} else {
			Intent intent = new Intent(context, RestService.class);
			context.stopService(intent);

			Toast.makeText(context, context.getText(R.string.info_restapi_stopped), Toast.LENGTH_SHORT).show();
		}
	}
}
