package at.andreasrohner.spartantimelapserec.camera2;

import android.content.SharedPreferences;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.R;

/**
 * Handle buttons and button actions of the camera preview
 */
public class CameraControlButtonHandler implements PopupDialogBase.DialogResult {

	/**
	 * Activity
	 */
	private final AppCompatActivity activity;

	/**
	 * Preferences
	 */
	private final SharedPreferences prefs;

	/**
	 * Camera
	 */
	private Camera2Wrapper camera;

	/**
	 * AF/MF, Focus Control Button
	 */
	private ImageButton afmfButton;

	/**
	 * ISO Button
	 */
	private ImageButton isoButton;

	/**
	 * Brightness button
	 */
	private ImageButton brightnessButton;

	/**
	 * Camera Configuration Listener
	 */
	private ConfigChangeListener configChangeListener;

	/**
	 * Constructor
	 *
	 * @param activity Activity
	 */
	public CameraControlButtonHandler(AppCompatActivity activity) {
		this.activity = activity;
		this.prefs = PreferenceManager.getDefaultSharedPreferences(activity);
	}

	/**
	 * @param configChangeListener Camera Configuration Listener
	 */
	public void setConfigChangeListener(ConfigChangeListener configChangeListener) {
		this.configChangeListener = configChangeListener;
	}

	/**
	 * Camera opened
	 *
	 * @param camera Camera
	 */
	public void cameraOpened(Camera2Wrapper camera) {
		this.camera = camera;

		this.afmfButton = ((ImageButton) activity.findViewById(R.id.bt_afmf));

		this.isoButton = ((ImageButton) activity.findViewById(R.id.bt_iso));
		PopupDialogIso isoDialog = new PopupDialogIso(activity, camera);
		isoDialog.setDialogResultListener(this);
		this.isoButton.setOnClickListener(isoDialog);

		this.brightnessButton = ((ImageButton) activity.findViewById(R.id.bt_brightness));

		updateButtonImage();
	}

	/**
	 * Update button according to the config
	 */
	private void updateButtonImage() {
		int iso = prefs.getInt("pref_camera_iso", -1);
		if (iso == -1) {
			this.isoButton.setImageResource(R.drawable.ic_cam_bt_iso);
		} else {
			this.isoButton.setImageResource(R.drawable.ic_cam_bt_iso_enabled);
		}

/*
		if (iso == -1) {
			this.afmfButton.setImageResource(R.drawable.ic_cam_bt_afmf);
		} else {
			this.afmfButton.setImageResource(R.drawable.ic_cam_bt_afmf_enabled);
		}
*/
		//	this.brightnessButton = ((ImageButton) activity.findViewById(R.id.bt_brightness));
	}

	@Override
	public void dialogFinished(boolean accepted) {
		updateButtonImage();
		if (configChangeListener != null) {
			configChangeListener.cameraConfigChanged();
		}
	}

	/**
	 * Configuration has been changed
	 */
	public interface ConfigChangeListener {

		/**
		 * Camera Configuration has been changed
		 */
		void cameraConfigChanged();
	}
}
