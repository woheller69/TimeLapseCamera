package at.andreasrohner.spartantimelapserec.camera2;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.ImageRecorderState;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.ServiceHelper;
import at.andreasrohner.spartantimelapserec.camera2.filename.AbstractFileNameController;
import at.andreasrohner.spartantimelapserec.camera2.filename.ImageFile;
import at.andreasrohner.spartantimelapserec.camera2.wrapper.Camera2Wrapper;
import at.andreasrohner.spartantimelapserec.camera2.wrapper.CameraOpenCallback;
import at.andreasrohner.spartantimelapserec.camera2.wrapper.ImageTakenListener;
import at.andreasrohner.spartantimelapserec.state.Logger;

/**
 * Base class for Previews
 */
public abstract class AbstractPreview2Activity extends AppCompatActivity implements CameraOpenCallback, ImageTakenListener, ProcessErrorHandler, CameraControlButtonHandler.ConfigChangeListener {

	/**
	 * Logger
	 */
	protected Logger logger = new Logger(getClass());

	/**
	 * Log Tag
	 */
	private static final String TAG = AbstractPreview2Activity.class.getSimpleName();

	/**
	 * Texture view
	 */
	protected TextureView textureView;

	/**
	 * Camera implementation
	 */
	protected Camera2Wrapper camera;

	/**
	 * Capture session
	 */
	protected CameraCaptureSession cameraCaptureSession;

	/**
	 * Capture Request Builder
	 */
	protected CaptureRequest.Builder captureRequestBuilder;

	/**
	 * Image Reader
	 */
	protected ImageReader imageReader;

	/**
	 * Handler to store image
	 */
	protected Handler backgroundHandler;

	/**
	 * Thread to store image
	 */
	protected HandlerThread backgroundThread;

	/**
	 * Camera configuration
	 */
	protected ConfigureCamera2FromPrefs cameraConfig;

	/**
	 * Overlay
	 */
	protected PreviewOverlay overlay;

	/**
	 * Calculate the scaling for the preview
	 */
	protected PreviewScaling scaling = new PreviewScaling();

	/**
	 * Camera characteristics
	 */
	protected CameraCharacteristics characteristics;

	/**
	 * Constructor
	 */

	public AbstractPreview2Activity() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		loadContentView();

		textureView = (TextureView) findViewById(R.id.texture);
		overlay = (PreviewOverlay) findViewById(R.id.imageOverlay);
		overlay.setScaling(scaling);

		assert textureView != null;
		textureView.setSurfaceTextureListener(new PreviewSurfaceListener(() -> openCamera()));
	}

	/**
	 * Load content view
	 */
	protected abstract void loadContentView();

	/**
	 * Start the background thread
	 */
	protected void startBackgroundThread() {
		backgroundThread = new HandlerThread("Camera Background");
		backgroundThread.start();
		backgroundHandler = new Handler(backgroundThread.getLooper());
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
			Log.w(TAG, "Error stopping background Thread");
		}
		backgroundThread = null;
		backgroundHandler = null;
	}

	/**
	 * Take a picture
	 *
	 * @param listener Listener
	 */
	public void takePicture(ImageTakenListener listener) {
		if (camera == null) {
			logger.error("Camera is null");
			return;
		}

		if (!camera.isOpen()) {
			error("Camera is not open!", null);
			return;
		}

		textureView.setOnTouchListener(null);

		camera.takePicture(backgroundHandler, ImageTakenListener.combine(this, listener));
	}

	/**
	 * Image taken, start the preview again
	 */
	@Override
	public void takeImageFinished() {
		createCameraPreview();

		ImageFile img = ImageRecorderState.getCurrentRecordedImage();
		if (img != null) {
			Toast.makeText(getApplicationContext(), img.getName(), Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Create the preview
	 */
	public void createCameraPreview() {
		try {
			CameraDevice cameraDevice = camera.getCameraDevice();
			SurfaceTexture texture = textureView.getSurfaceTexture();
			assert texture != null;

			this.characteristics = camera.getCharacteristics();
			StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
			assert map != null;
			Size imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];

			transformTexture(imageDimension);
			texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
			Surface surface = new Surface(texture);

			logger.debug("createCaptureRequest for Preview");
			captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
			captureRequestBuilder.addTarget(surface);
			cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
				@Override
				public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
					//The camera is already closed
					if (null == cameraDevice) {
						return;
					}
					// When the session is ready, we start displaying the preview.
					AbstractPreview2Activity.this.cameraCaptureSession = cameraCaptureSession;
					updatePreview();

					onCameraConfigured();
				}

				@Override
				public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
					logger.error("Configuration failed");
					Toast.makeText(AbstractPreview2Activity.this, "Configuration failed", Toast.LENGTH_SHORT).show();
				}
			}, null);
		} catch (Exception e) {
			error("Create Preview failed", e);
		}
	}

	/**
	 * Camera configured
	 */
	protected void onCameraConfigured() {
		// Can be overwritten by extending classes
	}

	/**
	 * Update focus display
	 */
	protected void updateFocusDisplay(FocusChangeListener.FocusState state) {
		runOnUiThread(new Runnable() {
			public void run() {
				overlay.setFocusState(state);
				overlay.invalidate();
			}
		});
	}

	/**
	 * Set the correct transformation matrix
	 *
	 * @param imageDimension Image size
	 */
	private void transformTexture(Size imageDimension) {
		scaling.setTextureSize(textureView.getWidth(), textureView.getHeight());

		// Image is rotated by default, therefore here width/height is swapped!
		scaling.setImageSize(imageDimension.getHeight(), imageDimension.getWidth());

		Context context = getApplicationContext();
		scaling.setRotationEnum(((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation());

		scaling.calculate();

		textureView.setTransform(scaling.createMatrix());
		updateFocusDisplay(FocusChangeListener.FocusState.FOCUSSING);
	}

	/**
	 * Open the camera, if the camera is already open, close it first
	 */
	protected synchronized void openCamera() {
		if (camera != null) {
			camera.close();
		}

		closeCamera();

		camera = new Camera2Wrapper(this, AbstractFileNameController.createInstance(this), this);
		camera.setOpenCallback(this);
		camera.open();

		cameraConfig = new ConfigureCamera2FromPrefs(PreferenceManager.getDefaultSharedPreferences(this));
	}

	@Override
	public void cameraOpened(boolean success) {
		if (success) {
			createCameraPreview();
		}
	}

	@Override
	public void cameraConfigChanged(int flags) {
		updatePreview();
		if (flags == 1) {

			// Re-open Camera
			openCamera();
		}
	}

	/**
	 * Update the preview
	 */
	protected void updatePreview() {
		if (!camera.isOpen()) {
			error("Update preview failed!", null);
			return;
		}

		cameraConfig.config(captureRequestBuilder);

		try {
			cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler);
		} catch (Exception e) {
			error("Error configure camera", e);
		}
	}

	/**
	 * Close the camera
	 */
	private void closeCamera() {
		if (camera != null) {
			camera.close();
			camera = null;
		}
		if (null != imageReader) {
			imageReader.close();
			imageReader = null;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		ServiceHelper.setCurrentPreviewActivity(this);
		startBackgroundThread();
		if (textureView.isAvailable()) {
			openCamera();
		} else {
			textureView.setSurfaceTextureListener(new PreviewSurfaceListener(() -> openCamera()));
		}
	}

	@Override
	protected void onPause() {
		ServiceHelper.resetCurrentPreviewActivity(this);
		closeCamera();
		stopBackgroundThread();
		super.onPause();
	}

	@Override
	public void error(String msg, Exception e) {
		Log.e(TAG, msg, e);
		Toast.makeText(getApplicationContext(), "Error: " + msg, Toast.LENGTH_SHORT).show();
	}
}