package at.andreasrohner.spartantimelapserec.camera2;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ImageButton;

import androidx.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.rest.HttpThread;

/**
 * Menu dialog with all settings which are not directly available
 */
public class PopupDialogMenu extends PopupDialogBase {

	/**
	 * Log Tag
	 */
	private static final String TAG = HttpThread.class.getSimpleName();

	/**
	 * White Balance Button
	 */
	private final ImageButton camWbA;

	/**
	 * White Balance Button
	 */
	private final ImageButton camWbIncandescent;

	/**
	 * White Balance Button
	 */
	private final ImageButton camWbDaylight;

	/**
	 * White Balance Button
	 */
	private final ImageButton camWbFluorescent;

	/**
	 * White Balance Button
	 */
	private final ImageButton camWbCloud;

	/**
	 * Current WB mode
	 */
	private String currentWbMode;

	/**
	 * Constructor
	 *
	 * @param context Context
	 * @param camera  Camera
	 */
	public PopupDialogMenu(Context context, Camera2Wrapper camera) {
		super(context);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		this.currentWbMode = prefs.getString("pref_camera_wb", "auto");

		this.camWbA = ((ImageButton) view.findViewById(R.id.camWbA));
		this.camWbA.setOnClickListener(l -> setWbMode("auto"));
		this.camWbIncandescent = ((ImageButton) view.findViewById(R.id.camWbIncandescent));
		this.camWbIncandescent.setOnClickListener(l -> setWbMode("incandescent"));
		this.camWbDaylight = ((ImageButton) view.findViewById(R.id.camWbDaylight));
		this.camWbDaylight.setOnClickListener(l -> setWbMode("daylight"));
		this.camWbFluorescent = ((ImageButton) view.findViewById(R.id.camWbFluorescent));
		this.camWbFluorescent.setOnClickListener(l -> setWbMode("fluorescent"));
		this.camWbCloud = ((ImageButton) view.findViewById(R.id.camWbCloud));
		this.camWbCloud.setOnClickListener(l -> setWbMode("cloud"));
		updateWbButtons();
	}

	/**
	 * Set WB Mode
	 *
	 * @param mode Mode
	 */
	private void setWbMode(String mode) {
		this.currentWbMode = mode;

		updateWbButtons();
	}

	/**
	 * Update the WB Button Images
	 */
	private void updateWbButtons() {
		if ("auto".equals(this.currentWbMode)) {
			this.camWbA.setImageResource(R.drawable.ic_cam_bt_wb_a_enabled);
		} else {
			this.camWbA.setImageResource(R.drawable.ic_cam_bt_wb_a);
		}

		if ("incandescent".equals(this.currentWbMode)) {
			this.camWbIncandescent.setImageResource(R.drawable.ic_cam_bt_wb_incandescent_enabled);
		} else {
			this.camWbIncandescent.setImageResource(R.drawable.ic_cam_bt_wb_incandescent);
		}

		if ("daylight".equals(this.currentWbMode)) {
			this.camWbDaylight.setImageResource(R.drawable.ic_cam_bt_wb_daylight_enabled);
		} else {
			this.camWbDaylight.setImageResource(R.drawable.ic_cam_bt_wb_daylight);
		}

		if ("fluorescent".equals(this.currentWbMode)) {
			this.camWbFluorescent.setImageResource(R.drawable.ic_cam_bt_wb_fluorescent_enabled);
		} else {
			this.camWbFluorescent.setImageResource(R.drawable.ic_cam_bt_wb_fluorescent);
		}

		if ("cloud".equals(this.currentWbMode)) {
			this.camWbCloud.setImageResource(R.drawable.ic_cam_bt_wb_cloud_enabled);
		} else {
			this.camWbCloud.setImageResource(R.drawable.ic_cam_bt_wb_cloud);
		}
	}

	@Override
	protected void storeValue() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("pref_camera_wb", currentWbMode);
		editor.apply();
	}

	@Override
	public int getDialogId() {
		return R.layout.dialog_cam_menu;
	}

	@Override
	public int getTitleId() {
		return R.string.camera_menu_config;
	}

	@Override
	public int getMessageId() {
		return R.string.camera_menu_config_message;
	}
}
