package at.andreasrohner.spartantimelapserec.preference.preftype;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraManager;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.camera2.AfPos;
import at.andreasrohner.spartantimelapserec.camera2.CameraTiming;
import at.andreasrohner.spartantimelapserec.camera2.pupcfg.CameraModel;
import at.andreasrohner.spartantimelapserec.preference.CameraSettings;
import at.andreasrohner.spartantimelapserec.state.Logger;

/**
 * Show a camera setting, read only
 */
@SuppressWarnings("unused")
public class ShowCameraInfoPreference extends DialogPreference {

	/**
	 * Logger
	 */
	private Logger logger = new Logger(getClass());

	/**
	 * Camera timing values
	 */
	private CameraTiming timing;

	/**
	 * Constructor
	 *
	 * @param context      Context
	 * @param attrs        AttributeSet
	 * @param defStyleAttr Style Attributes
	 * @param defStyleRes  Style Resources
	 */
	public ShowCameraInfoPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	/**
	 * Constructor
	 *
	 * @param context      Context
	 * @param attrs        AttributeSet
	 * @param defStyleAttr Style Attributes
	 */
	public ShowCameraInfoPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	/**
	 * Constructor
	 *
	 * @param context Context
	 * @param attrs   AttributeSet
	 */
	public ShowCameraInfoPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onAttached() {
		super.onAttached();

		updateSummary();
	}

	/**
	 * Show the value in the summary part
	 */
	public void updateSummary() {
		String key = getKey();
		SharedPreferences prefs = getPreferenceManager().getSharedPreferences();

		if ("pref_camera_iso".equals(key)) {
			int iso = prefs.getInt("pref_camera_iso", -1);
			if (iso == -1) {
				setIcon(R.drawable.ic_cam_bt_iso);
				setSummary(R.string.camera_value_auto);
			} else {
				setIcon(R.drawable.ic_cam_bt_iso_enabled);
				setSummary(String.valueOf(iso));
			}
		} else if ("pref_camera_exposure".equals(key)) {
			long exposure = prefs.getLong("pref_camera_exposure", -1);
			int relExposure = prefs.getInt("pref_camera_exposure_rel", 0);
			if (exposure == -1 && relExposure == 0) {
				setIcon(R.drawable.ic_cam_bt_brightness);
				setSummary(R.string.camera_value_auto);
			} else {
				setIcon(R.drawable.ic_cam_bt_brightness_enabled);
				setSummary(CameraSettings.formatBrightness(getContext(), false));
			}

		} else if ("pref_camera_wb".equals(key)) {
			String currentWbMode = prefs.getString("pref_camera_wb", "auto");

			if ("auto".equals(currentWbMode)) {
				setIcon(R.drawable.ic_cam_bt_wb_a);
				setSummary(R.string.camera_value_auto);
			} else if ("incandescent".equals(currentWbMode)) {
				setIcon(R.drawable.ic_cam_bt_wb_incandescent_enabled);
				setSummary(R.string.cam_wb_incandescent);
			} else if ("daylight".equals(currentWbMode)) {
				setIcon(R.drawable.ic_cam_bt_wb_daylight_enabled);
				setSummary(R.string.cam_wb_daylight);
			} else if ("fluorescent".equals(currentWbMode)) {
				setIcon(R.drawable.ic_cam_bt_wb_fluorescent_enabled);
				setSummary(R.string.cam_wb_fluorescent);
			} else if ("cloud".equals(currentWbMode)) {
				setIcon(R.drawable.ic_cam_bt_wb_cloud_enabled);
				setSummary(R.string.cam_wb_cloud);
			}
		} else if ("pref_camera_flash".equals(key)) {
			String currentFlashMode = prefs.getString("pref_camera_flash", "off");

			if ("auto".equals(currentFlashMode)) {
				setIcon(R.drawable.ic_cam_bt_flash_auto);
				setSummary(R.string.camera_value_auto);
			} else if ("on".equals(currentFlashMode)) {
				setIcon(R.drawable.ic_cam_bt_flash_on);
				setSummary(R.string.camera_flash_on);
			} else {
				setIcon(R.drawable.ic_cam_bt_flash_off);
				setSummary(R.string.camera_flash_off);
			}
		} else if ("pref_camera_af_mode".equals(key)) {
			String afMode = prefs.getString("pref_camera_af_mode", null);

			if ("field".equals(afMode)) {
				AfPos pos = AfPos.fromString(prefs.getString("pref_camera_af_field", null));
				if (pos == null) {
					setSummary("ERROR");
				} else {
					setSummary("F: " + String.format("%.02f", pos.getFocusRelX()) + " / " + String.format("%.02f", pos.getFocusRelY()));
				}
			} else if ("manual".equals(afMode)) {
				float focusDistance = prefs.getFloat("pref_camera_af_manual", 0);
				String m;
				if (focusDistance == 0) {
					m = "∞";
				} else {
					m = String.format("%.2f", (1.0f / focusDistance));
				}
				setSummary("M: " + String.format("%.4f", focusDistance) + " " + getContext().getText(R.string.dioptre) + " / " + m + "m");
			} else {
				setSummary(R.string.camera_value_auto);
			}
		} else if ("pref_camera".equals(key)) {
			String cameraId = prefs.getString("pref_camera", null);

			if (cameraId == null) {
				setSummary(R.string.camera_not_selected);
			} else {
				CameraManager manager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
				try {
					CameraModel camera = new CameraModel(getContext(), cameraId, manager.getCameraCharacteristics(cameraId));
					setSummary(camera.toString());
				} catch (Exception e) {
					logger.error("Error loading camera details", e);
					setSummary("Could not load Camera Details: " + e);
				}
			}

		} else {
			setSummary(prefs.getString(key, null));
		}
	}
}
