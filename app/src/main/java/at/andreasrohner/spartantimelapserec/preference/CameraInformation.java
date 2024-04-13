package at.andreasrohner.spartantimelapserec.preference;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import java.net.InetAddress;

import androidx.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.rest.RestService;
import at.andreasrohner.spartantimelapserec.settings.Camera1SettingsActivity;
import at.andreasrohner.spartantimelapserec.settings.RestSettingsActivity;

/**
 * Show Camera / Recording settings
 */
public class CameraInformation extends DialogPreference {

	public CameraInformation(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);

		updateData();
	}

	public CameraInformation(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public CameraInformation(Context context, AttributeSet attrs) {
		this(context, attrs, 0, 0);
	}

	public CameraInformation(Context context) {
		this(context, null, 0);
	}

	@Override
	protected void showDialog(Bundle state) {
		Context ctx = getContext();
		Intent myIntent = new Intent(ctx, Camera1SettingsActivity.class);
		ctx.startActivity(myIntent);
	}

	/**
	 * Update Data
	 */
	public void updateData() {
		// TODO !!!!!!!!!!!!!!
		Context ctx = getContext();
		setSummary("blablablabal");
	//	setSummary(ctx.getString(R.string.error_no_ip_refresh));
//		setTitle(ctx.getString(R.string.pref_restapi_connectioninfo_title_on));
	}
}