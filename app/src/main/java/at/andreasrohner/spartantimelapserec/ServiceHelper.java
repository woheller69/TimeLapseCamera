package at.andreasrohner.spartantimelapserec;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Toast;

import androidx.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.camera2.Camera2ForegroundService;
import at.andreasrohner.spartantimelapserec.camera2.Camera2PreviewRecorder;
import at.andreasrohner.spartantimelapserec.camera2.Camera2Recorder;
import at.andreasrohner.spartantimelapserec.data.RecMode;
import at.andreasrohner.spartantimelapserec.data.RecSettingsLegacy;
import at.andreasrohner.spartantimelapserec.state.Logger;

/**
 * Helper class to start / stop picture service
 */
public class ServiceHelper {

	/**
	 * Who started the service
	 */
	public enum ServiceStartType {

		/**
		 * Started from UI
		 */
		UI(true),

		/**
		 * Started by REST API
		 */
		REST(false),

		/**
		 * Started from Preview2 Activity
		 */
		PREVIEW(true);

		/**
		 * true if called from Activity, false if not
		 */
		private boolean calledFromUi;

		/**
		 * Constructor
		 *
		 * @param calledFromUi true if called from Activity, false if not
		 */
		ServiceStartType(boolean calledFromUi) {
			this.calledFromUi = calledFromUi;
		}
	}

	/**
	 * Current preview activity
	 */
	private static Activity currentPreviewActivity;

	/**
	 * Logger
	 */
	private Logger logger = new Logger(getClass());

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
	 * Set current preview activity
	 *
	 * @param activity Activity
	 */
	public static synchronized void setCurrentPreviewActivity(Activity activity) {
		ServiceHelper.currentPreviewActivity = activity;
	}

	/**
	 * Reset current preview activity
	 *
	 * @param activity Activity
	 */
	public static synchronized void resetCurrentPreviewActivity(Activity activity) {
		if (ServiceHelper.currentPreviewActivity == activity) {
			ServiceHelper.currentPreviewActivity = null;
		}
	}

	/**
	 * Start
	 *
	 * @param type Start Type
	 */
	public void start(ServiceStartType type) {
		logger.info("Start Service");

		if (type != ServiceStartType.PREVIEW) {
			// Stop preview, if running and started by REST API
			// This may should be improved later
			synchronized (ServiceHelper.class) {
				if (ServiceHelper.currentPreviewActivity != null) {
					ServiceHelper.currentPreviewActivity.finish();
				}
			}
		}

		Intent intent;
		if (getRecMode() == RecMode.CAMERA2_TIME_LAPSE) {
			intent = new Intent(context, Camera2ForegroundService.class);
			if (type == ServiceStartType.PREVIEW) {
				intent.putExtra("recorder", Camera2PreviewRecorder.class.getSimpleName());
			} else {
				intent.putExtra("recorder", Camera2Recorder.class.getSimpleName());
			}
		} else {
			intent = new Intent(context, Camera1ForegroundService.class);
		}

		if (BaseForegroundService.getStatus().getState() == ServiceState.State.RUNNING) {
			if (type.calledFromUi) {
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
	 *
	 * @param reason    Stop Reason
	 * @param errorStop Stopped because of error
	 */
	public void stop(String reason, boolean errorStop) {
		logger.info("Stop Service");

		Intent intent;
		if (getRecMode() == RecMode.CAMERA2_TIME_LAPSE) {
			intent = new Intent(context, Camera2ForegroundService.class);
		} else {
			intent = new Intent(context, Camera1ForegroundService.class);
		}

		intent.setAction(Camera1ForegroundService.ACTION_STOP_SERVICE);
		intent.putExtra("reason", reason);
		intent.putExtra("errorStop", errorStop);
		context.startService(intent);
	}

	/**
	 * @return The current recording mdoe
	 */
	private RecMode getRecMode() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
		RecMode mode = RecSettingsLegacy.getRecMode(prefs);
		logger.info("Rec mode: {}", mode);
		return mode;
	}

	/**
	 * Start / stop depending on scheduling
	 */
	public void startStopIfSchedulingIsActive() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if (prefs.getBoolean("pref_schedule_recording_enabled", false)) {
			start(ServiceStartType.UI);
		} else {
			if (BaseForegroundService.getStatus().getState() == ServiceState.State.SCHEDULED) {
				stop("Stop, scheduling stopped", false);
			}
		}
	}
}
