package at.andreasrohner.spartantimelapserec.preference.preftype;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;
import at.andreasrohner.spartantimelapserec.FormatUtil;
import at.andreasrohner.spartantimelapserec.preference.update.SummaryPreference;

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
	@SuppressWarnings("ConstantConditions")
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
