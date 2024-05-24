package at.andreasrohner.spartantimelapserec.camera2.wrapper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.Handler;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import androidx.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.camera2.Camera2Utils;
import at.andreasrohner.spartantimelapserec.camera2.ProcessErrorHandler;
import at.andreasrohner.spartantimelapserec.camera2.filename.AbstractFileNameController;
import at.andreasrohner.spartantimelapserec.state.Logger;

/**
 * Wraps the camera interface and configures the camera
 */
public class Camera2Wrapper {

	/**
	 * Logger
	 */
	private Logger logger = new Logger(getClass());

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
	private final AbstractFileNameController fileNameController;

	/**
	 * Log / Show error in timelapse recording
	 */
	private final ProcessErrorHandler errorHandler;

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
	 * A {@link Semaphore} to prevent the app from exiting before closing the camera.
	 */
	private Semaphore cameraOpenCloseLock = new Semaphore(1);

	/**
	 * State callback listener for camera
	 */
	private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
		@Override
		public void onOpened(CameraDevice camera) {
			logger.info("Camera Ready");
			cameraOpenCloseLock.release();
			cameraDevice = camera;
			if (openCallback != null) {
				openCallback.cameraOpened(true);
			}
		}

		@Override
		public void onDisconnected(CameraDevice camera) {
			cameraOpenCloseLock.release();
			camera.close();
			if (openCallback != null) {
				openCallback.cameraOpened(false);
			}
		}

		@Override
		public void onError(CameraDevice camera, int error) {
			cameraOpenCloseLock.release();
			camera.close();
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
	 * @param errorHandler       Log / Show error in timelapse recording
	 */
	public Camera2Wrapper(Context context, AbstractFileNameController fileNameController, ProcessErrorHandler errorHandler) {
		this.fileNameController = fileNameController;
		this.context = context;
		this.errorHandler = errorHandler;
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
	public AbstractFileNameController getFileNameController() {
		return fileNameController;
	}

	/**
	 * @return Log / Show error in timelapse recording
	 */
	public ProcessErrorHandler getErrorHandler() {
		return errorHandler;
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
		logger.info("Open camera");
		try {
			if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
				throw new RuntimeException("Time out waiting to lock camera opening.");
			}

			cameraId = prefs.getString("pref_camera", cameraManager.getCameraIdList()[0]);

			this.characteristics = cameraManager.getCameraCharacteristics(cameraId);
			cameraManager.openCamera(cameraId, stateCallback, null);
		} catch (Exception e) {
			errorHandler.error("Could not open Camera", e);
		}
	}

	/**
	 * Close the camera device
	 */
	public synchronized void close() {
		try {
			cameraOpenCloseLock.acquire();
			/* TODO !!!!!!!!!!!!!!!!!!!!!!!!!
			if (null != mCaptureSession) {
				mCaptureSession.close();
				mCaptureSession = null;
			}*/
			if (cameraDevice != null) {
				cameraDevice.close();
				cameraDevice = null;
			}
			/* TODO !!!!!!!!!!!!!!!!!!!!!!!!!
			if (null != mImageReader) {
				mImageReader.close();
				mImageReader = null;
			}*/
		} catch (InterruptedException e) {
			logger.error("Interrupted while trying to lock camera closing.", e);
		} finally {
			cameraOpenCloseLock.release();
		}
	}

	/**
	 * @return True if the camera is open
	 */
	public boolean isOpen() {
		return cameraDevice != null;
	}

	/**
	 * @return Camera Device
	 */
	public CameraDevice getCameraDevice() {
		return cameraDevice;
	}

	/**
	 * @return CameraCharacteristics
	 */
	public CameraCharacteristics getCharacteristics() {
		return characteristics;
	}

	/**
	 * Take an image without AF
	 *
	 * @param backgroundHandler  Background Thread to use to store the image
	 * @param imageTakenListener Interface to get notified when the image is taken
	 */
	public void takePicture(Handler backgroundHandler, ImageTakenListener imageTakenListener) {
		TakePicture picture = new TakePicture(this, backgroundHandler);
		picture.addImageTakenListener(imageTakenListener);
		takePicture(picture);
	}

	/**
	 * Take an image with AF
	 *
	 * @param backgroundHandler  Background Thread to use to store the image
	 * @param imageTakenListener Interface to get notified when the image is taken
	 */
	public void takePictureWithAf(Handler backgroundHandler, ImageTakenListener imageTakenListener) {
		TakePictureAf picture = new TakePictureAf(this, backgroundHandler);
		picture.addImageTakenListener(imageTakenListener);
		takePicture(picture);
	}

	/**
	 * Take a picture
	 *
	 * @param picture Picture handler
	 */
	private void takePicture(BaseTakePicture picture) {
		picture.create();
	}

	public boolean isAfSupported() {
		return Camera2Utils.isAfSupported(characteristics);
	}
}
