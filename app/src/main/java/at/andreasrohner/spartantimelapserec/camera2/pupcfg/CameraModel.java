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
	 * Context
	 */
	private final Context context;

	/**
	 * CameraCharacteristics
	 */
	private final CameraCharacteristics characteristics;

	/**
	 * Constructor
	 *
	 * @param context         Context
	 * @param cameraId        Camera ID
	 * @param characteristics CameraCharacteristics
	 */
	public CameraModel(Context context, String cameraId, CameraCharacteristics characteristics) {
		this.context = context;
		this.cameraId = cameraId;
		this.characteristics = characteristics;
		Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);

		StringBuilder cam = new StringBuilder();
		cam.append(cameraId);
		cam.append(": ");
		if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
			cam.append(context.getString(R.string.pref_camera_front));
		} else {
			cam.append(context.getString(R.string.pref_camera_back));
		}

		buildLensList(cam);
		this.name = cam.toString();
	}

	/**
	 * Build the lens list
	 *
	 * @param cam [out] StringBuilder
	 */
	private void buildLensList(StringBuilder cam) {
		float[] focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
		if (focalLengths == null) {
			return;
		}

		if (focalLengths.length == 0) {
			return;
		}

		cam.append(' ');
		cam.append(context.getString(R.string.pref_camera_objective));
		cam.append(": ");

		boolean first = true;
		for (float fl : focalLengths) {
			if (first) {
				first = false;
			} else {
				cam.append(", ");
			}
			cam.append(focalLengths[0]);
			cam.append("mm");
		}
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