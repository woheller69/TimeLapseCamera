package at.andreasrohner.spartantimelapserec.camera2;

import android.content.SharedPreferences;
import android.hardware.camera2.CaptureRequest;

/**
 * Configure camera from android preferences
 */
public class ConfigureCamera2FromPrefs {

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
		captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
		int iso = prefs.getInt("pref_camera_iso", -1);
		if (iso != -1) { //-1: Auto
			captureBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, iso);
		}

		long exposure = prefs.getLong("pref_camera_exposure", -1);
		if (exposure != -1) {
			captureBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, exposure);
		}
	}
}
