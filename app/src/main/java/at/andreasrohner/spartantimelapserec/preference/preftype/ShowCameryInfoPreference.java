package at.andreasrohner.spartantimelapserec.preference.preftype;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.camera2.CameraTiming;

/**
 * Show a camera setting, read only
 */
public class ShowCameryInfoPreference extends DialogPreference {

	/**
	 * Camera timing values
	 */
	private CameraTiming timing;

	/**
	 * Constructor
	 *
	 * @param context      Context
	 * @param attrs        AttributeSet
	 * @param defStyleAttr Style Attributes
	 * @param defStyleRes  Style Resources
	 */
	public ShowCameryInfoPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	/**
	 * Constructor
	 *
	 * @param context      Context
	 * @param attrs        AttributeSet
	 * @param defStyleAttr Style Attributes
	 */
	public ShowCameryInfoPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	/**
	 * Constructor
	 *
	 * @param context Context
	 * @param attrs   AttributeSet
	 */
	public ShowCameryInfoPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onAttached() {
		super.onAttached();

		updateSummary();
	}

	/**
	 * Show the value in the summary part
	 */
	public void updateSummary() {
		String key = getKey();
		SharedPreferences prefs = getPreferenceManager().getSharedPreferences();

		if ("pref_camera_iso".equals(key)) {
			int iso = prefs.getInt("pref_camera_iso", -1);
			if (iso == -1) {
				setIcon(R.drawable.ic_cam_bt_iso);
				setSummary(R.string.camera_value_auto);
			} else {
				setIcon(R.drawable.ic_cam_bt_iso_enabled);
				setSummary(String.valueOf(iso));
			}
		} else if ("pref_camera_exposure".equals(key)) {
			long exposure = prefs.getLong("pref_camera_exposure", -1);
			if (exposure == -1) {
				setIcon(R.drawable.ic_cam_bt_brightness);
				setSummary(R.string.camera_value_auto);
			} else {
				setIcon(R.drawable.ic_cam_bt_brightness_enabled);
				if (timing == null) {
					timing = new CameraTiming(getContext());
					timing.buildRangeSelection(-1, Long.MAX_VALUE);
				}

				setSummary(timing.findBestMatchingValue(exposure));
			}
		}
	}
}
