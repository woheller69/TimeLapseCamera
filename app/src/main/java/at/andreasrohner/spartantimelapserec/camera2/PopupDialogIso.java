package at.andreasrohner.spartantimelapserec.camera2;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraCharacteristics;
import android.util.Range;
import android.widget.ImageButton;
import android.widget.NumberPicker;

import java.util.ArrayList;
import java.util.List;

import androidx.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.R;

/**
 * ISO Popup Dialog
 */
public class PopupDialogIso extends PopupDialogBase {

	/**
	 * Input field
	 */
	private final NumberPicker input;

	/**
	 * Display values
	 */
	private List<String> displayValues = new ArrayList<>();

	/**
	 * Constructor
	 *
	 * @param context Context
	 * @param button  Button
	 * @param camera  Camera
	 */
	public PopupDialogIso(Context context, ImageButton button, Camera2Wrapper camera) {
		super(context, button);

		displayValues.add(context.getString(R.string.iso_auto));

		Range<Integer> ranges = camera.getCharacteristics().get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
		int upper = ranges.getUpper();
		int lower = ranges.getLower();

		for (int i = lower; i < upper; i += lower) {
			displayValues.add(String.valueOf(i));
		}
		String highestValue = String.valueOf(upper);
		if (!displayValues.contains(highestValue)) {
			displayValues.add(highestValue);
		}

		this.input = (NumberPicker) view.findViewById(R.id.iso_input);
		input.setMinValue(0);
		input.setMaxValue(displayValues.size() - 1);
		input.setDisplayedValues(displayValues.toArray(new String[] {}));

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		int iso = prefs.getInt("pref_camera_iso", -1);
		if (iso != -1) {
			String isoString = String.valueOf(iso);
			int index = displayValues.indexOf(isoString);
			if (index != -1) {
				input.setValue(index);
			}
		} else {
			input.setValue(0);
		}
	}

	@Override
	protected void storeValue() {
		int iso;
		int index = input.getValue();
		if (index == 0) {
			// Auto
			iso = -1;
		} else {
			iso = Integer.parseInt(displayValues.get(index));
		}

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("pref_camera_iso", iso);
		editor.apply();
	}

	@Override
	public int getDialogId() {
		return R.layout.dialog_cam_iso;
	}

	@Override
	public int getTitleId() {
		return R.string.iso;
	}

	@Override
	public int getMessageId() {
		return R.string.iso_message;
	}
}
