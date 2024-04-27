package at.andreasrohner.spartantimelapserec;

import android.content.Context;

import at.andreasrohner.spartantimelapserec.data.RecSettingsLegacy;
import at.andreasrohner.spartantimelapserec.recorder.Recorder;

/**
 * Camera 1 implementaion
 */
public class Camera1ForegroundService extends BaseForegroundService {

	/**
	 * Recorder
	 */
	private Recorder recorder;

	/**
	 * Constructor
	 */
	public Camera1ForegroundService() {
	}

	@Override
	protected void startupService() {
		RecSettingsLegacy settings = new RecSettingsLegacy();
		settings.load(getApplicationContext());

		Context context = getApplicationContext();
		recorder = Recorder.getInstance(settings, context, handler, wakeLock, getOutputDir());

		handler.post(new Runnable() {
			@Override
			public void run() {
				recorder.start();
			}
		});
	}

	@Override
	protected void stopRecording() {
		if (recorder != null) {
			recorder.stop();
			recorder = null;
		}
	}
}

