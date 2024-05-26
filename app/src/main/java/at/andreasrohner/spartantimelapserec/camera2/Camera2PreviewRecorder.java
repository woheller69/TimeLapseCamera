package at.andreasrohner.spartantimelapserec.camera2;

import android.content.Context;
import android.os.Handler;

/**
 * Handle Recording
 */
public class Camera2PreviewRecorder extends BaseRecorder {

	/**
	 * Activity
	 */
	private static Preview2Activity preview2Activity;

	/**
	 * Constructor
	 *
	 * @param context Context
	 * @param handler Handler
	 */
	public Camera2PreviewRecorder(Context context, Handler handler) {
		super(context, handler);
	}

	/**
	 * @param preview2Activity Activity
	 */
	public static void setPreview2Activity(Preview2Activity preview2Activity) {
		Camera2PreviewRecorder.preview2Activity = preview2Activity;
	}

	@Override
	protected void takePicture() {
		if (preview2Activity == null) {
			logger.error("Preview activity not set!");
			return;
		}

		preview2Activity.takePicture();
	}
}
