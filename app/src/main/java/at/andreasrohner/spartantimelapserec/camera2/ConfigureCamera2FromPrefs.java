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
		// TODO !!!!!!!!!!!!!!!!!!!
		//captureBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, seekFocus);
/*
// TODO !!!!!!!!!!!!!!!!!!!
		public static final int CONTROL_AWB_MODE_OFF = 0;
		public static final int CONTROL_AWB_MODE_AUTO = 1;
		public static final int CONTROL_AWB_MODE_INCANDESCENT = 2;
		public static final int CONTROL_AWB_MODE_FLUORESCENT = 3;
		public static final int CONTROL_AWB_MODE_WARM_FLUORESCENT = 4;
		public static final int CONTROL_AWB_MODE_DAYLIGHT = 5;
		public static final int CONTROL_AWB_MODE_CLOUDY_DAYLIGHT = 6;
		public static final int CONTROL_AWB_MODE_TWILIGHT = 7;
		public static final int CONTROL_AWB_MODE_SHADE = 8;
		*/

		captureBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO);
                                /*captureBuilder.set(CaptureRequest.COLOR_CORRECTION_MODE, CaptureRequest.COLOR_CORRECTION_MODE_TRANSFORM_MATRIX);
                                captureBuilder.set(CaptureRequest.COLOR_CORRECTION_GAINS, colorTemperature(seekWb));*/

	}
}
