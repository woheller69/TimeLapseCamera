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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.rest.HttpThread;

/**
 * Focus touch handler
 */
public class CameraFocusOnTouchHandler implements View.OnTouchListener {

	/**
	 * Log Tag
	 */
	private static final String TAG = HttpThread.class.getSimpleName();

	/**
	 * Tag used for feedback
	 */
	private static String REQUEST_TAG = "FOCUS_TAG";

	/**
	 * Preferences
	 */
	private final SharedPreferences prefs;

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

			String afMode = prefs.getString("pref_camera_af_mode", null);

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
			} catch (CameraAccessException e) {
				Log.e(TAG, "Error start repeating request after focus", e);
			}
		}

		@Override
		public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
			super.onCaptureFailed(session, request, failure);
			Log.e(TAG, "Manual AF failure: " + failure);
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
	 */
	public CameraFocusOnTouchHandler(Context context, CameraCharacteristics cameraCharacteristics, CaptureRequest.Builder previewRequestBuilder, CameraCaptureSession captureSession, Handler backgroundHandler) {
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
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		final int actionMasked = motionEvent.getActionMasked();
		if (actionMasked != MotionEvent.ACTION_DOWN) {
			return false;
		}
		if (manualFocusStarted) {
			Log.w(TAG, "Manual focus already started");
			return true;
		}

		String afMode = prefs.getString("pref_camera_af_mode", null);

		final Rect sensorArraySize = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);

		//TODO: here I just flip x,y, but this needs to correspond with the sensor orientation (via SENSOR_ORIENTATION)
		final int y = (int) ((motionEvent.getX() / (float) view.getWidth()) * (float) sensorArraySize.height());
		final int x = (int) ((motionEvent.getY() / (float) view.getHeight()) * (float) sensorArraySize.width());
		final int halfTouchWidth = 50; //(int)motionEvent.getTouchMajor(); //TODO: this doesn't represent actual touch size in pixel. Values range in [3, 10]...
		final int halfTouchHeight = 50; //(int)motionEvent.getTouchMinor();
		MeteringRectangle focusAreaTouch = new MeteringRectangle(Math.max(x - halfTouchWidth, 0), Math.max(y - halfTouchHeight, 0), halfTouchWidth * 2, halfTouchHeight * 2, MeteringRectangle.METERING_WEIGHT_MAX - 1);

		// first stop the existing repeating request
		try {
			captureSession.stopRepeating();
		} catch (CameraAccessException e) {
			Log.e(TAG, "Stop repeating for AF failed!", e);
		}

		//cancel any existing AF trigger (repeated touches, etc.)
		previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
		previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
		try {
			captureSession.capture(previewRequestBuilder.build(), captureCallbackHandler, backgroundHandler);
		} catch (CameraAccessException e) {
			Log.e(TAG, "Start capture / cancel session failed!", e);
		}

		// Now add a new AF trigger with focus region
		if (Camera2Utils.isAfSupported(cameraCharacteristics)) {
			previewRequestBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[] {focusAreaTouch});
			if ("field".equals(afMode)) {
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("pref_camera_af_field", focusAreaTouch.toString());
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
			Log.e(TAG, "Start capture session failed!", e);
		}
		manualFocusStarted = true;

		return true;
	}
}