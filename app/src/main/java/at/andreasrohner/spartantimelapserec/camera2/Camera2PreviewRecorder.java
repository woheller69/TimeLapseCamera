package at.andreasrohner.spartantimelapserec.camera2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

import androidx.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.camera2.wrapper.ImageTakenListener;
import at.andreasrohner.spartantimelapserec.preference.PrefUtil;

/**
 * Handle Recording
 */
public class Camera2PreviewRecorder extends BaseRecorder implements FocusChangeListener {

	/**
	 * Activity
	 */
	private static Preview2Activity preview2Activity;

	/**
	 * Image ID
	 */
	private int imageId = 0;

	/**
	 * Refocus every X image (0=disabled)
	 */
	private int refocusEvery;

	/**
	 * Wait for focus result
	 */
	private int waitForImagesFocused = 0;

	/**
	 * Constructor
	 *
	 * @param context Context
	 * @param handler Handler
	 */
	public Camera2PreviewRecorder(Context context, Handler handler) {
		super(context, handler);
	}

	/**
	 * @param preview2Activity Activity
	 */
	public static void setPreview2Activity(Preview2Activity preview2Activity) {
		Camera2PreviewRecorder.preview2Activity = preview2Activity;
	}

	@Override
	public void start() {
		if (preview2Activity == null) {
			logger.error("Preview activity not set!");
			return;
		}

		super.start();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		refocusEvery = prefs.getInt("pref_camera_af_refocus", 1);

		PrefUtil.AfMode afMode = PrefUtil.getAfMode(prefs);
		if (afMode != PrefUtil.AfMode.FIELD) {
			refocusEvery = 0;
		}

		preview2Activity.enableRecordingMode();
		preview2Activity.setFocusChangedListener(this);
	}

	@Override
	public void stop() {
		super.stop();

		if (preview2Activity == null) {
			logger.error("Preview activity not set!");
			return;
		}

		preview2Activity.disableRecordingMode();
		preview2Activity.setFocusChangedListener(null);
	}

	@Override
	public void focusChanged(FocusState state) {
		if (waitForImagesFocused == 0) {
			return;
		}
		if (state == FocusState.FOCUS_SUCCESS || state == FocusState.FOCUS_FAILED) {
			waitForImagesFocused = 0;
			doTakeImage();
		}
	}

	@Override
	protected void takePicture() {
		if (preview2Activity == null) {
			logger.error("Preview activity not set!");
			return;
		}

		if (refocusEvery > 0 && imageId % refocusEvery == 0) {
			if (waitForImagesFocused > 0) {
				logger.warn("Missed image, focus not yet ready!");
			}
			waitForImagesFocused = 1;
			preview2Activity.focus();
		} else {
			doTakeImage();
		}
	}

	/**
	 * Call the activity to take an image
	 */
	private void doTakeImage() {
		preview2Activity.takePicture(new ImageTakenListener() {
			@Override
			public void takeImageFinished() {
				logger.info("Image taken");
				imageId++;

				preview2Activity.updateRecordingText(String.format(context.getString(R.string.recording_text_placeholder), imageId));
			}
		});
	}
}
