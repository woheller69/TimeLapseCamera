package at.andreasrohner.spartantimelapserec.camera2;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.os.Handler;

import androidx.annotation.NonNull;
import at.andreasrohner.spartantimelapserec.state.Logger;

/**
 * Wraps the Camera2 instance to focus
 */
public class FocusHelper {

	/**
	 * Logger
	 */
	protected Logger logger = new Logger(getClass());

	/**
	 * Tag used for feedback
	 */
	private static String REQUEST_TAG = "FOCUS_TAG";

	/**
	 * Current camera session
	 */
	private final CameraCaptureSession captureSession;

	/**
	 * Request Builder
	 */
	private final CaptureRequest.Builder requestBuilder;

	/**
	 * Background Thread
	 */
	private final Handler backgroundHandler;

	/**
	 * Manual focus already started
	 */
	private boolean manualFocusStarted = false;

	/**
	 * Listener for focus changes
	 */
	private FocusChangeListener focusChangeListener;

	/**
	 * Callback
	 */
	private CameraCaptureSession.CaptureCallback captureCallbackHandler = new CameraCaptureSession.CaptureCallback() {
		@Override
		public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
			super.onCaptureCompleted(session, request, result);
			manualFocusStarted = false;

			if (!REQUEST_TAG.equals(request.getTag())) {
				return;
			}

			focusCompleted(result);

			// the focus trigger is complete - resume repeating (preview surface will get frames), clear AF trigger
			requestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, null);

			try {
				captureSession.setRepeatingRequest(requestBuilder.build(), null, null);
			} catch (Exception e) {
				logger.error("Error start repeating request after focus", e);
			}

			fireFocusState(FocusChangeListener.FocusState.FOCUS_SUCCESS);
		}

		@Override
		public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
			super.onCaptureFailed(session, request, failure);
			logger.error("Manual AF failure: «{}»", failure);
			manualFocusStarted = false;

			fireFocusState(FocusChangeListener.FocusState.FOCUS_FAILED);
		}
	};

	/**
	 * Constructor
	 *
	 * @param captureSession    Current camera session
	 * @param requestBuilder    Request Builder
	 * @param backgroundHandler Background Thread
	 */
	public FocusHelper(CameraCaptureSession captureSession, CaptureRequest.Builder requestBuilder, Handler backgroundHandler) {
		this.captureSession = captureSession;
		this.requestBuilder = requestBuilder;
		this.backgroundHandler = backgroundHandler;
	}

	/**
	 * Focus has been completed
	 *
	 * @param result Focus result
	 */
	protected void focusCompleted(TotalCaptureResult result) {
		// can be overwritten, noting to do here
	}

	/**
	 * @return Manual focus already started
	 */
	public boolean isManualFocusStarted() {
		return manualFocusStarted;
	}

	/**
	 * @param focusChangeListener Listener for focus changes
	 */
	public void setFocusChangeListener(FocusChangeListener focusChangeListener) {
		this.focusChangeListener = focusChangeListener;
	}

	/**
	 * Fire Focus state changed
	 *
	 * @param focusState Focus State
	 */
	private void fireFocusState(FocusChangeListener.FocusState focusState) {
		if (focusChangeListener == null) {
			return;
		}

		focusChangeListener.focusChanged(focusState);
	}

	/**
	 * Focus at specific position
	 *
	 * @param focusArea Focus position
	 */
	public void focusAtPosition(MeteringRectangle focusArea) {
		fireFocusState(FocusChangeListener.FocusState.FOCUSSING);

		stopRepeatingAndCancelAf();

		// Now add a new AF trigger with focus region
		requestBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[] {focusArea});

		requestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
		requestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
		requestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);

		// we'll capture this later for resuming the preview
		requestBuilder.setTag(REQUEST_TAG);

		// then we ask for a single request (not repeating!)
		try {
			captureSession.capture(requestBuilder.build(), captureCallbackHandler, backgroundHandler);
		} catch (Exception e) {
			logger.error("Start capture session failed!", e);
		}
		manualFocusStarted = true;
	}

	/**
	 * Stop already running repeating requests, and stop AF trigger
	 */
	private void stopRepeatingAndCancelAf() {
		// first stop the existing repeating request
		try {
			captureSession.stopRepeating();
		} catch (Exception e) {
			logger.error("Stop repeating for AF failed!", e);
		}

		// cancel any existing AF trigger (repeated touches, etc.)
		requestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
		requestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
		try {
			captureSession.capture(requestBuilder.build(), captureCallbackHandler, backgroundHandler);
		} catch (Exception e) {
			logger.error("Start capture / cancel session failed!", e);
		}
	}
}
