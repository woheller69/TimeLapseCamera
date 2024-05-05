package at.andreasrohner.spartantimelapserec.preference.preftype;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

/**
 * Dialog to select a time
 */
public class TimeSpanDialogHourMin extends TimeSpanDialogHour {

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
	public TimeSpanDialogHourMin(Context context, SharedPreferences prefs, String key, CharSequence title, CharSequence dialogMessage, ChangeListener changeListener) {
		super(context, prefs, key, title, dialogMessage, changeListener);
	}

	@Override
	protected void loadData() {
		infiniteCheckbox.setVisibility(View.GONE);

		int value = prefs.getInt(key, 0);

		minutesPicker.setValue(value % 100);

		hoursPicker.setMaxValue(23);
		hoursPicker.setValue(value / 100);
	}

	@Override
	protected void storeValue() {
		int value = minutesPicker.getValue() + hoursPicker.getValue() * 100;
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(key, value);
		editor.apply();
	}
}
