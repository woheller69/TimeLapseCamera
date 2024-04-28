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
import android.media.ImageReader;
import android.os.Handler;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import java.util.ArrayList;
import java.util.List;

import at.andreasrohner.spartantimelapserec.rest.HttpThread;

public class TakePicture {

	/**
	 * Log Tag
	 */
	private static final String TAG = HttpThread.class.getSimpleName();

	/**
	 * Camera implementation
	 */
	private Camera2Wrapper camera;

	private final CameraManager manager;

	private final TextureView textureView;

	private final Handler mBackgroundHandler;

	private final Context context;

	private final CameraPreview cameraPreview;

	public TakePicture(Camera2Wrapper camera, CameraManager manager, TextureView textureView, Handler mBackgroundHandler, Context context, CameraPreview cameraPreview) {
		this.camera = camera;
		this.manager = manager;
		this.textureView = textureView;
		this.mBackgroundHandler = mBackgroundHandler;
		this.context = context;
		this.cameraPreview = cameraPreview;
	}

	public void create() {
		try {
			CameraDevice cameraDevice = camera.getCameraDevice();
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
			List<Surface> outputSurfaces = new ArrayList<>(2);
			outputSurfaces.add(reader.getSurface());

			// TODO is this needed or is this just for preview?
			outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
			final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
			captureBuilder.addTarget(reader.getSurface());
			captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
			// Orientation
			CameraOrientation orientaion = new CameraOrientation(context);
			captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, orientaion.getRotation());
			reader.setOnImageAvailableListener(new ImageSaver(), mBackgroundHandler);
			final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
				@Override
				public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
					super.onCaptureCompleted(session, request, result);
					cameraPreview.createCameraPreview();
				}
			};
			cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
				@Override
				public void onConfigured(CameraCaptureSession session) {
					try {
						// TODO Configure Camera, here te picture is started
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
