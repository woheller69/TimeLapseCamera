package at.andreasrohner.spartantimelapserec.preference;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.Preference;
import at.andreasrohner.spartantimelapserec.FormatUtil;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.camera2.CameraTiming;
import at.andreasrohner.spartantimelapserec.data.RecMode;
import at.andreasrohner.spartantimelapserec.data.RecSettingsLegacy;
import at.andreasrohner.spartantimelapserec.preference.activity.Camera2SettingsActivity;
import at.andreasrohner.spartantimelapserec.preference.activity.LegacyCamera1SettingsActivity;
import at.andreasrohner.spartantimelapserec.preference.mainmenu.MainSettingsMenu;

/**
 * Camera Settings
 */
@SuppressWarnings({"unused", "deprecated"}) // Loaded by menu / legacy classes, will not be updated
public class CameraSettings implements MainSettingsMenu {

	/**
	 * Camera timing values
	 */
	private CameraTiming timing;

	/**
	 * Constructor
	 */
	public CameraSettings() {
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

		b.append(ctx.getString(R.string.pref_camera_camera));
		b.append(": ");
		if (Integer.parseInt(prefs.getString("pref_camera", "0")) == 1) {
			b.append(ctx.getString(R.string.pref_camera_front));
		} else {
			b.append(ctx.getString(R.string.pref_camera_back));
		}

		b.append(", ");
		b.append(ctx.getString(R.string.pref_frame_size));
		b.append(": ");
		b.append(prefs.getString("pref_frame_size", "1920x1080"));

		if (recMode == RecMode.CAMERA2_TIME_LAPSE) {
			int iso = prefs.getInt("pref_camera_iso", -1);
			if (iso != -1) {
				b.append(", ");
				b.append(ctx.getString(R.string.iso));
				b.append(": ");
				b.append(String.valueOf(iso));
			}

			long exposure = prefs.getLong("pref_camera_exposure", -1);
			if (exposure != -1) {
				b.append(", ");
				b.append(ctx.getString(R.string.exposure_time));
				b.append(": ");

				if (timing == null) {
					timing = new CameraTiming(ctx);
					timing.buildRangeSelection(-1, Long.MAX_VALUE);
				}

				b.append(timing.findBestMatchingValue(exposure));
			}

			String currentWbMode = prefs.getString("pref_camera_wb", "auto");
			if (!"auto".equals(currentWbMode)) {
				b.append(", ");
				b.append(ctx.getString(R.string.cam_header_wb));
				b.append(": ");

				if ("incandescent".equals(currentWbMode)) {
					b.append(ctx.getString(R.string.cam_wb_incandescent));
				} else if ("daylight".equals(currentWbMode)) {
					b.append(ctx.getString(R.string.cam_wb_daylight));
				} else if ("fluorescent".equals(currentWbMode)) {
					b.append(ctx.getString(R.string.cam_wb_fluorescent));
				} else if ("cloud".equals(currentWbMode)) {
					b.append(ctx.getString(R.string.cam_wb_cloud));
				}
			}
		}

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
