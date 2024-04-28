package at.andreasrohner.spartantimelapserec.camera2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.util.Log;

import androidx.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.rest.HttpThread;

/**
 * Wrapps the camera interface and configures the camera
 */
public class Camera2Wrapper {

	/**
	 * Log Tag
	 */
	private static final String TAG = HttpThread.class.getSimpleName();

	/**
	 * Context
	 */
	private final Context context;

	/**
	 * Preferences
	 */
	private final SharedPreferences prefs;

	/**
	 * Camera Manager
	 */
	private final CameraManager cameraManager;

	/**
	 * Controller for output filenames
	 */
	private final FileNameController fileNameController;

	/**
	 * CameraCharacteristics
	 */
	private CameraCharacteristics characteristics;

	/**
	 * Camera ID
	 */
	private String cameraId;

	/**
	 * Camera Device
	 */
	protected CameraDevice cameraDevice;

	/**
	 * Callback to get informed when the camera is open
	 */
	private CameraOpenCallback openCallback;

	/**
	 * State callback listener for camera
	 */
	private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
		@Override
		public void onOpened(CameraDevice camera) {
			Log.e(TAG, "Camera Ready");
			cameraDevice = camera;
			if (openCallback != null) {
				openCallback.cameraOpened(true);
			}
		}

		@Override
		public void onDisconnected(CameraDevice camera) {
			close();
			if (openCallback != null) {
				openCallback.cameraOpened(false);
			}
		}

		@Override
		public void onError(CameraDevice camera, int error) {
			close();
			if (openCallback != null) {
				openCallback.cameraOpened(false);
			}
		}
	};

	/**
	 * Constructor
	 *
	 * @param context            Context
	 * @param fileNameController Controller for output filenames
	 */
	public Camera2Wrapper(Context context, FileNameController fileNameController) {
		this.fileNameController = fileNameController;
		this.context = context;
		this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
		this.cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
	}

	/**
	 * @return Context
	 */
	public Context getContext() {
		return context;
	}

	/**
	 * @return Controller for output filenames
	 */
	public FileNameController getFileNameController() {
		return fileNameController;
	}

	/**
	 * @param openCallback Callback to get informed when the camera is open
	 */
	public void setOpenCallback(CameraOpenCallback openCallback) {
		this.openCallback = openCallback;
	}

	/**
	 * Open camera
	 */
	@SuppressLint("MissingPermission") // permission is request in Main Activity
	public void open() {
		Log.i(TAG, "Open camera");
		try {
			cameraId = prefs.getString("pref_camera", cameraManager.getCameraIdList()[0]);

			this.characteristics = cameraManager.getCameraCharacteristics(cameraId);
			cameraManager.openCamera(cameraId, stateCallback, null);
		} catch (CameraAccessException e) {
			Log.e(TAG, "Could not open Camera", e);
			// TODO Error Handling
		}
	}

	public synchronized void close() {
		if (cameraDevice != null) {
			cameraDevice.close();
		}
		cameraDevice = null;
	}

	/**
	 * @return True if the camera is open
	 */
	public boolean isOpen() {
		return cameraDevice != null;
	}

	public CameraDevice getCameraDevice() {
		return cameraDevice;
	}

	public CameraCharacteristics getCharacteristics() {
		return characteristics;
	}

	/**
	 * Take an image
	 *
	 * @param backgroundHandler  Background Thread to use to store the image
	 * @param imageTakenListener Interface to get notified when the image is taken
	 */
	public void takePicture(Handler backgroundHandler, TakePicture.ImageTakenListener imageTakenListener) {
		TakePicture picture = new TakePicture(this, backgroundHandler);
		picture.setImageTakenListener(imageTakenListener);
		picture.create();
	}

	/**
	 * Callback to get informed when the camera is open
	 */
	public interface CameraOpenCallback {

		/**
		 * Gets called when the camea was opened
		 *
		 * @param success true on succes
		 */
		void cameraOpened(boolean success);
	}
}
