package at.andreasrohner.spartantimelapserec.preference.preftype;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.CheckBox;
import android.widget.NumberPicker;

import at.andreasrohner.spartantimelapserec.R;

/**
 * Dialog to select a timespan
 */
public class TimeSpanDialogHour extends BaseTimeSpanDialog {

	/**
	 * Minutes
	 */
	protected NumberPicker minutesPicker;

	/**
	 * Hours
	 */
	protected NumberPicker hoursPicker;

	/**
	 * Infinite checkbox
	 */
	protected CheckBox infiniteCheckbox;

	/**
	 * Constructor
	 *
	 * @param context        Context
	 * @param prefs          Preferences
	 * @param key            Preferences key
	 * @param title          Title
	 * @param dialogMessage  Message to display
	 * @param changeListener Listener which gets called on change
	 */
	public TimeSpanDialogHour(Context context, SharedPreferences prefs, String key, CharSequence title, CharSequence dialogMessage, ChangeListener changeListener) {
		super(context, prefs, key, title, dialogMessage, changeListener);
	}

	@Override
	protected void initDialog() {
		view = View.inflate(context, R.layout.dialog_intervalpicker_h_preference, null);
		builder.setView(view);

		infiniteCheckbox = (CheckBox) view.findViewById(R.id.dialog_seekbar_preference_infinite);
		minutesPicker = (NumberPicker) view.findViewById(R.id.dialog_seekbar_preference_minutes);
		minutesPicker.setMinValue(0);
		minutesPicker.setMaxValue(59);

		hoursPicker = (NumberPicker) view.findViewById(R.id.dialog_seekbar_preference_hours);
		hoursPicker.setMinValue(0);
		hoursPicker.setMaxValue(47);

		loadData();
	}

	/**
	 * Load data
	 */
	protected void loadData() {
		boolean infinite = false;
		int min = 0;
		int hour = 0;
		int value = prefs.getInt(key, 2880);
		if (value >= 2880) {
			infinite = true;
			min = value % 60;
			hour = (value - (value % 60)) / 60;
		}

		infiniteCheckbox.setChecked(infinite);

		minutesPicker.setValue(min);

		hoursPicker.setValue(hour);
	}

	@Override
	protected void storeValue() {
		int value;
		if (infiniteCheckbox.isChecked()) {
			value = 2880;
		} else {
			value = minutesPicker.getValue() + hoursPicker.getValue() * 60;
		}
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(key, value);
		editor.apply();
	}
}
