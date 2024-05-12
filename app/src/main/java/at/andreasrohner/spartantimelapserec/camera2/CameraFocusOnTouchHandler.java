package at.andreasrohner.spartantimelapserec.camera2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.state.Logger;

/**
 * Focus touch handler
 */
public class CameraFocusOnTouchHandler implements View.OnTouchListener {

	/**
	 * Logger
	 */
	private Logger logger = new Logger(getClass());

	/**
	 * Tag used for feedback
	 */
	private static String REQUEST_TAG = "FOCUS_TAG";

	/**
	 * Preferences
	 */
	private final SharedPreferences prefs;

	/**
	 * PreviewScaling
	 */
	private final PreviewScaling scaling;

	/**
	 * CameraCharacteristics
	 */
	private CameraCharacteristics cameraCharacteristics;

	/**
	 * Preview Request Builder
	 */
	private CaptureRequest.Builder previewRequestBuilder;

	/**
	 * CameraCaptureSession
	 */
	private CameraCaptureSession captureSession;

	/**
	 * Background Thread
	 */
	private Handler backgroundHandler;

	/**
	 * Manual focus already started
	 */
	private boolean manualFocusStarted = false;

	/**
	 * Listener for focus changes
	 */
	private FocusChangeListener focusChangeListener;

	/**
	 * Image Relative X
	 */
	private float relX;

	/**
	 * Image Relative Y
	 */
	private float relY;

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

			String afMode = prefs.getString("pref_camera_af_mode", "auto");

			if ("manual".equals(afMode)) {
				float focusDistance = result.get(CaptureResult.LENS_FOCUS_DISTANCE);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putFloat("pref_camera_af_manual", focusDistance);
				editor.putString("pref_camera_af_field", relX + "/" + relY);
				editor.apply();
			}

			// the focus trigger is complete - resume repeating (preview surface will get frames), clear AF trigger
			previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, null);

			try {
				captureSession.setRepeatingRequest(previewRequestBuilder.build(), null, null);
			} catch (CameraAccessException e) {
				logger.error("Error start repeating request after focus", e);
			}

			if (focusChangeListener != null) {
				focusChangeListener.focusChanged();
			}
		}

		@Override
		public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
			super.onCaptureFailed(session, request, failure);
			logger.error("Manual AF failure: «{}»", failure);
			manualFocusStarted = false;
		}
	};

	/**
	 * Constructor
	 *
	 * @param context               Context
	 * @param cameraCharacteristics CameraCharacteristics
	 * @param previewRequestBuilder Preview Request Builder
	 * @param captureSession        CameraCaptureSession
	 * @param backgroundHandler     Background Thread
	 * @param scaling               PreviewScaling
	 */
	public CameraFocusOnTouchHandler(Context context, CameraCharacteristics cameraCharacteristics, CaptureRequest.Builder previewRequestBuilder, CameraCaptureSession captureSession, Handler backgroundHandler, PreviewScaling scaling) {
		this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
		this.cameraCharacteristics = cameraCharacteristics;
		if (cameraCharacteristics == null) {
			throw new IllegalArgumentException("cameraCharacteristics == null");
		}
		this.previewRequestBuilder = previewRequestBuilder;
		if (previewRequestBuilder == null) {
			throw new IllegalArgumentException("previewRequestBuilder == null");
		}
		this.captureSession = captureSession;
		if (captureSession == null) {
			throw new IllegalArgumentException("captureSession == null");
		}
		this.backgroundHandler = backgroundHandler;
		if (backgroundHandler == null) {
			throw new IllegalArgumentException("backgroundHandler == null");
		}

		this.scaling = scaling;
	}

	/**
	 * @param focusChangeListener Listener for focus changes
	 */
	public void setFocusChangeListener(FocusChangeListener focusChangeListener) {
		this.focusChangeListener = focusChangeListener;
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		final int actionMasked = motionEvent.getActionMasked();
		if (actionMasked != MotionEvent.ACTION_DOWN) {
			return false;
		}
		if (manualFocusStarted) {
			logger.warn("Manual focus already started");
			return true;
		}

		String afMode = prefs.getString("pref_camera_af_mode", null);

		final Rect sensorArraySize = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);

		float sx = scaling.getScaleX();
		float sy = scaling.getScaleY();
		int vw = view.getWidth();
		int vh = view.getHeight();
		int iw = (int) (vw * sx);
		int ih = (int) (vh * sy);
		int left = (vw - iw) / 2;
		int top = (vh - ih) / 2;

		Rect boundingBox = new Rect(left, top, iw + left, ih + top);
		if (!boundingBox.contains((int) motionEvent.getX(), (int) motionEvent.getY())) {
			// Touch not within image area
			return true;
		}

		float tpx = motionEvent.getX() - left;
		float tpy = motionEvent.getY() - top;

		this.relX = tpx / iw;
		this.relY = tpy / ih;

		final int y = (int) (relX * (float) sensorArraySize.height());
		final int x = (int) (relY * (float) sensorArraySize.width());

		int focusSize = 50;
		MeteringRectangle focusArea = new MeteringRectangle(Math.max(x - focusSize, 0), Math.max(y - focusSize, 0), focusSize * 2, focusSize * 2, MeteringRectangle.METERING_WEIGHT_MAX - 1);

		// first stop the existing repeating request
		try {
			captureSession.stopRepeating();
		} catch (CameraAccessException e) {
			logger.error("Stop repeating for AF failed!", e);
		}

		//cancel any existing AF trigger (repeated touches, etc.)
		previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
		previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
		try {
			captureSession.capture(previewRequestBuilder.build(), captureCallbackHandler, backgroundHandler);
		} catch (CameraAccessException e) {
			logger.error("Start capture / cancel session failed!", e);
		}

		// Now add a new AF trigger with focus region
		if (Camera2Utils.isAfSupported(cameraCharacteristics)) {
			previewRequestBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[] {focusArea});
			if ("field".equals(afMode)) {
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("pref_camera_af_field", relX + "/" + relY);
				editor.apply();
			}
		}
		previewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
		previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
		previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
		previewRequestBuilder.setTag(REQUEST_TAG); //we'll capture this later for resuming the preview

		// then we ask for a single request (not repeating!)
		try {
			captureSession.capture(previewRequestBuilder.build(), captureCallbackHandler, backgroundHandler);
		} catch (CameraAccessException e) {
			logger.error("Start capture session failed!", e);
		}
		manualFocusStarted = true;

		return true;
	}

	/**
	 * Listener for focus changes
	 */
	public interface FocusChangeListener {

		/**
		 * Focus has been changed
		 */
		void focusChanged();
	}
}