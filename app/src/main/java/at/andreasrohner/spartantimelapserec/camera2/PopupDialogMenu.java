package at.andreasrohner.spartantimelapserec.camera2;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
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
	 * Camera Selection
	 */
	private final Spinner camCameraSelection;

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

		CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

		String selectedCamera = prefs.getString("pref_camera", null);

		List<Camera> cameras = new ArrayList<>();
		try {
			for (String cameraId : manager.getCameraIdList()) {
				cameras.add(new Camera(context, cameraId, manager.getCameraCharacteristics(cameraId)));
			}
		} catch (CameraAccessException e) {
			Log.e(TAG, "Could not list cameras", e);
		}

		this.camCameraSelection = (Spinner) view.findViewById(R.id.camCameraSelection);
		ArrayAdapter<CharSequence> adapter = new ArrayAdapter(context, android.R.layout.simple_spinner_item, cameras);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		this.camCameraSelection.setAdapter(adapter);

		int selectedCameraIndex = -1;
		if (selectedCamera != null) {
			for (int i = 0; i < cameras.size(); i++) {
				if (selectedCamera.equals(cameras.get(i).getCameraId())) {
					selectedCameraIndex = i;
					break;
				}
			}
		}

		if (selectedCameraIndex != -1) {
			this.camCameraSelection.setSelection(selectedCameraIndex);
		}

		this.camCameraSelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				updateFlashButton();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				updateFlashButton();
			}
		});

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
	 * Update the Flash Button Images
	 */
	private void updateFlashButton() {
		CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
		String newSelectedCamera = ((Camera) this.camCameraSelection.getSelectedItem()).getCameraId();
		boolean flashSupported = false;
		try {
			Boolean available = manager.getCameraCharacteristics(newSelectedCamera).get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
			flashSupported = available == null ? false : available;
		} catch (CameraAccessException e) {
			Log.e(TAG, "Could not get camera information", e);
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
		String newSelectedCamera = ((Camera) this.camCameraSelection.getSelectedItem()).getCameraId();

		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("pref_camera_wb", currentWbMode);

		if (newSelectedCamera != null && !newSelectedCamera.equals(selectedCamera)) {
			editor.putString("pref_camera", newSelectedCamera);
			flags = 1;
		}

		editor.putString("pref_camera_flash", currentFlashMode);

		editor.apply();
		return flags;
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

	/**
	 * Camera
	 */
	private static class Camera {

		/**
		 * Camera ID
		 */
		private final String cameraId;

		/**
		 * Display name
		 */
		private final String name;

		/**
		 * Constructor
		 *
		 * @param context         Context
		 * @param cameraId        Camera ID
		 * @param characteristics CameraCharacteristics
		 */
		public Camera(Context context, String cameraId, CameraCharacteristics characteristics) {
			this.cameraId = cameraId;
			Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);

			String cam;
			if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
				cam = cameraId + ": " + context.getString(R.string.pref_camera_front);
			} else {
				cam = cameraId + ": " + context.getString(R.string.pref_camera_back);
			}

			float[] focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
			if (focalLengths != null && focalLengths.length > 0) {
				cam += " Lens: " + focalLengths[0] + "mm";
			}
			this.name = cam;
		}

		/**
		 * @return Camera ID
		 */
		public String getCameraId() {
			return cameraId;
		}

		@NonNull
		@Override
		public String toString() {
			return name;
		}
	}
}
