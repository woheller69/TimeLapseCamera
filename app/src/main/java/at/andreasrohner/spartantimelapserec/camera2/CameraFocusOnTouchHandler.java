package at.andreasrohner.spartantimelapserec.camera2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
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
				editor.apply();
			}

			// the focus trigger is complete - resume repeating (preview surface will get frames), clear AF trigger
			previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, null);

			try {
				captureSession.setRepeatingRequest(previewRequestBuilder.build(), null, null);
			} catch (Exception e) {
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

		// The Camera does not support AF, no config possible
		if (!Camera2Utils.isAfSupported(cameraCharacteristics)) {
			return true;
		}

		if (manualFocusStarted) {
			logger.warn("Manual focus already started");
			return true;
		}

		processFocus(view, motionEvent);

		return true;
	}

	/**
	 * Load last focus configuration from settings
	 */
	public void loadLastFocusConfig() {
		String afMode = prefs.getString("pref_camera_af_mode", null);
		if (!"field".equals(afMode)) {
			// Just load the AF Field in the field mode
			return;
		}

		AfPos pos = AfPos.fromPref(prefs);
		if (pos == null) {
			// Error already logged
			return;
		}

		MeteringRectangle focusArea = pos.createMeteringRectangle();
		focusAtPosition(focusArea);
	}

	/**
	 * Focus at the position where the user touched the screen
	 *
	 * @param view        View
	 * @param motionEvent Motion Event
	 */
	private void processFocus(View view, MotionEvent motionEvent) {
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
			return;
		}

		float tpx = motionEvent.getX() - left;
		float tpy = motionEvent.getY() - top;

		this.relX = tpx / iw;
		this.relY = tpy / ih;

		final Rect sensorArraySize = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
		final int y = (int) (relX * (float) sensorArraySize.height());
		final int x = (int) (relY * (float) sensorArraySize.width());

		int focusSize = 50;
		MeteringRectangle focusArea = new MeteringRectangle(Math.max(x - focusSize, 0), Math.max(y - focusSize, 0), focusSize * 2, focusSize * 2, MeteringRectangle.METERING_WEIGHT_MAX - 1);

		storeAfPosition(sensorArraySize, focusArea);

		focusAtPosition(focusArea);
	}

	/**
	 * Focus at specific position
	 *
	 * @param focusArea Focus position
	 */
	private void focusAtPosition(MeteringRectangle focusArea) {
		stopRepeatingAndCancelAf();

		// Now add a new AF trigger with focus region
		previewRequestBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[] {focusArea});

		previewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
		previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
		previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);

		// we'll capture this later for resuming the preview
		previewRequestBuilder.setTag(REQUEST_TAG);

		// then we ask for a single request (not repeating!)
		try {
			captureSession.capture(previewRequestBuilder.build(), captureCallbackHandler, backgroundHandler);
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
		previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
		previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
		try {
			captureSession.capture(previewRequestBuilder.build(), captureCallbackHandler, backgroundHandler);
		} catch (Exception e) {
			logger.error("Start capture / cancel session failed!", e);
		}
	}

	/**
	 * Store AF Position, if not in auto mode
	 *
	 * @param sensorArraySize Sensor Size
	 * @param focusArea       Focus Area
	 */
	private void storeAfPosition(Rect sensorArraySize, MeteringRectangle focusArea) {
		String afMode = prefs.getString("pref_camera_af_mode", null);
		if (!"field".equals(afMode) && !"manual".equals(afMode)) {
			// The field is needed for 'field', but additionally also saved for 'manual', so the focus
			// position can be displayed in the preview, even if this value is not use. For 'manual'
			// the 'pref_camera_af_manual' is used.
			return;
		}

		SharedPreferences.Editor editor = prefs.edit();
		// Resolution / Position
		editor.putString("pref_camera_af_field", "Res:" + sensorArraySize.width() + "/" + sensorArraySize.height() + " Pos:" + focusArea.getX() + "," + focusArea.getY() + "," + focusArea.getWidth() + "," + focusArea.getHeight());
		logger.debug("Preview focus at {}", focusArea);
		editor.apply();
	}
}