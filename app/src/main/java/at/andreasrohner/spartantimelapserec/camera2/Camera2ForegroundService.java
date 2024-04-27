package at.andreasrohner.spartantimelapserec.camera2;

import android.content.Context;

import at.andreasrohner.spartantimelapserec.BaseForegroundService;

/**
 * Service for Camea 2 Implementation
 */
public class Camera2ForegroundService extends BaseForegroundService {

	/**
	 * Recorder implementation
	 */
	private Camera2Recorder recorder;

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
		recorder = new Camera2Recorder(context, handler);
		handler.post(new Runnable() {
			@Override
			public void run() {
				recorder.start();
			}
		});
		updateNotification();
	}

	@Override
	protected void stopRecording() {
		if (recorder != null) {
			recorder.stop();
			recorder = null;
		}
	}
}

