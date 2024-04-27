package at.andreasrohner.spartantimelapserec;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

/**
 * Helper class to start / stop picture service
 */
public class ServiceHelper {

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
		Intent intent = new Intent(context, Camera1ForegroundService.class);
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
		Intent intent = new Intent(context, Camera1ForegroundService.class);
		intent.setAction(Camera1ForegroundService.ACTION_STOP_SERVICE);
		context.startService(intent);
	}
}
