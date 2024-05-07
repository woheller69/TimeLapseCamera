package at.andreasrohner.spartantimelapserec.camera2;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.util.Size;
import android.view.Surface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import androidx.preference.PreferenceManager;

public class TakePicture implements ImageReader.OnImageAvailableListener {

	/**
	 * Camera implementation
	 */
	private Camera2Wrapper camera;

	/**
	 * Background Thread to use to store the image
	 */
	private final Handler backgroundHandler;

	/**
	 * Context
	 */
	private final Context context;

	/**
	 * Interface to get notified when the image is taken
	 */
	private ImageTakenListener imageTakenListener;

	/**
	 * Camera configuration
	 */
	private ConfigureCamera2FromPrefs cameraConfig;

	/**
	 * Constructor
	 *
	 * @param camera            Camera
	 * @param backgroundHandler Background Thread to use to store the image
	 */
	public TakePicture(Camera2Wrapper camera, Handler backgroundHandler) {
		this.camera = camera;
		this.backgroundHandler = backgroundHandler;
		this.context = camera.getContext();

		cameraConfig = new ConfigureCamera2FromPrefs(PreferenceManager.getDefaultSharedPreferences(context));
	}

	/**
	 * @param imageTakenListener Interface to get notified when the image is taken
	 */
	public void setImageTakenListener(ImageTakenListener imageTakenListener) {
		this.imageTakenListener = imageTakenListener;
	}

	/**
	 * Create a picture
	 */
	public void create() {
		try {
			CameraDevice cameraDevice = camera.getCameraDevice();

			Size size = cameraConfig.prepareSize();
			ImageReader reader = ImageReader.newInstance(size.getWidth(), size.getHeight(), ImageFormat.JPEG, 1);
			List<Surface> outputSurfaces = new ArrayList<>(1);
			outputSurfaces.add(reader.getSurface());

			final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
			captureBuilder.addTarget(reader.getSurface());
			captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			String jpegOrientation = prefs.getString("jpeg_orientation", "SCREEN_ORIENTATION");
			switch (jpegOrientation) {
				case "NO_ORIENTATION":
					// Nothing to do
					break;

				case "PORTRAIT":
					captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, 90);
					break;
				case "PORTRAIT_FLIPPED":
					captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, 270);
					break;
				case "LANDSCAPE_LEFT":
					captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, 0);
					break;
				case "LANDSCAPE_RIGHT":
					captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, 180);
					break;

				case "SCREEN_ORIENTATION":
				default:
					CameraOrientation orientation = new CameraOrientation(context);
					captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, orientation.getRotation());
			}

			cameraConfig.config(captureBuilder);

			reader.setOnImageAvailableListener(this, backgroundHandler);
			final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
				@Override
				public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
					super.onCaptureCompleted(session, request, result);
					if (imageTakenListener != null) {
						imageTakenListener.takeImageFinished();
					}
				}
			};
			cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
				@Override
				public void onConfigured(CameraCaptureSession session) {
					try {
						session.capture(captureBuilder.build(), captureListener, backgroundHandler);
					} catch (CameraAccessException e) {
						camera.getErrorHandler().error("Failed to configure camera", e);
					}
				}

				@Override
				public void onConfigureFailed(CameraCaptureSession session) {
					camera.getErrorHandler().error("Camera configuration failed", null);
				}
			}, backgroundHandler);
		} catch (CameraAccessException e) {
			camera.getErrorHandler().error("Failed to create picture", e);
		}
	}

	@Override
	public void onImageAvailable(ImageReader reader) {
		try (Image image = reader.acquireLatestImage()) {
			ByteBuffer buffer = image.getPlanes()[0].getBuffer();
			byte[] bytes = new byte[buffer.capacity()];
			buffer.get(bytes);

			FileNameController fileNameController = camera.getFileNameController();
			final File file = fileNameController.getOutputFile("jpg");

			try (OutputStream output = new FileOutputStream(file)) {
				output.write(bytes);
			}
		} catch (IOException e) {
			camera.getErrorHandler().error("Error saving image", e);
		}
	}

	/**
	 * Interface to get notified when the image is taken
	 */
	public interface ImageTakenListener {

		/**
		 * The image is taken
		 */
		void takeImageFinished();
	}
}
