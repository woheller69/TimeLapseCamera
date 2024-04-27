package at.andreasrohner.spartantimelapserec.camera2;

import android.content.Context;

import at.andreasrohner.spartantimelapserec.BaseForegroundService;

public class Camera2ForegroundService extends BaseForegroundService {

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
		/*
		recorder = Recorder.getInstance(settings, context, handler, wakeLock);

		handler.post(new Runnable() {
			@Override
			public void run() {
				recorder.start();
			}
		});*/
		updateNotification();
	}

	@Override
	protected void stopRecording() {
		/*
		if (recorder != null) {
			recorder.stop();
			recorder = null;
		}*/
	}
}

