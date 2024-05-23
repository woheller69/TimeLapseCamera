package at.andreasrohner.spartantimelapserec.camera2.wrapper;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.ImageReader;
import android.os.Handler;
import android.view.Surface;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * Create a picture with autofocus handling
 * <p>
 * Example: https://raw.githubusercontent.com/googlearchive/android-Camera2Basic/master/Application/src/main/java/com/example/android/camera2basic/Camera2BasicFragment.java
 */
public class TakePictureAf extends BaseTakePicture {

	/**
	 * Camera Session
	 */
	private CameraCaptureSession session;

	/**
	 * The current state of camera state for taking pictures.
	 *
	 * @see #mCaptureCallback
	 */
	private CameraAfState currentState = CameraAfState.STATE_PREVIEW;

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
							setCurrentState(CameraAfState.STATE_PICTURE_TAKEN);
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
						setCurrentState(CameraAfState.STATE_WAITING_NON_PRECAPTURE);
					}
					break;
				}
				case STATE_WAITING_NON_PRECAPTURE: {
					// CONTROL_AE_STATE can be null on some devices
					Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
					if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
						setCurrentState(CameraAfState.STATE_PICTURE_TAKEN);
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
	public TakePictureAf(Camera2Wrapper camera, Handler backgroundHandler) {
		super(camera, backgroundHandler);
	}

	/**
	 * Set the state
	 *
	 * @param currentState State
	 */
	private void setCurrentState(CameraAfState currentState) {
		// logger.debug("TakePicture #{} new state {}", id, currentState);
		this.currentState = currentState;
	}

	@Override
	protected void takeImage(CameraDevice cameraDevice, List<Surface> outputSurfaces) throws CameraAccessException {
		cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {

			@Override
			public void onConfigured(CameraCaptureSession session) {
				TakePictureAf.this.session = session;
				lockFocus();
			}

			@Override
			public void onConfigureFailed(CameraCaptureSession session) {
				camera.getErrorHandler().error("Camera configuration failed", null);
			}
		}, backgroundHandler);
	}

	/**
	 * Lock the focus as the first step for a still image capture.
	 */
	private void lockFocus() {
		try {
			// This is how to tell the camera to lock focus.
			captureBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
			// Tell #mCaptureCallback to wait for the lock.
			setCurrentState(CameraAfState.STATE_WAITING_LOCK);
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
			setCurrentState(CameraAfState.STATE_WAITING_PRECAPTURE);
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
					setCurrentState(CameraAfState.STATE_ENDED);
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
		if (currentState != CameraAfState.STATE_PICTURE_TAKEN) {
			return;
		}

		super.onImageAvailable(reader);
	}
}
