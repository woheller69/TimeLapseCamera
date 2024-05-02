package at.andreasrohner.spartantimelapserec.camera2;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraCharacteristics;
import android.util.Range;
import android.widget.NumberPicker;

import androidx.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.rest.HttpThread;

/**
 * Exposure Time Popup Dialog
 */
public class PopupDialogExposureTime extends PopupDialogBase {

	/**
	 * Log Tag
	 */
	private static final String TAG = HttpThread.class.getSimpleName();

	/**
	 * Input field
	 */
	private final NumberPicker input;

	/**
	 * Camera timing values
	 */
	private CameraTiming timing;

	/**
	 * Constructor
	 *
	 * @param context Context
	 * @param camera  Camera
	 */
	public PopupDialogExposureTime(Context context, Camera2Wrapper camera) {
		super(context);
		timing = new CameraTiming(context);

		Range<Long> ranges = camera.getCharacteristics().get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
		// Nanoseconds, 1/1000000000s
		long upper = ranges.getUpper();
		long lower = ranges.getLower();

		timing.buildRangeSelection(lower, upper);

		this.input = (NumberPicker) view.findViewById(R.id.iso_input);
		input.setMinValue(0);
		input.setMaxValue(timing.getDisplayValues().size() - 1);
		input.setDisplayedValues(timing.getDisplayValues().toArray(new String[] {}));

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		long exposure = prefs.getLong("pref_camera_exposure", -1);
		int index = timing.getTimeValues().indexOf(exposure);

		if (index != -1) {
			input.setValue(index);
		} else {
			input.setValue(0);
		}
	}

	@Override
	protected int storeValue() {
		int index = input.getValue();
		long exposure = timing.getTimeValues().get(index);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putLong("pref_camera_exposure", exposure);
		editor.apply();
		return 0;
	}

	@Override
	public int getDialogId() {
		return R.layout.dialog_cam_selnumber;
	}

	@Override
	public int getTitleId() {
		return R.string.exposure_time;
	}

	@Override
	public int getMessageId() {
		return R.string.exposure_time_message;
	}
}
