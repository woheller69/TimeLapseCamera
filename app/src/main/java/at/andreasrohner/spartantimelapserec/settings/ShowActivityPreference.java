package at.andreasrohner.spartantimelapserec.settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;
import androidx.preference.Preference;
import at.andreasrohner.spartantimelapserec.rest.HttpThread;

/**
 * Show an activity, more like a 'submenu' than a real setting
 */
public class ShowActivityPreference extends DialogPreference {

	/**
	 * Log Tag
	 */
	private static final String TAG = HttpThread.class.getSimpleName();

	/**
	 * Settings implementation for this menu entry
	 */
	private MainSettingsMenu settings;

	/**
	 * Constructor
	 *
	 * @param context Context
	 */
	public ShowActivityPreference(@NonNull Context context) {
		super(context);
	}

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
		init();
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
		init();
	}

	/**
	 * Constructor
	 *
	 * @param context Context
	 * @param attrs   AttributeSet
	 */
	public ShowActivityPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	/**
	 * Initialize
	 */
	private void init() {
		try {
			settings = (MainSettingsMenu) Class.forName(getSummary().toString()).newInstance();
		} catch (Exception e) {
			Log.e(TAG, "Could not instance Settings implementation «" + getSummary().toString() + "»", e);
			return;
		}
		setSummary("");
	}

	/**
	 * @return The activity class of this settings
	 */
	public Class<? extends Activity> getActivityClass() {
		if (settings == null) {
			return null;
		}
		return settings.getActivityClass();
	}

	/**
	 * Update the summary in the
	 */
	public void updateSummary() {
		if (settings == null) {
			return ;
		}

		SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
		settings.updateSummary(this, getContext(), prefs);
	}
}
