package at.andreasrohner.spartantimelapserec.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceViewHolder;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.rest.HttpThread;
import at.andreasrohner.spartantimelapserec.updateableprefs.SummaryPreference;

/**
 * Allow to select a timespan
 */
public class TimeSpanPreference extends DialogPreference implements SummaryPreference, TimeSpanDialog.ChangeListener {

	/**
	 * Constructor
	 *
	 * @param context      Context
	 * @param attrs        AttributeSet
	 * @param defStyleAttr Style Attributes
	 * @param defStyleRes  Style Resources
	 */
	public TimeSpanPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	/**
	 * Constructor
	 *
	 * @param context      Context
	 * @param attrs        AttributeSet
	 * @param defStyleAttr Style Attributes
	 */
	public TimeSpanPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	/**
	 * Constructor
	 *
	 * @param context Context
	 * @param attrs   AttributeSet
	 */
	public TimeSpanPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void updateSummary() {
		this.setSummary(FormatUtil.formatTime(getPreferenceManager().getSharedPreferences().getInt(getKey(), 0), getContext()));
	}

	/**
	 * Show the dialog to enter the timespan
	 */
	public void showDialog() {
		TimeSpanDialog dlg = new TimeSpanDialog(getContext(), getSharedPreferences(), getKey(), getTitle(), getDialogMessage(), this);
		dlg.showDialog();
	}

	@Override
	public void valueChanged() {
		updateSummary();
	}
}
