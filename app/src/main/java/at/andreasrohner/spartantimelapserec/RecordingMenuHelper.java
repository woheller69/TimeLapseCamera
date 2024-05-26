package at.andreasrohner.spartantimelapserec;

import android.content.Context;
import android.view.Menu;

import androidx.core.content.ContextCompat;
import at.andreasrohner.spartantimelapserec.data.SchedulingSettings;

/**
 * Recording menu helper
 */
public class RecordingMenuHelper {

	/**
	 * Menu
	 */
	private final Menu menu;

	/**
	 * Context
	 */
	private final Context context;

	/**
	 * Start menu ID
	 */
	private int idStart = 0;

	/**
	 * Preview menu ID
	 */
	private int idPreview = 0;

	/**
	 * Stop menu ID
	 */
	private int idStop = 0;

	/**
	 * Constructor
	 *
	 * @param menu    Menu
	 * @param context Context
	 */
	public RecordingMenuHelper(Menu menu, Context context) {
		this.menu = menu;
		this.context = context;
	}

	/**
	 * @param idStart Start menu ID
	 */
	public void setIdStart(int idStart) {
		this.idStart = idStart;
	}

	/**
	 * @param idPreview Preview menu ID
	 */
	public void setIdPreview(int idPreview) {
		this.idPreview = idPreview;
	}

	/**
	 * @param idStop Stop menu ID
	 */
	public void setIdStop(int idStop) {
		this.idStop = idStop;
	}

	/**
	 * Update Menu
	 */
	public void update() {
		if (BaseForegroundService.getStatus().getState() == ServiceState.State.RUNNING) {
			menu.findItem(idStart).setEnabled(false);
			menu.findItem(idStart).setIcon(ContextCompat.getDrawable(context, R.drawable.ic_radio_button_checked_disabled_24px));
			if (idPreview != 0) {
				menu.findItem(idPreview).setEnabled(false);
				menu.findItem(idPreview).setIcon(ContextCompat.getDrawable(context, R.drawable.ic_visibility_disabled_24px));
			}

			SchedulingSettings settings = new SchedulingSettings();
			settings.load(context);
			if (settings.isSchedRecEnabled() && settings.getSchedRecTime() > System.currentTimeMillis()) {
				menu.findItem(idStop).setEnabled(false);
				menu.findItem(idStop).setIcon(ContextCompat.getDrawable(context, R.drawable.ic_stop_circle_disabled_24px));
			}

		} else {
			menu.findItem(idStop).setEnabled(false);
			menu.findItem(idStop).setIcon(ContextCompat.getDrawable(context, R.drawable.ic_stop_circle_disabled_24px));
		}
	}
}
