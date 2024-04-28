package at.andreasrohner.spartantimelapserec.preference.preftype;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;
import at.andreasrohner.spartantimelapserec.FormatUtil;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.preference.update.SummaryPreference;
import at.andreasrohner.spartantimelapserec.rest.HttpThread;

/**
 * Allow to select a timespan
 */
@SuppressWarnings("unused") // Loaded by reflection
public class TimeSpanPreference extends DialogPreference implements SummaryPreference, TimeSpanDialog.ChangeListener {

	/**
	 * Log Tag
	 */
	private static final String TAG = HttpThread.class.getSimpleName();

	/**
	 * Type of this entry
	 */
	private String timeSpanType;

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
		init(context, attrs);
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
		init(context, attrs);
	}

	/**
	 * Constructor
	 *
	 * @param context Context
	 * @param attrs   AttributeSet
	 */
	public TimeSpanPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	/**
	 * Initialize
	 *
	 * @param context Context
	 * @param attrs   AttributeSet
	 */
	private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TimeLapse);
		this.timeSpanType = a.getString(R.styleable.TimeLapse_timeSpanType);
		a.recycle();
	}
	//formatMs

	@Override
	@SuppressWarnings("ConstantConditions")
	public void updateSummary() {
		String formatted = "error!";
		int value = getPreferenceManager().getSharedPreferences().getInt(getKey(), 0);
		if ("formatMs".equals(timeSpanType)) {
			formatted = FormatUtil.formatTime(value, getContext());
		} else if ("formatHour".equals(timeSpanType)) {
			if (value >= 2880) {
				formatted = getContext().getString(R.string.pref_infinite);
			} else {
				formatted = FormatUtil.formatTimeMin(value, getContext());
			}
		} else if ("formatHourMinClock".equals(timeSpanType)) {
			formatted = FormatUtil.formatTimeFromInt(value);
		}

		this.setSummary(formatted);
	}

	/**
	 * Show the dialog to enter the timespan
	 */
	public void showDialog() {
		BaseTimeSpanDialog dlg;

		if ("formatMs".equals(timeSpanType)) {
			dlg = new TimeSpanDialog(getContext(), getSharedPreferences(), getKey(), getTitle(), getDialogMessage(), this);
		} else if ("formatHour".equals(timeSpanType)) {
			dlg = new TimeSpanDialogHour(getContext(), getSharedPreferences(), getKey(), getTitle(), getDialogMessage(), this);
		} else if ("formatHourMinClock".equals(timeSpanType)) {
			dlg = new TimeSpanDialogHourMin(getContext(), getSharedPreferences(), getKey(), getTitle(), getDialogMessage(), this);
		} else {
			Log.e(TAG, "No dialog for «" + timeSpanType + "»");
			return;
		}

		dlg.showDialog();
	}

	@Override
	public void valueChanged() {
		updateSummary();
	}
}
