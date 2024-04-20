package at.andreasrohner.spartantimelapserec.preference;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import at.andreasrohner.spartantimelapserec.settings.LegacyCamera1Settings;
import at.andreasrohner.spartantimelapserec.settings.LegacyScheduling1Settings;

/**
 * Schedule settings
 */
public class SchedulingInformation extends DialogPreference {

	public SchedulingInformation(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);

		updateData();
	}

	public SchedulingInformation(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public SchedulingInformation(Context context, AttributeSet attrs) {
		this(context, attrs, 0, 0);
	}

	public SchedulingInformation(Context context) {
		this(context, null, 0);
	}

	@Override
	protected void showDialog(Bundle state) {
		Context ctx = getContext();
		Intent myIntent = new Intent(ctx, LegacyScheduling1Settings.class);
		ctx.startActivity(myIntent);
	}

	/**
	 * Update Data
	 */
	public void updateData() {
		// TODO !!!!!!!!!!!!!!
		Context ctx = getContext();
		setSummary("blablablabal222");
	//	setSummary(ctx.getString(R.string.error_no_ip_refresh));
//		setTitle(ctx.getString(R.string.pref_restapi_connectioninfo_title_on));
	}
}