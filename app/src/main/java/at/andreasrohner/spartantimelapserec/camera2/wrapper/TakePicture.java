package at.andreasrohner.spartantimelapserec.camera2.wrapper;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Handler;
import android.view.Surface;

import java.util.List;

/**
 * Take a single image without additional AF handling
 */
public class TakePicture extends BaseTakePicture {

	/**
	 * Constructor
	 *
	 * @param camera            Camera
	 * @param backgroundHandler Background Thread to use to store the image
	 */
	public TakePicture(Camera2Wrapper camera, Handler backgroundHandler) {
		super(camera, backgroundHandler);
	}

	@Override
	protected void takeImage(CameraDevice cameraDevice, CaptureRequest.Builder captureBuilder, List<Surface> outputSurfaces) throws CameraAccessException {
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
				} catch (Exception e) {
					camera.getErrorHandler().error("Failed to configure camera", e);
				}
			}

			@Override
			public void onConfigureFailed(CameraCaptureSession session) {
				camera.getErrorHandler().error("Camera configuration failed", null);
			}
		}, backgroundHandler);
	}
}