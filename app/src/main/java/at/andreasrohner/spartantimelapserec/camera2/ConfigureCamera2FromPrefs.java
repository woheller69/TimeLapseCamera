package at.andreasrohner.spartantimelapserec.camera2;

import android.content.SharedPreferences;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.MeteringRectangle;
import android.util.Log;

import at.andreasrohner.spartantimelapserec.rest.HttpThread;

/**
 * Configure camera from android preferences
 */
public class ConfigureCamera2FromPrefs {

	/**
	 * Log Tag
	 */
	private static final String TAG = HttpThread.class.getSimpleName();

	/**
	 * Preferences
	 */
	private final SharedPreferences prefs;

	/**
	 * Constructor
	 *
	 * @param prefs Preferences
	 */
	public ConfigureCamera2FromPrefs(SharedPreferences prefs) {
		this.prefs = prefs;
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
			// TODO This value is dependent on the current image resolution!

			String afField = prefs.getString("pref_camera_af_field", null);
			if (afField == null) {
				Log.e(TAG, "Missing AF Field!");
				return;
			}

			afField = afField.substring(1, afField.length() - 2);
			String[] parts = afField.split(",");
			int x = 0;
			int y = 0;
			int width = 1;
			int height = 1;
			int meteringWeight = 1;
			for (String p : parts) {
				p = p.trim();
				Log.e("xxxxxxxx", "xxxxx " + p);

				String[] f = p.split(":");
				if (f.length != 2) {
					continue;
				}

				int val = Integer.parseInt(f[1]);
				if ("x".equals(f[0])) {
					x = val;
				} else if ("y".equals(f[0])) {
					y = val;
				} else if ("w".equals(f[0])) {
					width = val;
				} else if ("h".equals(f[0])) {
					height = val;
				} else if ("wt".equals(f[0])) {
					meteringWeight = val;
				}
			}

			MeteringRectangle focusAreaTouch = new MeteringRectangle(x, y, width, height, meteringWeight);
			captureBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[] {focusAreaTouch});
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
