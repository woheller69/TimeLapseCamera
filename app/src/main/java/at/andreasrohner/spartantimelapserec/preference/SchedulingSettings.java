package at.andreasrohner.spartantimelapserec.preference;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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

		boolean schedRecEnabled = prefs.getBoolean("pref_schedule_recording_enabled", false);

		StringBuilder b = new StringBuilder();
		b.append(ctx.getString(R.string.pref_scheduling_summ_start));
		b.append(' ');
		b.append(FormatUtil.formatTime(initialDelay, ctx));

		if (stopRecordingAfter < 2880) {
			b.append(", ");
			b.append(ctx.getString(R.string.pref_scheduling_summ_duration));
			b.append(' ');
			b.append(FormatUtil.formatTimeMin(stopRecordingAfter, ctx));
		}

		if (schedRecEnabled) {
			b.append(", ");
			b.append(ctx.getString(R.string.pref_scheduling_summ_scheduling));
			b.append(' ');

			try {
				Date date = new SimpleDateFormat("yyyy-MM-dd").parse(prefs.getString("pref_schedule_recording_date", ""));
				Calendar c = Calendar.getInstance();
				c.setTime(date);
				int time = prefs.getInt("pref_schedule_recording_time", 0);

				c.set(Calendar.HOUR_OF_DAY, time / 100);
				c.set(Calendar.MINUTE, time % 100);

				DateFormat f = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM, SimpleDateFormat.SHORT);
				b.append(f.format(c.getTime()));
			} catch (ParseException e) {
				b.append("Invalid Date");
			}
		}

		pref.setSummary(b.toString());
	}
}
