package at.andreasrohner.spartantimelapserec.preference.preftype;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.NumberPicker;

import at.andreasrohner.spartantimelapserec.R;

/**
 * Dialog to select a timespan
 */
public class TimeSpanDialog extends BaseTimeSpanDialog {

	/**
	 * Milliseconds
	 */
	protected NumberPicker milliPicker;

	/**
	 * Seconds
	 */
	protected NumberPicker secondsPicker;

	/**
	 * Minutes
	 */
	protected NumberPicker minutesPicker;

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
	public TimeSpanDialog(Context context, SharedPreferences prefs, String key, CharSequence title, CharSequence dialogMessage, ChangeListener changeListener) {
		super(context, prefs, key, title, dialogMessage, changeListener);
	}

	@Override
	protected void initDialog() {
		view = View.inflate(context, R.layout.dialog_intervalpicker_preference, null);
		builder.setView(view);

		int value = prefs.getInt(key, 0);

		milliPicker = (NumberPicker) view.findViewById(R.id.dialog_seekbar_preference_milli);
		milliPicker.setFormatter(new FormatterMs());
		milliPicker.setMinValue(0);
		milliPicker.setMaxValue(99);
		milliPicker.setValue((value % 1000) / 10);

		secondsPicker = (NumberPicker) view.findViewById(R.id.dialog_seekbar_preference_seconds);
		secondsPicker.setFormatter(new Formatter2Digit());
		secondsPicker.setMinValue(0);
		secondsPicker.setMaxValue(59);
		secondsPicker.setValue((value - value % 1000) / 1000 % 60);

		minutesPicker = (NumberPicker) view.findViewById(R.id.dialog_seekbar_preference_minutes);
		minutesPicker.setMinValue(0);
		minutesPicker.setMaxValue(10);
		minutesPicker.setValue((value - secondsPicker.getValue() * 1000 - milliPicker.getValue() * 10) / 60000);
	}

	@Override
	protected void storeValue() {
		int value = minutesPicker.getValue() * 60000 + secondsPicker.getValue() * 1000 + milliPicker.getValue() * 10;
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(key, value);
		editor.apply();
	}
}
