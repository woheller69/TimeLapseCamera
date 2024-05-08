package at.andreasrohner.spartantimelapserec.preference.mainmenu;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.preference.update.SummaryPreference;

/**
 * Show an activity, more like a 'submenu' than a real setting
 */
public class ShowActivityPreference extends DialogPreference implements SummaryPreference {

	/**
	 * Log Tag
	 */
	private static final String TAG = ShowActivityPreference.class.getSimpleName();

	/**
	 * Settings implementation for this menu entry
	 */
	private MainSettingsMenu settings;

	/**
	 * Constructor
	 *
	 * @param context      Context
	 * @param attrs        AttributeSet
	 * @param defStyleAttr Style Attributes
	 * @param defStyleRes  Style Resources
	 */
	public ShowActivityPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
	public ShowActivityPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	/**
	 * Constructor
	 *
	 * @param context Context
	 * @param attrs   AttributeSet
	 */
	public ShowActivityPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
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
		String settingsClass = a.getString(R.styleable.TimeLapse_settingsClass);
		a.recycle();

		if (settingsClass == null) {
			Log.e(TAG, "Missing settings class for «" + getKey() + "»");
			return;
		}

		try {
			settings = (MainSettingsMenu) Class.forName(settingsClass).newInstance();
		} catch (Exception e) {
			Log.e(TAG, "Could not instance Settings implementation «" + settingsClass + "» for «" + getKey() + "»", e);
		}
	}

	/**
	 * Get the activity to open
	 *
	 * @param prefs Preferences
	 * @return The activity class of this settings
	 */
	public Class<? extends Activity> getActivityClass(SharedPreferences prefs) {
		if (settings == null) {
			return null;
		}
		return settings.getActivityClass(prefs);
	}

	/**
	 * Update the summary in the
	 */
	public void updateSummary() {
		if (settings == null) {
			return;
		}

		SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
		settings.updateSummary(this, getContext(), prefs);
	}
}
