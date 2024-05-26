package at.andreasrohner.spartantimelapserec.camera2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.camera2.filename.AbstractFileNameController;
import at.andreasrohner.spartantimelapserec.camera2.wrapper.Camera2Wrapper;
import at.andreasrohner.spartantimelapserec.preference.PrefUtil;

/**
 * Handle Recording
 */
public class Camera2Recorder extends BaseRecorder {

	/**
	 * Camera wrapper
	 */
	private Camera2Wrapper camera;

	/**
	 * Handler to store image
	 */
	private Handler backgroundHandler;

	/**
	 * Thread to store image
	 */
	private HandlerThread backgroundThread;

	/**
	 * Constructor
	 *
	 * @param context Context
	 * @param handler Handler
	 */
	public Camera2Recorder(Context context, Handler handler) {
		super(context, handler);
	}

	@Override
	protected void initRecording() {
		backgroundThread = new HandlerThread("Picture Thread");
		backgroundThread.start();
		backgroundHandler = new Handler(backgroundThread.getLooper());

		camera = new Camera2Wrapper(context, AbstractFileNameController.createInstance(context), this);
		camera.open();
	}

	/**
	 * Take a picture
	 */
	@Override
	protected void takePicture() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		PrefUtil.AfMode afMode = PrefUtil.getAfMode(prefs);
		if (afMode == PrefUtil.AfMode.AUTO) {
			camera.takePicture(backgroundHandler, this);
		} else {
			camera.takePictureWithAf(backgroundHandler, this);
		}
	}

	/**
	 * Stop the background Thread
	 */
	protected synchronized void stopBackgroundThread() {
		if (backgroundThread == null) {
			return;
		}
		backgroundThread.quitSafely();
		try {
			backgroundThread.join();
		} catch (Exception e) {
			logger.error("Error joining background Thread", e);
		}

		backgroundThread = null;
		backgroundHandler = null;
	}

	/**
	 * Stop Recoding
	 */
	public void stop() {
		super.stop();
		stopBackgroundThread();
	}
}
