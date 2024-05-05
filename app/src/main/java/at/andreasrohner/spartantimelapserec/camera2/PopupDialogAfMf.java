package at.andreasrohner.spartantimelapserec.camera2;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.RadioGroup;

import androidx.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.R;

/**
 * Autofocus config Popup Dialog
 */
public class PopupDialogAfMf extends PopupDialogBase {

	/**
	 * Button group
	 */
	private final RadioGroup group;

	/**
	 * Constructor
	 *
	 * @param context Context
	 * @param camera  Camera
	 */
	public PopupDialogAfMf(Context context, Camera2Wrapper camera) {
		super(context);

		this.group = (RadioGroup) view.findViewById(R.id.bt_afmf_group);
		updateSelectedCheckbox();
	}

	/**
	 * Select the current checkbox
	 */
	private void updateSelectedCheckbox() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String afMode = prefs.getString("pref_camera_af_mode", null);

		if ("field".equals(afMode)) {
			this.group.check(R.id.bt_afmf_af_field);
		} else if ("manual".equals(afMode)) {
			this.group.check(R.id.bt_afmf_af_manual);
		} else {
			this.group.check(R.id.bt_afmf_autofocus);
		}
	}

	@Override
	protected int storeValue() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();

		int radioButtonID = group.getCheckedRadioButtonId();

		String mode;
		if (radioButtonID == R.id.bt_afmf_af_manual) {
			mode = "manual";
		} else if (radioButtonID == R.id.bt_afmf_af_field) {
			mode = "field";
		} else {
			mode = "auto";
		}

		editor.putString("pref_camera_af_mode", mode);
		editor.apply();

		return 0;
	}

	@Override
	public int getDialogId() {
		return R.layout.dialog_cam_afmf;
	}

	@Override
	public int getTitleId() {
		return R.string.autofocus;
	}

	@Override
	public int getMessageId() {
		return R.string.autofocus_message;
	}
}
