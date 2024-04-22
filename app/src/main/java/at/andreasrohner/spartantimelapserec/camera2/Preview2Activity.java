package at.andreasrohner.spartantimelapserec.camera2;

import android.Manifest;
import android.content.Context;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import at.andreasrohner.spartantimelapserec.R;

import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Preview with Camera 2 Activity
 */
public class Preview2Activity extends AppCompatActivity implements CameraPreview {

	private CameraCharacteristics characteristics;

	/**
	 * Constructor
	 */
	public Preview2Activity() {
	}

	private static final String TAG = "AndroidCameraApi";

	private Button takePictureButton;

	private TextureView textureView;

	public static final SparseIntArray ORIENTATIONS = new SparseIntArray();

	static {
		ORIENTATIONS.append(Surface.ROTATION_0, 90);
		ORIENTATIONS.append(Surface.ROTATION_90, 0);
		ORIENTATIONS.append(Surface.ROTATION_180, 270);
		ORIENTATIONS.append(Surface.ROTATION_270, 180);
	}

	private String cameraId;

	protected CameraDevice cameraDevice;

	protected CameraCaptureSession cameraCaptureSessions;

	protected CaptureRequest captureRequest;

	protected CaptureRequest.Builder captureRequestBuilder;

	private Size imageDimension;

	private ImageReader imageReader;

	private File file;

	private static final int REQUEST_CAMERA_PERMISSION = 200;

	private boolean mFlashSupported;

	private Handler mBackgroundHandler;

	private HandlerThread mBackgroundThread;

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

	private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
		@Override
		public void onOpened(CameraDevice camera) {
			//This is called when the camera is open
			Log.e(TAG, "onOpened");
			cameraDevice = camera;
			createCameraPreview();
		}

		@Override
		public void onDisconnected(CameraDevice camera) {
			cameraDevice.close();
		}

		@Override
		public void onError(CameraDevice camera, int error) {
			cameraDevice.close();
			cameraDevice = null;
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
		if (null == cameraDevice) {
			Log.e(TAG, "cameraDevice is null");
			return;
		}

		TakePicture picture = new TakePicture((CameraManager) getSystemService(Context.CAMERA_SERVICE), cameraDevice, textureView, getWindowManager(), mBackgroundHandler, this.getApplicationContext(), this);
		picture.create();
	}

	public void createCameraPreview() {
		try {
			SurfaceTexture texture = textureView.getSurfaceTexture();
			assert texture != null;
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

	private void openCamera() {
		CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
		Log.e(TAG, "is camera open");
		try {
			cameraId = manager.getCameraIdList()[0];
			this.characteristics = manager.getCameraCharacteristics(cameraId);
			StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
			assert map != null;
			imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
			// Add permission for camera and let user grant the permission

			// TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {  //WRITE_EXTERNAL_STORAGE is deprecated (and is not granted) when targeting Android 13+, in addition POST_NOTIFICATION is needed
				if ((ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && !shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) || (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && !shouldShowRequestPermissionRationale(android.Manifest.permission.RECORD_AUDIO)) || (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && !shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
					ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
				}
			} else {
				if ((ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && !shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) || (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && !shouldShowRequestPermissionRationale(android.Manifest.permission.RECORD_AUDIO)) || (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED && !shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS))) {
					ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO, Manifest.permission.POST_NOTIFICATIONS}, 123);
				}
			}
			manager.openCamera(cameraId, stateCallback, null);

		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
		Log.e(TAG, "openCamera X");
	}

	protected void updatePreview() {
		if (null == cameraDevice) {
			Log.e(TAG, "updatePreview error, return");
		}
		captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
		try {
			cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	private void closeCamera() {
		if (null != cameraDevice) {
			cameraDevice.close();
			cameraDevice = null;
		}
		if (null != imageReader) {
			imageReader.close();
			imageReader = null;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == REQUEST_CAMERA_PERMISSION) {
			if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
				// close the app
				Toast.makeText(Preview2Activity.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
				finish();
			}
		}

		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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