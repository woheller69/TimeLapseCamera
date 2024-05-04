package at.andreasrohner.spartantimelapserec.camera2;

import android.hardware.camera2.CameraCharacteristics;

/**
 * Helper for Camera 2
 */
public final class Camera2Utils {

	/**
	 * Utility class
	 */
	private Camera2Utils() {
	}

	/**
	 * Check if the camera supports AF
	 *
	 * @param characteristics CameraCharacteristics
	 * @return true if yes
	 */
	public static boolean isAfSupported(CameraCharacteristics characteristics) {
		Integer value = characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF);
		if (value != null) {
			return value >= 1;
		} else {
			return false;
		}
	}
}
