package at.andreasrohner.spartantimelapserec.camera2;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.util.Size;
import android.view.Surface;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.camera2.filename.AbstractFileNameController;
import at.andreasrohner.spartantimelapserec.state.Logger;

/**
 * Implementation to take a single picture
 */
public class TakePicture implements ImageReader.OnImageAvailableListener {

	/**
	 * Global ID, incremented to get unique IDs
	 */
	private static AtomicInteger SID = new AtomicInteger(0);

	/**
	 * Logger
	 */
	private Logger logger = new Logger(getClass());

	/**
	 * ID Of this picture instance, for logging
	 */
	private final int id = SID.incrementAndGet();

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
	 * Capture Request
	 */
	private CaptureRequest.Builder captureBuilder;

	/**
	 * Camera Session
	 */
	private CameraCaptureSession session;

	private enum State {
		/**
		 * Camera state: Showing camera preview.
		 */
		STATE_PREVIEW,

		/**
		 * Camera state: Waiting for the focus to be locked.
		 */
		STATE_WAITING_LOCK,

		/**
		 * Camera state: Waiting for the exposure to be precapture state.
		 */
		STATE_WAITING_PRECAPTURE,

		/**
		 * Camera state: Waiting for the exposure state to be something other than precapture.
		 */
		STATE_WAITING_NON_PRECAPTURE,

		/**
		 * Camera state: Picture was taken.
		 */
		STATE_PICTURE_TAKEN,

		/**
		 * Take image ended
		 */
		STATE_ENDED
	}

	/**
	 * The current state of camera state for taking pictures.
	 *
	 * @see #mCaptureCallback
	 */
	private State currentState = State.STATE_PREVIEW;

	/**
	 * A {@link CameraCaptureSession.CaptureCallback} that handles events related to JPEG capture.
	 */
	private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {

		private void process(CaptureResult result) {
			logger.debug("TakePicture #{} process state {}", id, currentState);

			switch (currentState) {
				case STATE_PREVIEW: {
					// We have nothing to do when the camera preview is working normally.
					break;
				}
				case STATE_WAITING_LOCK: {
					Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
					if (afState == null) {
						captureStillPicture();
					} else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
						// CONTROL_AE_STATE can be null on some devices
						Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
						if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
							setCurrentState(State.STATE_PICTURE_TAKEN);
							captureStillPicture();
						} else {
							runPrecaptureSequence();
						}
					}
					break;
				}
				case STATE_WAITING_PRECAPTURE: {
					// CONTROL_AE_STATE can be null on some devices
					Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
					if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE || aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
						setCurrentState(State.STATE_WAITING_NON_PRECAPTURE);
					}
					break;
				}
				case STATE_WAITING_NON_PRECAPTURE: {
					// CONTROL_AE_STATE can be null on some devices
					Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
					if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
						setCurrentState(State.STATE_PICTURE_TAKEN);
						captureStillPicture();
					}
					break;
				}
			}
		}

		@Override
		public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
			process(partialResult);
		}

		@Override
		public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
			process(result);
		}

	};

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

		logger.debug("New TakePicture instance #{}", id);
	}

	/**
	 * Set the state
	 *
	 * @param currentState State
	 */
	private void setCurrentState(State currentState) {
		// logger.debug("TakePicture #{} new state {}", id, currentState);
		this.currentState = currentState;
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

			Size size = cameraConfig.prepareSize();
			ImageReader reader = ImageReader.newInstance(size.getWidth(), size.getHeight(), ImageFormat.JPEG, 1);
			List<Surface> outputSurfaces = new ArrayList<>(1);
			outputSurfaces.add(reader.getSurface());

			this.captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
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

			cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {

				@Override
				public void onConfigured(CameraCaptureSession session) {
					TakePicture.this.session = session;
					lockFocus();
				}

				@Override
				public void onConfigureFailed(CameraCaptureSession session) {
					camera.getErrorHandler().error("Camera configuration failed", null);
				}
			}, backgroundHandler);
		} catch (Exception e) {
			camera.getErrorHandler().error("Failed to create picture", e);
		}
	}

	/**
	 * Lock the focus as the first step for a still image capture.
	 */
	private void lockFocus() {
		try {
			// This is how to tell the camera to lock focus.
			captureBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
			// Tell #mCaptureCallback to wait for the lock.
			setCurrentState(State.STATE_WAITING_LOCK);
			session.capture(captureBuilder.build(), mCaptureCallback, backgroundHandler);
		} catch (Exception e) {
			camera.getErrorHandler().error("Failed to lock focus", e);
		}
	}

	/**
	 * Run the precapture sequence for capturing a still image. This method should be called when
	 * we get a response in {@link #mCaptureCallback} from {@link #lockFocus()}.
	 */
	private void runPrecaptureSequence() {
		try {
			// This is how to tell the camera to trigger.
			captureBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
			// Tell #mCaptureCallback to wait for the precapture sequence to be set.
			setCurrentState(State.STATE_WAITING_PRECAPTURE);
			session.capture(captureBuilder.build(), mCaptureCallback, backgroundHandler);
		} catch (Exception e) {
			camera.getErrorHandler().error("Failed to run precapture sequence", e);
		}
	}

	/**
	 * Capture a still picture. This method should be called when we get a response in
	 * {@link #mCaptureCallback} from both {@link #lockFocus()}.
	 */
	private void captureStillPicture() {
		try {
			captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

			CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {

				@Override
				public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
					logger.info("Image taken");
					if (imageTakenListener != null) {
						imageTakenListener.takeImageFinished();
					}
					setCurrentState(State.STATE_ENDED);
					try {
						session.stopRepeating();
						session.abortCaptures();
					} catch (CameraAccessException e) {
						camera.getErrorHandler().error("Cleanup camera failed", e);
					}
				}
			};

			session.stopRepeating();
			session.abortCaptures();
			session.capture(captureBuilder.build(), CaptureCallback, null);
		} catch (Exception e) {
			camera.getErrorHandler().error("Failed to take image", e);
		}
	}

	@Override
	public void onImageAvailable(ImageReader reader) {
		if (currentState != State.STATE_PICTURE_TAKEN) {
			return;
		}

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
