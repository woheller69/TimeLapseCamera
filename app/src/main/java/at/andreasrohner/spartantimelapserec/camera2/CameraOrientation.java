package at.andreasrohner.spartantimelapserec.camera2;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.WindowManager;

/**
 * Handle camera orientation
 */
public class CameraOrientation {

	/**
	 * Orientation list
	 */
	public static final SparseIntArray ORIENTATIONS = new SparseIntArray();

	static {
		ORIENTATIONS.append(Surface.ROTATION_0, 90);
		ORIENTATIONS.append(Surface.ROTATION_90, 0);
		ORIENTATIONS.append(Surface.ROTATION_180, 270);
		ORIENTATIONS.append(Surface.ROTATION_270, 180);
	}

	/**
	 * Context
	 */
	private final Context context;

	/**
	 * Constructor
	 *
	 * @param context Context
	 */
	public CameraOrientation(Context context) {
		this.context = context;
	}

	/**
	 * @return Rotation for JPEG Tag <code>CaptureRequest.JPEG_ORIENTATION</code>
	 */
	public int getRotation() {
		int rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
		return ORIENTATIONS.get(rotation);
	}
}
