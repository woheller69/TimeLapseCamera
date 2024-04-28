package at.andreasrohner.spartantimelapserec.preference.preftype;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;
import at.andreasrohner.spartantimelapserec.preference.update.SummaryPreference;
import at.andreasrohner.spartantimelapserec.rest.HttpThread;

/**
 * Used to display the preference only (editing not (yet) implemented)
 */
@SuppressWarnings("unused") // Loaded by reflection
public class DisplayPreference extends DialogPreference implements SummaryPreference, TimeSpanDialog.ChangeListener {

	/**
	 * Log Tag
	 */
	private static final String TAG = HttpThread.class.getSimpleName();

	/**
	 * Constructor
	 *
	 * @param context      Context
	 * @param attrs        AttributeSet
	 * @param defStyleAttr Style Attributes
	 * @param defStyleRes  Style Resources
	 */
	public DisplayPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	/**
	 * Constructor
	 *
	 * @param context      Context
	 * @param attrs        AttributeSet
	 * @param defStyleAttr Style Attributes
	 */
	public DisplayPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	/**
	 * Constructor
	 *
	 * @param context Context
	 * @param attrs   AttributeSet
	 */
	public DisplayPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	@SuppressWarnings("ConstantConditions")
	public void updateSummary() {
		int value = getPreferenceManager().getSharedPreferences().getInt(getKey(), 0);
		this.setSummary(Integer.valueOf(value));
	}

	/**
	 * TODO !!!!!!!
	 */
	public void showDialog() {
		// TODO !!!!!!!!!!!!!!!!!!!!
	}

	@Override
	public void valueChanged() {
		updateSummary();
	}
}
