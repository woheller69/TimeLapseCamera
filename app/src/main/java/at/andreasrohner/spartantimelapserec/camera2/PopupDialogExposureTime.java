package at.andreasrohner.spartantimelapserec.camera2;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraCharacteristics;
import android.util.Log;
import android.util.Range;
import android.widget.NumberPicker;

import java.util.ArrayList;
import java.util.List;

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
	 * Predefined time values, copied from a Canon EOS Camera (a little extended)
	 */
	private static final String TIMES = "1/16000 1/8000 1/4000 1/3200 1/2500 1/2000 1/1600 1/1250 1/1000 1/800 1/640 1/500 1400 1/320 1/250 1/200 1/160 1/125 1/100 1/80 1/60 1/50 1/40 1/30 1/25 1/20 1/15 1/13 1/10 1/8 1/6 1/5 1/4 0\"3 0\"4 0\"5 0\"6 0\"8 1\" 1\"3 1\"6 2\" 2\"5 3\"2 4\" 5\" 6\" 8\" 10\" 13\" 15\" 20\" 25\" 30\"";

	/**
	 * Input field
	 */
	private final NumberPicker input;

	/**
	 * Display values
	 */
	private List<String> displayValues = new ArrayList<>();

	/**
	 * Time values in ns
	 */
	private List<Long> timeValues = new ArrayList<>();

	/**
	 * Constructor
	 *
	 * @param context Context
	 * @param camera  Camera
	 */
	public PopupDialogExposureTime(Context context, Camera2Wrapper camera) {
		super(context);

		displayValues.add(context.getString(R.string.camera_value_auto));
		timeValues.add(-1L);

		Range<Long> ranges = camera.getCharacteristics().get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
		// Nanoseconds, 1/1000000000s
		long upper = ranges.getUpper();
		long lower = ranges.getLower();

		buildRangeSelection(displayValues, lower, upper);

		this.input = (NumberPicker) view.findViewById(R.id.iso_input);
		input.setMinValue(0);
		input.setMaxValue(displayValues.size() - 1);
		input.setDisplayedValues(displayValues.toArray(new String[] {}));

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		long exposure = prefs.getLong("pref_camera_exposure", -1);
		int index = timeValues.indexOf(exposure);

		if (index != -1) {
			input.setValue(index);
		} else {
			input.setValue(0);
		}
	}

	/**
	 * Build exposure time selection
	 * <p>
	 * Example, Samsung Galaxy S8:
	 * min 60000/1000000000s => 6/100000s
	 * max 100000000/1000000000s => 1/10s
	 *
	 * @param displayValues [out] Values
	 * @param lower         Lower range
	 * @param upper         Upper range
	 */
	private void buildRangeSelection(List<String> displayValues, long lower, long upper) {
		for (String t : TIMES.split(" ")) {
			long ns = parseTime(t);
			if (ns < lower) {
				continue;
			}
			if (ns > upper) {
				break;
			}

			displayValues.add(t);
			timeValues.add(ns);
		}
	}

	/**
	 * Parse Time String
	 *
	 * @param t Time String
	 * @return Time in ns
	 */
	private long parseTime(String t) {
		if (t.startsWith("1/")) {
			long divisor = Long.parseLong(t.substring(2));
			return 1000000000L / divisor;
		}

		int pos = t.indexOf('"');
		if (pos != -1) {
			String s2 = t.substring(pos + 1);
			long t1 = Long.parseLong(t.substring(0, pos));
			if (s2.isEmpty()) {
				return 1000000000L * t1;
			}

			long t2 = Long.parseLong(s2);
			return 1000000000L * t1 + 1000000000L / t2;
		}

		Log.e(TAG, "Invalid predefined time value «" + t + "»");
		return -1;
	}

	@Override
	protected int storeValue() {
		int index = input.getValue();
		long exposure = timeValues.get(index);

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
