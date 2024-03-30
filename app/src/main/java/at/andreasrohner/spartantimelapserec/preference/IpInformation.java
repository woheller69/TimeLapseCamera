package at.andreasrohner.spartantimelapserec.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.widget.Toast;

import java.net.InetAddress;

import at.andreasrohner.spartantimelapserec.R;
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
		updateData();
	}

	/**
	 * Update Data
	 */
	private void updateData() {
		Context ctx = getContext();
		InetAddress addr = RestService.getLocalInetAddress(ctx);
		if (addr == null) {
			setSummary(ctx.getString(R.string.error_no_ip_refresh));
		} else {
			setSummary("http://" + addr + ":" + RestService.getPort(ctx));
		}
	}
}