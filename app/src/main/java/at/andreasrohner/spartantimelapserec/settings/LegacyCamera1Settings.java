package at.andreasrohner.spartantimelapserec.settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;

import androidx.preference.Preference;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.data.RecMode;
import at.andreasrohner.spartantimelapserec.data.RecSettingsLegacy;

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
	public Class<? extends Activity> getActivityClass(SharedPreferences prefs) {
		RecMode recMode = RecSettingsLegacy.getRecMode(prefs);
		if (recMode == RecMode.CAMERA2_TIME_LAPSE) {
			return Camera2SettingsActivity.class;
		} else {
			return LegacyCamera1SettingsActivity.class;
		}
	}

	@Override
	public void updateSummary(Preference pref, Context ctx, SharedPreferences prefs) {
		RecMode recMode = RecSettingsLegacy.getRecMode(prefs);
		StringBuilder b = new StringBuilder();

		if (recMode == RecMode.CAMERA2_TIME_LAPSE) {
			pref.setSummary("CAMERA 2 TODO");
			return;
		}

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

		if (recMode == RecMode.IMAGE_TIME_LAPSE || recMode == RecMode.VIDEO_TIME_LAPSE) {
			b.append(", ");
			b.append(ctx.getString(R.string.pref_capture_rate));
			b.append(": ");
			b.append(FormatUtil.formatTime(prefs.getInt("pref_capture_rate", 1000), ctx));
		}

		if (recMode == RecMode.VIDEO || recMode == RecMode.VIDEO_TIME_LAPSE) {
			String frameRate = prefs.getString("pref_frame_rate", null);
			if (frameRate != null) {
				b.append(", ");
				b.append(ctx.getString(R.string.pref_frame_rate));
				b.append(": ");
				b.append(frameRate);
			}
		}
		pref.setSummary(b.toString());
	}
}
