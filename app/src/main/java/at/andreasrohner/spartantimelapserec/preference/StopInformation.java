package at.andreasrohner.spartantimelapserec.preference;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import androidx.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.settings.Camera1SettingsActivity;
import at.andreasrohner.spartantimelapserec.settings.StopSettingsActivity;

/**
 * Show Stop settings
 */
public class StopInformation extends DialogPreference {

	public StopInformation(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);

		updateData();
	}

	public StopInformation(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public StopInformation(Context context, AttributeSet attrs) {
		this(context, attrs, 0, 0);
	}

	public StopInformation(Context context) {
		this(context, null, 0);
	}

	@Override
	protected void showDialog(Bundle state) {
		Context ctx = getContext();
		Intent myIntent = new Intent(ctx, StopSettingsActivity.class);
		ctx.startActivity(myIntent);
	}

	/**
	 * Update Data
	 */
	public void updateData() {
		Context ctx = getContext();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		boolean lowStorage = prefs.getBoolean("pref_stop_low_storage", true);
		boolean lowBattery = prefs.getBoolean("pref_stop_low_battery", true);
		if (lowStorage == false && lowBattery == false) {
			setSummary(ctx.getString(R.string.pref_stop_info_nostop));
		} else {
			String info = ctx.getString(R.string.pref_stop_info_stop_start) + ' ';

			if (lowStorage) {
				info += ctx.getString(R.string.pref_stop_info_stop_storage);
			}
			if (lowStorage && lowBattery) {
				info += ", ";
			}
			if (lowBattery) {
				info += ctx.getString(R.string.pref_stop_info_stop_battery);
			}
			setSummary(info);
		}
	}
}