package at.andreasrohner.spartantimelapserec.camera2.pupcfg;

import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;

import androidx.annotation.NonNull;
import at.andreasrohner.spartantimelapserec.R;

/**
 * Camera
 */
public class CameraModel implements IdData {

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
	public CameraModel(Context context, String cameraId, CameraCharacteristics characteristics) {
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

	@Override
	public String getId() {
		return cameraId;
	}

	@NonNull
	@Override
	public String toString() {
		return name;
	}
}