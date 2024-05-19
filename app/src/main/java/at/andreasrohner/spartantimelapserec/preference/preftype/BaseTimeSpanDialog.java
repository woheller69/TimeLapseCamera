package at.andreasrohner.spartantimelapserec.preference.preftype;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.NumberPicker;

import java.util.Locale;

import at.andreasrohner.spartantimelapserec.R;

/**
 * Base class for time span dialog
 */
public abstract class BaseTimeSpanDialog {

	/**
	 * Context
	 */
	protected final Context context;

	/**
	 * Preferences
	 */
	protected final SharedPreferences prefs;

	/**
	 * Preferences key
	 */
	protected final String key;

	/**
	 * Dialog Builder
	 */
	protected final AlertDialog.Builder builder;

	/**
	 * Listener which gets called on change
	 */
	protected final TimeSpanDialog.ChangeListener changeListener;

	/**
	 * Displayed view
	 */
	protected View view;

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
	public BaseTimeSpanDialog(Context context, SharedPreferences prefs, String key, CharSequence title, CharSequence dialogMessage, TimeSpanDialog.ChangeListener changeListener) {
		this.context = context;
		this.prefs = prefs;
		this.key = key;
		this.changeListener = changeListener;

		builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setMessage(dialogMessage);
	}

	/**
	 * Show the Dialog
	 */
	public void showDialog() {
		initDialog();

		builder.setPositiveButton(R.string.dialog_OK_button, (dialog, which) -> {
			storeValue();

			changeListener.valueChanged();
		});
		builder.setNegativeButton(R.string.dialog_CANCEL_button, (dialog, which) -> dialog.cancel());

		builder.show();
	}

	/**
	 * Store the current value
	 */
	protected abstract void storeValue();

	/**
	 * Initialize dialog
	 */
	protected abstract void initDialog();

	/**
	 * Formatter
	 */
	public static class Formatter2Digit implements NumberPicker.Formatter {

		@Override
		public String format(int value) {
			return String.format(Locale.getDefault(), "%02d", value);
		}
	}

	/**
	 * Formatter
	 */
	public static class FormatterMs implements NumberPicker.Formatter {

		@Override
		public String format(int value) {
			return String.format("%02d", value) + '0';
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
