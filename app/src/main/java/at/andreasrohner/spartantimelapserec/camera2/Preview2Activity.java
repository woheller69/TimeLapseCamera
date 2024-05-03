package at.andreasrohner.spartantimelapserec.camera2;

import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
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
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.ImageRecorderState;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.rest.HttpThread;

/**
 * Preview with Camera 2 Activity
 */
public class Preview2Activity extends AppCompatActivity implements Camera2Wrapper.CameraOpenCallback, TakePicture.ImageTakenListener, ProcessErrorHandler, CameraControlButtonHandler.ConfigChangeListener {

	/**
	 * Log Tag
	 */
	private static final String TAG = HttpThread.class.getSimpleName();

	/**
	 * Texture view
	 */
	private TextureView textureView;

	/**
	 * Camera implementation
	 */
	private Camera2Wrapper camera;

	/**
	 * Capture session
	 */
	protected CameraCaptureSession cameraCaptureSessions;

	/**
	 * Capture Request Builder
	 */
	protected CaptureRequest.Builder captureRequestBuilder;

	/**
	 * Image Reader
	 */
	private ImageReader imageReader;

	/**
	 * Handler to store image
	 */
	private Handler backgroundHandler;

	/**
	 * Thread to store image
	 */
	private HandlerThread backgroundThread;

	/**
	 * Touch / focus handler
	 */
	private CameraFocusOnTouchHandler touchFocusHandler;

	/**
	 * Handle buttons and button actions of the camera preview
	 */
	private CameraControlButtonHandler cameraControlButtonHandler;

	/**
	 * Camera configuration
	 */
	private ConfigureCamera2FromPrefs cameraConfig;

	/**
	 * Constructor
	 */
	public Preview2Activity() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preview2);
		textureView = (TextureView) findViewById(R.id.texture);
		assert textureView != null;
		textureView.setSurfaceTextureListener(new PreviewSurfaceListener(() -> openCamera()));

		cameraControlButtonHandler = new CameraControlButtonHandler(this);
		cameraControlButtonHandler.setConfigChangeListener(this);

		((ImageButton) findViewById(R.id.btn_takepicture)).setOnClickListener(v -> takePicture());
	}

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
		} catch (InterruptedException e) {
			Log.w(TAG, "Error stopping background Thread");
		}
		backgroundThread = null;
		backgroundHandler = null;
	}

	protected void takePicture() {
		if (!camera.isOpen()) {
			error("Camera is not open!", null);
			return;
		}

		textureView.setOnTouchListener(null);
		camera.takePicture(backgroundHandler, this);
	}

	@Override
	public void takeImageFinished() {
		// Image taken, start the preview again

		// Attach the Listener again, which was removed until the picture was saved
		textureView.setOnTouchListener(touchFocusHandler);
		createCameraPreview();

		File img = ImageRecorderState.getCurrentRecordedImage();
		if (img != null) {
			Toast.makeText(getApplicationContext(), img.getName(), Toast.LENGTH_SHORT).show();
		}
	}

	public void createCameraPreview() {
		try {
			CameraDevice cameraDevice = camera.getCameraDevice();
			SurfaceTexture texture = textureView.getSurfaceTexture();
			assert texture != null;

			CameraCharacteristics characteristics = camera.getCharacteristics();
			StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
			assert map != null;
			Size imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
			texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
			Surface surface = new Surface(texture);
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
					cameraCaptureSessions = cameraCaptureSession;
					updatePreview();

					touchFocusHandler = new CameraFocusOnTouchHandler(characteristics, captureRequestBuilder, cameraCaptureSessions, backgroundHandler);
					textureView.setOnTouchListener(touchFocusHandler);
				}

				@Override
				public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
					Toast.makeText(Preview2Activity.this, "Configuration change", Toast.LENGTH_SHORT).show();
				}
			}, null);
		} catch (CameraAccessException e) {
			error("Create Preview failed", e);
		}
	}

	private synchronized void openCamera() {
		if (camera != null) {
			camera.close();
		}

		closeCamera();

		camera = new Camera2Wrapper(this, new FileNameController(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())), this);
		camera.setOpenCallback(this);
		camera.open();

		cameraControlButtonHandler.cameraOpened(camera);

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
			// TODO: May reset invalid camera values, they may out of range now, if the camera has different parameter

			// Re-open Camera
			openCamera();
		}
	}

	protected void updatePreview() {
		if (!camera.isOpen()) {
			error("Update preview failed!", null);
			return;
		}

		cameraConfig.config(captureRequestBuilder);

		try {
			cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler);
		} catch (CameraAccessException e) {
			error("Error configure camera", e);
		}
	}

	private void closeCamera() {
		if (camera != null) {
			camera.close();
		}
		if (null != imageReader) {
			imageReader.close();
			imageReader = null;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.e(TAG, "onResume");
		startBackgroundThread();
		if (textureView.isAvailable()) {
			openCamera();
		} else {
			textureView.setSurfaceTextureListener(new PreviewSurfaceListener(() -> openCamera()));
		}
	}

	@Override
	protected void onPause() {
		Log.e(TAG, "onPause");
		//closeCamera();
		stopBackgroundThread();
		super.onPause();
	}

	@Override
	public void error(String msg, Exception e) {
		Log.e(TAG, msg, e);
		Toast.makeText(getApplicationContext(), "Error: " + msg, Toast.LENGTH_SHORT).show();
	}
}