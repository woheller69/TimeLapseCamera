package at.andreasrohner.spartantimelapserec.camera2;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

/**
 * Sample from here: https://stackoverflow.com/questions/53324821/touch-based-focus-with-camera-api2-on-android
 */
public class CameraFocusOnTouchHandler implements View.OnTouchListener {

	private static final String TAG = "FocusOnTouchHandler";

	private CameraCharacteristics cameraCharacteristics;

	private CaptureRequest.Builder previewRequestBuilder;

	private CameraCaptureSession captureSession;

	private Handler backgroundHandler;

	private boolean mManualFocusEngaged = false;

	public CameraFocusOnTouchHandler(CameraCharacteristics cameraCharacteristics, CaptureRequest.Builder previewRequestBuilder, CameraCaptureSession captureSession, Handler backgroundHandler) {
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

		//Override in your touch-enabled view (this can be different than the view you use for displaying the cam preview)

		final int actionMasked = motionEvent.getActionMasked();
		if (actionMasked != MotionEvent.ACTION_DOWN) {
			return false;
		}
		if (mManualFocusEngaged) {
			Log.d(TAG, "Manual focus already engaged");
			return true;
		}

		final Rect sensorArraySize = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);

		//TODO: here I just flip x,y, but this needs to correspond with the sensor orientation (via SENSOR_ORIENTATION)
		final int y = (int) ((motionEvent.getX() / (float) view.getWidth()) * (float) sensorArraySize.height());
		final int x = (int) ((motionEvent.getY() / (float) view.getHeight()) * (float) sensorArraySize.width());
		final int halfTouchWidth = 50; //(int)motionEvent.getTouchMajor(); //TODO: this doesn't represent actual touch size in pixel. Values range in [3, 10]...
		final int halfTouchHeight = 50; //(int)motionEvent.getTouchMinor();
		MeteringRectangle focusAreaTouch = new MeteringRectangle(Math.max(x - halfTouchWidth, 0), Math.max(y - halfTouchHeight, 0), halfTouchWidth * 2, halfTouchHeight * 2, MeteringRectangle.METERING_WEIGHT_MAX - 1);

		CameraCaptureSession.CaptureCallback captureCallbackHandler = new CameraCaptureSession.CaptureCallback() {
			@Override
			public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
				super.onCaptureCompleted(session, request, result);
				mManualFocusEngaged = false;

				if (request.getTag() == "FOCUS_TAG") {
					//the focus trigger is complete - resume repeating (preview surface will get frames), clear AF trigger
					previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, null);
					try {
						captureSession.setRepeatingRequest(previewRequestBuilder.build(), null, null);
					} catch (CameraAccessException e) {
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
				super.onCaptureFailed(session, request, failure);
				Log.e(TAG, "Manual AF failure: " + failure);
				mManualFocusEngaged = false;
			}
		};

		//first stop the existing repeating request
		try {
			captureSession.stopRepeating();
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}

		//cancel any existing AF trigger (repeated touches, etc.)
		previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
		previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
		try {
			captureSession.capture(previewRequestBuilder.build(), captureCallbackHandler, backgroundHandler);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}

		//Now add a new AF trigger with focus region
		if (isMeteringAreaAFSupported()) {
			previewRequestBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[] {focusAreaTouch});
		}
		previewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
		previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
		previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
		previewRequestBuilder.setTag("FOCUS_TAG"); //we'll capture this later for resuming the preview

		//then we ask for a single request (not repeating!)
		try {
			captureSession.capture(previewRequestBuilder.build(), captureCallbackHandler, backgroundHandler);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
		mManualFocusEngaged = true;

		return true;
	}

	private boolean isMeteringAreaAFSupported() {
		Integer value = cameraCharacteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF);
		if (value != null) {
			return value >= 1;
		} else {
			return false;
		}
	}
}