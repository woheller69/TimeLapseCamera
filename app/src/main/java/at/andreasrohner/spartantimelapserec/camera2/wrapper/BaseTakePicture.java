package at.andreasrohner.spartantimelapserec.camera2.wrapper;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.util.Size;
import android.view.Surface;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.camera2.CameraOrientation;
import at.andreasrohner.spartantimelapserec.camera2.ConfigureCamera2FromPrefs;
import at.andreasrohner.spartantimelapserec.camera2.filename.AbstractFileNameController;
import at.andreasrohner.spartantimelapserec.state.Logger;

/**
 * Base class for image creation
 */
public abstract class BaseTakePicture implements ImageReader.OnImageAvailableListener {

	/**
	 * Global ID, incremented to get unique IDs
	 */
	private static AtomicInteger SID = new AtomicInteger(0);

	/**
	 * Logger
	 */
	protected Logger logger = new Logger(getClass());

	/**
	 * ID Of this picture instance, for logging
	 */
	protected final int id = SID.incrementAndGet();

	/**
	 * Camera implementation
	 */
	protected Camera2Wrapper camera;

	/**
	 * Background Thread to use to store the image
	 */
	protected final Handler backgroundHandler;

	/**
	 * Context
	 */
	protected final Context context;

	/**
	 * Interface to get notified when the image is taken
	 */
	protected ImageTakenListener imageTakenListener;

	/**
	 * Camera configuration
	 */
	protected ConfigureCamera2FromPrefs cameraConfig;

	/**
	 * Constructor
	 *
	 * @param camera            Camera
	 * @param backgroundHandler Background Thread to use to store the image
	 */
	public BaseTakePicture(Camera2Wrapper camera, Handler backgroundHandler) {
		this.camera = camera;
		this.backgroundHandler = backgroundHandler;
		this.context = camera.getContext();

		cameraConfig = new ConfigureCamera2FromPrefs(PreferenceManager.getDefaultSharedPreferences(context));
		logger.debug("New TakePicture instance #{}", id);
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
		logger.debug("TakePicture #{}", id);
		try {
			CameraDevice cameraDevice = camera.getCameraDevice();
			if (cameraDevice == null) {
				logger.warn("Cannot take image, camera not open (yet)!");
				return;
			}

			prepareImageReader(cameraDevice);

		} catch (Exception e) {
			camera.getErrorHandler().error("Failed to create picture", e);
		}
	}

	/**
	 * Prepare image reader
	 *
	 * @param cameraDevice Camera
	 * @throws CameraAccessException
	 */
	private void prepareImageReader(CameraDevice cameraDevice) throws CameraAccessException {
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

		takeImage(cameraDevice, captureBuilder, outputSurfaces);
	}

	/**
	 * Take the image
	 *
	 * @param cameraDevice   Camera Device
	 * @param captureBuilder Capture Builder
	 * @param outputSurfaces Output Surface
	 */
	protected abstract void takeImage(CameraDevice cameraDevice, CaptureRequest.Builder captureBuilder, List<Surface> outputSurfaces) throws CameraAccessException;

	@Override
	public void onImageAvailable(ImageReader reader) {
		try (Image image = reader.acquireLatestImage()) {
			ByteBuffer buffer = image.getPlanes()[0].getBuffer();
			byte[] bytes = new byte[buffer.capacity()];
			buffer.get(bytes);

			AbstractFileNameController fileNameController = camera.getFileNameController();

			try (AbstractFileNameController.ImageOutput output = fileNameController.getOutputFile("jpg")) {
				output.getOut().write(bytes);
				logger.debug("Picture #{} stored as «{}»", id, output.getName());
			}
		} catch (Exception e) {
			camera.getErrorHandler().error("Error saving image", e);
		}
	}
}
