package at.andreasrohner.spartantimelapserec.camera2;

import android.content.Context;

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
	 * Constructor
	 *
	 * @param context Context
	 * @param camera  Camera
	 */
	public PopupDialogMenu(Context context, Camera2Wrapper camera) {
		super(context);
	}

	@Override
	protected void storeValue() {
/*
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putLong("pref_camera_exposure", exposure);
		editor.apply();*/
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
