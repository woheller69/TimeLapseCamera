package at.andreasrohner.spartantimelapserec.preference;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import androidx.preference.Preference;
import at.andreasrohner.spartantimelapserec.FormatUtil;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.preference.activity.SchedulingSettingsActivity;
import at.andreasrohner.spartantimelapserec.preference.mainmenu.MainSettingsMenu;

/**
 * Scheduling settings
 */
@SuppressWarnings("unused") // Loaded by menu
public class SchedulingSettings implements MainSettingsMenu {

	/**
	 * Constructor
	 */
	public SchedulingSettings() {
	}

	@Override
	public Class<? extends Activity> getActivityClass(SharedPreferences prefs) {
		return SchedulingSettingsActivity.class;
	}

	@Override
	public void updateSummary(Preference pref, Context ctx, SharedPreferences prefs) {
		int initialDelay = prefs.getInt("pref_initial_delay", 1000);

		//2880 = infinite
		int stopRecordingAfter = prefs.getInt("pref_stop_recording_after", 2880);

		// null = disabled
		String scheduleRecording = prefs.getString("pref_schedule_recording", null);

		StringBuilder b = new StringBuilder();
		b.append(ctx.getString(R.string.pref_scheduling_summ_start));
		b.append(' ');
		b.append(FormatUtil.formatTime(initialDelay, ctx));

		if (stopRecordingAfter < 2880) {
			b.append(", ");
			b.append(ctx.getString(R.string.pref_scheduling_summ_duration));
			b.append(' ');
			b.append(FormatUtil.formatTimeMin(initialDelay, ctx));
		}

		if (scheduleRecording != null && scheduleRecording.startsWith("1|")) {
			b.append(", ");
			b.append(ctx.getString(R.string.pref_scheduling_summ_scheduling));
			b.append(' ');
			DateFormat f = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM, SimpleDateFormat.SHORT);

			b.append(f.format(Long.parseLong(scheduleRecording.substring(2))));
		}

		pref.setSummary(b.toString());
	}
}
