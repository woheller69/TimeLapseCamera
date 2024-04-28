package at.andreasrohner.spartantimelapserec;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.camera2.Camera2ForegroundService;
import at.andreasrohner.spartantimelapserec.data.RecMode;
import at.andreasrohner.spartantimelapserec.data.RecSettingsLegacy;
import at.andreasrohner.spartantimelapserec.rest.HttpThread;

/**
 * Helper class to start / stop picture service
 */
public class ServiceHelper {

	/**
	 * Log Tag
	 */
	private static final String TAG = HttpThread.class.getSimpleName();

	/**
	 * Context
	 */
	private final Context context;

	/**
	 * Constructor
	 *
	 * @param context Context
	 */
	public ServiceHelper(Context context) {
		this.context = context;
	}

	/**
	 * Start
	 *
	 * @param calledFromUi true if called from Activity, false if not
	 */
	public void start(boolean calledFromUi) {
		Log.i(TAG, "Start Service");

		ImageRecorderState.resetImageCount();

		Intent intent;
		if (getRecMode() == RecMode.CAMERA2_TIME_LAPSE) {
			intent = new Intent(context, Camera2ForegroundService.class);
		} else {
			intent = new Intent(context, Camera1ForegroundService.class);
		}

		if (BaseForegroundService.getStatus().getState() == ServiceState.State.RUNNING) {
			if (calledFromUi) {
				Toast.makeText(context, context.getString(R.string.error_already_running), Toast.LENGTH_SHORT).show();
			}
		} else {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				context.startForegroundService(intent);
			} else {
				context.startService(intent);
			}
		}
	}

	/**
	 * Stop
	 */
	public void stop() {
		Log.i(TAG, "Stop Service");

		Intent intent;
		if (getRecMode() == RecMode.CAMERA2_TIME_LAPSE) {
			intent = new Intent(context, Camera2ForegroundService.class);
		} else {
			intent = new Intent(context, Camera1ForegroundService.class);
		}

		intent.setAction(Camera1ForegroundService.ACTION_STOP_SERVICE);
		context.startService(intent);
	}

	/**
	 * @return The current recording mdoe
	 */
	private RecMode getRecMode() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
		RecMode mode = RecSettingsLegacy.getRecMode(prefs);
		Log.i(TAG, "Rec mode: " + mode);
		return mode;
	}

	/**
	 * Start / stop depending on scheduling
	 */
	public void startStopIfSchedulingIsActive() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if (prefs.getBoolean("pref_schedule_recording_enabled", false)) {
			start(true);
		} else {
			stop();
		}
	}
}
