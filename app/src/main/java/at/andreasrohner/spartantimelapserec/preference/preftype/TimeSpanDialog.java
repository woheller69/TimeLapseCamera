package at.andreasrohner.spartantimelapserec.preference.preftype;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.NumberPicker;

import at.andreasrohner.spartantimelapserec.R;

/**
 * Dialog to select a timespan
 */
public class TimeSpanDialog {

	/**
	 * Context
	 */
	private final Context context;

	/**
	 * Preferences
	 */
	private final SharedPreferences prefs;

	/**
	 * Preferences key
	 */
	private final String key;

	/**
	 * Dialog Builder
	 */
	private final AlertDialog.Builder builder;

	/**
	 * Listener which gets called on change
	 */
	private final ChangeListener changeListener;

	/**
	 * Displayed view
	 */
	private View view;

	/**
	 * Milliseconds
	 */
	private NumberPicker milliPicker;

	/**
	 * Seconds
	 */
	private NumberPicker secondsPicker;

	/**
	 * Minutes
	 */
	private NumberPicker minutesPicker;

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
		this.context = context;
		this.prefs = prefs;
		this.key = key;
		this.changeListener = changeListener;

		builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setMessage(dialogMessage);
	}

	/**
	 * Initialize dialog
	 */
	private void initDialog() {
		view = View.inflate(context, R.layout.dialog_intervalpicker_preference, null);
		builder.setView(view);

		int value = prefs.getInt(key, 0);

		milliPicker = (NumberPicker) view.findViewById(R.id.dialog_seekbar_preference_milli);
		milliPicker.setFormatter(new Formatter());
		milliPicker.setMinValue(0);
		milliPicker.setMaxValue(99);
		milliPicker.setValue((value % 1000) / 10);

		secondsPicker = (NumberPicker) view.findViewById(R.id.dialog_seekbar_preference_seconds);
		secondsPicker.setFormatter(new Formatter());
		secondsPicker.setMinValue(0);
		secondsPicker.setMaxValue(59);
		secondsPicker.setValue((value - value % 1000) / 1000 % 60);

		minutesPicker = (NumberPicker) view.findViewById(R.id.dialog_seekbar_preference_minutes);
		minutesPicker.setMinValue(0);
		minutesPicker.setMaxValue(10);
		minutesPicker.setValue((value - secondsPicker.getValue() * 1000 - milliPicker.getValue() * 10) / 60000);
	}

	/**
	 * Show the Dialog
	 */
	public void showDialog() {
		initDialog();

		builder.setPositiveButton(context.getString(R.string.dialog_OK_button), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int value = minutesPicker.getValue() * 60000 + secondsPicker.getValue() * 1000 + milliPicker.getValue() * 10;
				SharedPreferences.Editor editor = prefs.edit();
				editor.putInt(key, value);
				editor.apply();

				changeListener.valueChanged();
			}
		});
		builder.setNegativeButton(context.getString(R.string.dialog_CANCEL_button), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		builder.show();
	}

	/**
	 * Formatter
	 */
	public static class Formatter implements NumberPicker.Formatter {

		@Override
		public String format(int value) {
			return String.format("%02d", value);
		}
	}

	/**
	 * Listener for changes
	 */
	public interface ChangeListener {

		/**
		 * Value has been changed
		 */
		void valueChanged();
	}
}
