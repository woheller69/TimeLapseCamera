package at.andreasrohner.spartantimelapserec.camera2.pupcfg;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Size;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.camera2.PopupDialogBase;
import at.andreasrohner.spartantimelapserec.camera2.wrapper.Camera2Wrapper;
import at.andreasrohner.spartantimelapserec.state.Logger;

/**
 * Menu dialog with all settings which are not directly available
 */
public class PopupDialogMenu extends PopupDialogBase {

	/**
	 * Logger
	 */
	private Logger logger = new Logger(getClass());

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
	 * Camera Selection
	 */
	private final SpinnerHelper<CameraModel> camCameraSelection;

	/**
	 * Camera resolution
	 */
	private final SpinnerHelper<ResolutionModel> camCameraResolution;

	/**
	 * Current Flash mode
	 */
	private final ImageButton camFlashOff;

	/**
	 * Current Flash mode
	 */
	private final ImageButton camFlashAuto;

	/**
	 * Current Flash mode
	 */
	private final ImageButton camFlashOn;

	/**
	 * Camera manager
	 */
	private final CameraManager manager;

	/**
	 * Current WB mode
	 */
	private String currentWbMode;

	/**
	 * Current Flash mode
	 */
	private String currentFlashMode;

	/**
	 * Constructor
	 *
	 * @param context Context
	 * @param camera  Camera
	 */
	public PopupDialogMenu(Context context, Camera2Wrapper camera) {
		super(context);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		this.manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

		String selectedCamera = prefs.getString("pref_camera", null);

		List<CameraModel> cameras = new ArrayList<>();
		try {
			for (String cameraId : manager.getCameraIdList()) {
				cameras.add(new CameraModel(context, cameraId, manager.getCameraCharacteristics(cameraId)));
			}
		} catch (Exception e) {
			logger.error("Could not list cameras", e);
		}

		this.camCameraSelection = new SpinnerHelper<>((Spinner) view.findViewById(R.id.camCameraSelection), context);
		this.camCameraSelection.setData(cameras);
		this.camCameraSelection.selectById(selectedCamera);
		this.camCameraSelection.setValueChangeListener(() -> cameraChanged());

		this.camCameraResolution = new SpinnerHelper<>((Spinner) view.findViewById(R.id.camCameraResolution), context);

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

		currentFlashMode = prefs.getString("pref_camera_flash", "off");

		this.camFlashOff = ((ImageButton) view.findViewById(R.id.camFlashOff));
		this.camFlashOff.setOnClickListener(l -> setFlashMode("off"));
		this.camFlashAuto = ((ImageButton) view.findViewById(R.id.camFlashAuto));
		this.camFlashAuto.setOnClickListener(l -> setFlashMode("auto"));
		this.camFlashOn = ((ImageButton) view.findViewById(R.id.camFlashOn));
		this.camFlashOn.setOnClickListener(l -> setFlashMode("on"));
		updateFlashButton();
	}

	/**
	 * Set Flash Mode
	 *
	 * @param mode Mode
	 */
	private void setFlashMode(String mode) {
		this.currentFlashMode = mode;

		updateFlashButton();
	}

	/**
	 * Camera was changed
	 */
	public void cameraChanged() {
		updateFlashButton();

		final StreamConfigurationMap map;
		try {
			final CameraCharacteristics characteristics = manager.getCameraCharacteristics(this.camCameraSelection.getSelectedItem().getId());
			map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

			if (map == null) {
				logger.error("Could not get camera characteristics");
			}
		} catch (Exception e) {
			logger.error("Error get camera resolutions", e);
			return;
		}

		List<ResolutionModel> resolutions = new ArrayList<>();
		Size[] resolutionArray = map.getOutputSizes(ImageFormat.JPEG);
		for (Size s : resolutionArray) {
			resolutions.add(new ResolutionModel(s));
		}
		Collections.sort(resolutions);
		this.camCameraResolution.setData(resolutions);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		this.camCameraResolution.selectById(prefs.getString("pref_frame_size", null));
	}

	/**
	 * Update the Flash Button Images
	 */
	private void updateFlashButton() {
		CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
		String newSelectedCamera = this.camCameraSelection.getSelectedItem().getId();
		boolean flashSupported = false;
		try {
			Boolean available = manager.getCameraCharacteristics(newSelectedCamera).get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
			flashSupported = available == null ? false : available;
		} catch (Exception e) {
			logger.error("Could not get camera information", e);
		}

		if (flashSupported) {
			this.camFlashAuto.setVisibility(View.VISIBLE);
			this.camFlashOn.setVisibility(View.VISIBLE);
		} else {
			this.camFlashOff.setImageResource(R.drawable.ic_cam_bt_flash_off_enabled);
			this.camFlashAuto.setVisibility(View.GONE);
			this.camFlashOn.setVisibility(View.GONE);
		}

		if ("off".equals(this.currentFlashMode)) {
			this.camFlashOff.setImageResource(R.drawable.ic_cam_bt_flash_off_enabled);
		} else {
			this.camFlashOff.setImageResource(R.drawable.ic_cam_bt_flash_off);
		}

		if ("auto".equals(this.currentFlashMode)) {
			this.camFlashAuto.setImageResource(R.drawable.ic_cam_bt_flash_auto_enabled);
		} else {
			this.camFlashAuto.setImageResource(R.drawable.ic_cam_bt_flash_auto);
		}

		if ("on".equals(this.currentFlashMode)) {
			this.camFlashOn.setImageResource(R.drawable.ic_cam_bt_flash_on_enabled);
		} else {
			this.camFlashOn.setImageResource(R.drawable.ic_cam_bt_flash_on);
		}
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
	protected int storeValue() {
		int flags = 0;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String selectedCamera = prefs.getString("pref_camera", null);
		String lastFrameSize = prefs.getString("pref_camera", null);
		String newSelectedCamera = this.camCameraSelection.getSelectedItem().getId();

		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("pref_camera_wb", currentWbMode);

		if (newSelectedCamera != null && !newSelectedCamera.equals(selectedCamera)) {
			editor.putString("pref_camera", newSelectedCamera);

			// Reset Auto Focus configuration on camera change
			resetFocusPreferences(editor);
			flags = 1;
		}

		editor.putString("pref_camera_flash", currentFlashMode);
		String newFrameSize = this.camCameraResolution.getSelectedItem().getId();
		if (!newFrameSize.equals(lastFrameSize)) {
			// Reset Auto Focus configuration on resolution change
			resetFocusPreferences(editor);
		}
		editor.putString("pref_frame_size", newFrameSize);

		editor.apply();
		return flags;
	}

	/**
	 * Reset preferences
	 *
	 * @param editor Editor
	 */
	private void resetFocusPreferences(SharedPreferences.Editor editor) {
		editor.putString("pref_camera_af_field", "");
		editor.putString("pref_camera_af_mode", "auto");
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
