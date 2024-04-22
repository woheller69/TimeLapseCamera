package at.andreasrohner.spartantimelapserec.camera2;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.os.Handler;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class TakePicture {

	private final CameraManager manager;

	private final CameraDevice cameraDevice;

	private final TextureView textureView;

	private final WindowManager windowManager;

	private final Handler mBackgroundHandler;

	private final Context context;

	private final CameraPreview cameraPreview;

	public TakePicture(CameraManager manager, CameraDevice cameraDevice, TextureView textureView, WindowManager windowManager, Handler mBackgroundHandler, Context context, CameraPreview cameraPreview) {
		this.manager = manager;
		this.cameraDevice = cameraDevice;
		this.textureView = textureView;
		this.windowManager = windowManager;
		this.mBackgroundHandler = mBackgroundHandler;
		this.context = context;
		this.cameraPreview = cameraPreview;
	}

	public void create() {
		try {
			CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
			Size[] jpegSizes = null;
			if (characteristics != null) {
				jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
			}
			int width = 640;
			int height = 480;
			if (jpegSizes != null && 0 < jpegSizes.length) {
				width = jpegSizes[0].getWidth();
				height = jpegSizes[0].getHeight();
			}
			ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
			List<Surface> outputSurfaces = new ArrayList<Surface>(2);
			outputSurfaces.add(reader.getSurface());
			outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
			final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
			captureBuilder.addTarget(reader.getSurface());
			captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
			// Orientation
			int rotation = windowManager.getDefaultDisplay().getRotation();
			captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, Preview2Activity.ORIENTATIONS.get(rotation));
			final File file = new File(Environment.getExternalStorageDirectory() + "/pic.jpg");
			ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
				@Override
				public void onImageAvailable(ImageReader reader) {
					Image image = null;
					try {
						image = reader.acquireLatestImage();
						ByteBuffer buffer = image.getPlanes()[0].getBuffer();
						byte[] bytes = new byte[buffer.capacity()];
						buffer.get(bytes);
						save(bytes);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						if (image != null) {
							image.close();
						}
					}
				}

				private void save(byte[] bytes) throws IOException {
					OutputStream output = null;
					try {
						output = new FileOutputStream(file);
						output.write(bytes);
					} finally {
						if (null != output) {
							output.close();
						}
					}
				}
			};
			reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
			final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
				@Override
				public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
					super.onCaptureCompleted(session, request, result);
					Toast.makeText(context, "Saved:" + file, Toast.LENGTH_SHORT).show();
					cameraPreview.createCameraPreview();
				}
			};
			cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
				@Override
				public void onConfigured(CameraCaptureSession session) {
					try {
						session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
					} catch (CameraAccessException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void onConfigureFailed(CameraCaptureSession session) {
				}
			}, mBackgroundHandler);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}
}
