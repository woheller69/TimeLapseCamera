package at.andreasrohner.spartantimelapserec.settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.preference.ListPreference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import androidx.preference.Preference;
import at.andreasrohner.spartantimelapserec.R;

/**
 * Legacy settings of Camera1 interface
 */
public class LegacyCamera1Settings implements MainSettingsMenu {

	/**
	 * Constructor
	 */
	public LegacyCamera1Settings() {
	}

	@Override
	public Class<? extends Activity> getActivityClass() {
		return LegacyCamera1SettingsActivity.class;
	}

	@Override
	public void updateSummary(Preference pref, Context ctx, SharedPreferences prefs) {
		StringBuffer b = new StringBuffer();
		b.append(ctx.getString(R.string.pref_camera_camera));
		b.append(": ");
		if (Integer.parseInt(prefs.getString("pref_camera", "0")) == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			b.append(ctx.getString(R.string.pref_camera_front));
		} else {
			b.append(ctx.getString(R.string.pref_camera_back));
		}

		b.append(", ");
		b.append(ctx.getString(R.string.pref_frame_size));
		b.append(": ");
		b.append(prefs.getString("pref_frame_size", "1920x1080"));
		b.append(", ");

		b.append(", ");
		b.append(ctx.getString(R.string.pref_capture_rate));
		b.append(": ");
		b.append(FormatUtil.formatTime(prefs.getInt("pref_capture_rate", 1000), ctx));

		pref.setSummary(b.toString());
	}
}
