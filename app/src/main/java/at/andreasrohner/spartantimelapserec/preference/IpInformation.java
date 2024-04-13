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
import at.andreasrohner.spartantimelapserec.settings.RestSettingsActivity;
import at.andreasrohner.spartantimelapserec.rest.RestService;

/**
 * Show the current IP and Port for connection
 */
public class IpInformation extends DialogPreference {

	public IpInformation(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);

		updateData();
	}

	public IpInformation(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public IpInformation(Context context, AttributeSet attrs) {
		this(context, attrs, 0, 0);
	}

	public IpInformation(Context context) {
		this(context, null, 0);
	}

	@Override
	protected void showDialog(Bundle state) {
		Context ctx = getContext();
		Intent myIntent = new Intent(ctx, RestSettingsActivity.class);
		ctx.startActivity(myIntent);
	}

	/**
	 * Update Data
	 */
	public void updateData() {
		Context ctx = getContext();
		InetAddress addr = RestService.getLocalInetAddress(ctx);
		if (addr == null) {
			setSummary(ctx.getString(R.string.error_no_ip_refresh));
		} else {
			setSummary("http:/" + addr + ":" + RestService.getPort(ctx));
		}

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		if (prefs.getBoolean("pref_restapi_enabled", false)) {
			setTitle(ctx.getString(R.string.pref_restapi_connectioninfo_title_on));
		} else {
			setTitle(ctx.getString(R.string.pref_restapi_connectioninfo_title_off));
		}
	}
}