package at.andreasrohner.spartantimelapserec.camera2;

import android.content.SharedPreferences;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.MeteringRectangle;
import android.util.Log;
import android.util.Size;

/**
 * Configure camera from android preferences
 */
public class ConfigureCamera2FromPrefs {

	/**
	 * Log Tag
	 */
	private static final String TAG = ConfigureCamera2FromPrefs.class.getSimpleName();

	/**
	 * Preferences
	 */
	private final SharedPreferences prefs;

	/**
	 * Image Size
	 */
	private int sizeW;

	/**
	 * Image Size
	 */
	private int sizeH;

	/**
	 * Constructor
	 *
	 * @param prefs Preferences
	 */
	public ConfigureCamera2FromPrefs(SharedPreferences prefs) {
		this.prefs = prefs;
	}

	/**
	 * Read camera size
	 *
	 * @return Image Size
	 */
	public Size prepareSize() {
		String sizeString = prefs.getString("pref_frame_size", "1920x1080");
		String[] sizeParts = sizeString.split("x");
		sizeW = Integer.parseInt(sizeParts[0]);
		sizeH = Integer.parseInt(sizeParts[1]);

		return new Size(sizeW, sizeH);
	}

	/**
	 * Configure camera
	 *
	 * @param captureBuilder Camera Configuration
	 */
	public void config(CaptureRequest.Builder captureBuilder) {
		configureAe(captureBuilder);
		configureFocus(captureBuilder);
		configureWb(captureBuilder);
	}

	/**
	 * Configure Focus
	 *
	 * @param captureBuilder Camera Configuration
	 */
	private void configureFocus(CaptureRequest.Builder captureBuilder) {
		String afMode = prefs.getString("pref_camera_af_mode", null);

		if ("field".equals(afMode)) {
			String afField = prefs.getString("pref_camera_af_field", null);
			if (afField == null) {
				Log.e(TAG, "Missing AF Field!");
				return;
			}

			float px;
			float py;
			try {
				String[] parts = afField.split("/");
				px = Float.parseFloat(parts[0]);
				py = Float.parseFloat(parts[1]);
			} catch (Exception e) {
				Log.e(TAG, "Invalid AF Value: «" + afField + "»");
				return;
			}

			int x = (int) (sizeW * px);
			int y = (int) (sizeH * py);

			int focusSize = 50;
			MeteringRectangle focusArea = new MeteringRectangle(Math.max(x - focusSize, 0), Math.max(y - focusSize, 0), focusSize * 2, focusSize * 2, MeteringRectangle.METERING_WEIGHT_MAX - 1);
			captureBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[] {focusArea});
		} else if ("manual".equals(afMode)) {
			captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
			float focusDistance = prefs.getFloat("pref_camera_af_manual", 0);
			captureBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, focusDistance);
		} else {
			captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
		}
	}

	/**
	 * Configure ISO and exposure time
	 *
	 * @param captureBuilder Camera Configuration
	 */
	private void configureAe(CaptureRequest.Builder captureBuilder) {
		int iso = prefs.getInt("pref_camera_iso", -1);
		long exposure = prefs.getLong("pref_camera_exposure", -1);

		if (iso == -1 && exposure == -1) {
			String currentFlashMode = prefs.getString("pref_camera_flash", "off");
			if ("auto".equals(currentFlashMode)) {
				captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
			} else if ("on".equals(currentFlashMode)) {
				captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
			} else {
				captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
			}

			int relExposure = prefs.getInt("pref_camera_exposure_rel", 0);
			if (relExposure != 0) {
				captureBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, relExposure);
			}
			return;
		}

		captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
		if (iso != -1) { //-1: Auto
			captureBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, iso);
		}

		if (exposure != -1) {
			captureBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, exposure);
		}
	}

	/**
	 * Configure white balance
	 *
	 * @param captureBuilder Camera Configuration
	 */
	private void configureWb(CaptureRequest.Builder captureBuilder) {
		String wb = prefs.getString("pref_camera_wb", "auto");
		int wbMode;
		switch (wb) {
			case "incandescent":
				wbMode = CaptureRequest.CONTROL_AWB_MODE_INCANDESCENT;
				break;

			case "daylight":
				wbMode = CaptureRequest.CONTROL_AWB_MODE_DAYLIGHT;
				break;

			case "fluorescent":
				wbMode = CaptureRequest.CONTROL_AWB_MODE_FLUORESCENT;
				break;

			case "cloud":
				wbMode = CaptureRequest.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT;
				break;

			case "auto":
			default:
				wbMode = CaptureRequest.CONTROL_AWB_MODE_AUTO;
				break;
		}
		// Not implemented: CONTROL_AWB_MODE_OFF, CONTROL_AWB_MODE_WARM_FLUORESCENT, CONTROL_AWB_MODE_TWILIGHT, CONTROL_AWB_MODE_SHADE
		captureBuilder.set(CaptureRequest.CONTROL_AWB_MODE, wbMode);
	}
}
