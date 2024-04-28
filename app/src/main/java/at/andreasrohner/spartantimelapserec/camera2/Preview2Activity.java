package at.andreasrohner.spartantimelapserec.camera2;

import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.rest.HttpThread;

/**
 * Preview with Camera 2 Activity
 */
public class Preview2Activity extends AppCompatActivity implements CameraPreview, Camera2Wrapper.CameraOpenCallback, TakePicture.ImageTakenListener {

	/**
	 * Log Tag
	 */
	private static final String TAG = HttpThread.class.getSimpleName();

	private Button takePictureButton;

	private TextureView textureView;

	/**
	 * Camera implementation
	 */
	private Camera2Wrapper camera;

	protected CameraCaptureSession cameraCaptureSessions;

	protected CaptureRequest captureRequest;

	protected CaptureRequest.Builder captureRequestBuilder;

	private ImageReader imageReader;

	private File file;

	private boolean mFlashSupported;

	private Handler mBackgroundHandler;

	private HandlerThread mBackgroundThread;

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
		textureView.setSurfaceTextureListener(textureListener);

		takePictureButton = (Button) findViewById(R.id.btn_takepicture);
		assert takePictureButton != null;
		takePictureButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				takePicture();
			}
		});
	}

	TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
		@Override
		public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
			//open your camera here
			openCamera();
		}

		@Override
		public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
			// Transform you image captured size according to the surface width and height
		}

		@Override
		public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
			return false;
		}

		@Override
		public void onSurfaceTextureUpdated(SurfaceTexture surface) {
		}
	};

	final CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback() {
		@Override
		public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
			super.onCaptureCompleted(session, request, result);
			Toast.makeText(Preview2Activity.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
			createCameraPreview();
		}
	};

	protected void startBackgroundThread() {
		mBackgroundThread = new HandlerThread("Camera Background");
		mBackgroundThread.start();
		mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
	}

	protected void stopBackgroundThread() {
		mBackgroundThread.quitSafely();
		try {
			mBackgroundThread.join();
			mBackgroundThread = null;
			mBackgroundHandler = null;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected void takePicture() {
		if (!camera.isOpen()) {
			Log.e(TAG, "cameraDevice is not open");
			return;
		}

		TakePicture picture = new TakePicture(camera, mBackgroundHandler);
		picture.setImageTakenListener(this);
		picture.create();
	}

	@Override
	public void takeImageFinished() {
		// Image taken, start the preview again

		// TODO Focusing crashes on take image, needs to wait until the camera is ready again!
		createCameraPreview();
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
					textureView.setOnTouchListener(new CameraFocusOnTouchHandler(characteristics, captureRequestBuilder, cameraCaptureSessions, mBackgroundHandler));
				}

				@Override
				public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
					Toast.makeText(Preview2Activity.this, "Configuration change", Toast.LENGTH_SHORT).show();
				}
			}, null);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	private synchronized void openCamera() {
		if (camera != null) {
			camera.close();
		}

		closeCamera();

		camera = new Camera2Wrapper(this);
		camera.setOpenCallback(this);
		camera.open();
	}

	@Override
	public void cameraOpened(boolean success) {
		if (success) {
			createCameraPreview();
		}
	}

	protected void updatePreview() {
		if (!camera.isOpen()) {
			Log.e(TAG, "updatePreview error, return");
			return;
		}

		// TODO Configure camera here
		captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
		try {
			cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
		} catch (CameraAccessException e) {
			e.printStackTrace();
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
			textureView.setSurfaceTextureListener(textureListener);
		}
	}

	@Override
	protected void onPause() {
		Log.e(TAG, "onPause");
		//closeCamera();
		stopBackgroundThread();
		super.onPause();
	}
}