package at.andreasrohner.spartantimelapserec.camera2;

import android.content.SharedPreferences;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.MeteringRectangle;
import android.util.Size;

import at.andreasrohner.spartantimelapserec.state.Logger;

/**
 * Configure camera from android preferences
 */
public class ConfigureCamera2FromPrefs {

	/**
	 * Logger
	 */
	private Logger logger = new Logger(getClass());

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
	 * Current focus area
	 */
	private MeteringRectangle focusArea;

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
	 * @return Current focus area
	 */
	public MeteringRectangle getFocusArea() {
		return focusArea;
	}

	/**
	 * Configure Focus
	 *
	 * @param captureBuilder Camera Configuration
	 */
	private void configureFocus(CaptureRequest.Builder captureBuilder) {
		String afMode = prefs.getString("pref_camera_af_mode", "auto");

		if ("field".equals(afMode)) {
			applyAfField(captureBuilder);
		} else if ("manual".equals(afMode)) {
			captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
			float focusDistance = prefs.getFloat("pref_camera_af_manual", 0);
			captureBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, focusDistance);
		} else {
			captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
		}
	}

	/**
	 * Apply an AF Field
	 *
	 * @param captureBuilder Camera Configuration
	 */
	private void applyAfField(CaptureRequest.Builder captureBuilder) {
		AfPos pos = AfPos.fromPref(prefs);
		if (pos == null) {
			// Error already logged
			return;
		}

		prepareSize();

		if (!pos.equalsSize(sizeW, sizeH)) {
			logger.warn("Focus set at another resolution, currently not implemented, re-focus manually: {}!={} || {}!={}", pos.getWidth(), sizeW, pos.getHeigth(), sizeH);
			return;
		}

		this.focusArea = pos.createMeteringRectangle();
		logger.debug("Picture focus at {}", focusArea);
		captureBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
		captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
		captureBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[] {focusArea});
		// captureBuilder.set(CaptureRequest.CONTROL_AE_REGIONS, new MeteringRectangle[] {focusArea});
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
