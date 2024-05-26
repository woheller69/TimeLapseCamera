package at.andreasrohner.spartantimelapserec.camera2;

import android.content.Context;
import android.content.Intent;

import at.andreasrohner.spartantimelapserec.BaseForegroundService;

/**
 * Service for Camea 2 Implementation
 */
public class Camera2ForegroundService extends BaseForegroundService {

	/**
	 * Recorder implementation
	 */
	private BaseRecorder recorder;

	/**
	 * Recorder class
	 */
	private String recorderClass;

	/**
	 * Constructor
	 */
	public Camera2ForegroundService() {
	}

	@Override
	protected void startupService() {
		initWakeLock();
		initHandler();

		Context context = getApplicationContext();

		if (Camera2PreviewRecorder.class.getSimpleName().equals(recorderClass)) {
			recorder = new Camera2PreviewRecorder(context, handler);
		} else {
			recorder = new Camera2Recorder(context, handler);
		}

		handler.post(new Runnable() {
			@Override
			public void run() {
				recorder.start();
			}
		});
		updateNotification();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		recorderClass = intent.getStringExtra("recorder");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	protected void stopRecording() {
		if (recorder != null) {
			recorder.stop();
			recorder = null;
		}
	}
}

