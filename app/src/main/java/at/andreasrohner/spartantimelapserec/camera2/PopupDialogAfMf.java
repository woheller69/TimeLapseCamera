package at.andreasrohner.spartantimelapserec.camera2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;

import androidx.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.camera2.wrapper.Camera2Wrapper;
import at.andreasrohner.spartantimelapserec.preference.PrefUtil;

/**
 * Autofocus config Popup Dialog
 */
public class PopupDialogAfMf extends PopupDialogBase {

	/**
	 * Button group
	 */
	private final RadioGroup group;

	/**
	 * Focus point width
	 */
	private final EditText focusPointWidth;

	/**
	 * Focus point height
	 */
	private final EditText focusPointHeight;

	/**
	 * Constructor
	 *
	 * @param context Context
	 * @param camera  Camera
	 */
	public PopupDialogAfMf(Context context, Camera2Wrapper camera) {
		super(context);

		this.group = (RadioGroup) view.findViewById(R.id.bt_afmf_group);
		this.focusPointWidth = (EditText) view.findViewById(R.id.focus_point_width);
		this.focusPointHeight = (EditText) view.findViewById(R.id.focus_point_height);

		((ImageButton) view.findViewById(R.id.focus_help_button)).setOnClickListener(x -> showInformationDialog(R.string.focus_information));
		((ImageButton) view.findViewById(R.id.refocus_help_button)).setOnClickListener(x -> showInformationDialog(R.string.refocus_info));

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		this.focusPointWidth.setText(String.valueOf(prefs.getInt("pref_camera_af_field_width", 100)));
		this.focusPointHeight.setText(String.valueOf(prefs.getInt("pref_camera_af_field_height", 100)));

		updateSelectedCheckbox();
	}

	/**
	 * Show Information / Help
	 *
	 * @param messageId Text ID
	 */
	private void showInformationDialog(int messageId) {
		AlertDialog.Builder helpDlg = new AlertDialog.Builder(context);
		helpDlg.setTitle(R.string.focus_information_header);
		helpDlg.setMessage(messageId);

		helpDlg.setPositiveButton(R.string.dialog_OK_button, (dialog, which) -> {
		});
		helpDlg.show();
	}

	/**
	 * Select the current checkbox
	 */
	private void updateSelectedCheckbox() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		PrefUtil.AfMode afMode = PrefUtil.getAfMode(prefs);

		if (afMode == PrefUtil.AfMode.FIELD) {
			this.group.check(R.id.bt_afmf_af_field);
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
		if (radioButtonID == R.id.bt_afmf_af_field) {
			mode = "field";
		} else {
			mode = "auto";
		}

		// Reset AF Field / Position
		editor.putString("pref_camera_af_field", "");

		// Store new mode
		editor.putString("pref_camera_af_mode", mode);

		// Store field size
		storeFieldSize(editor, "pref_camera_af_field_width", this.focusPointWidth);
		storeFieldSize(editor, "pref_camera_af_field_height", this.focusPointHeight);

		editor.apply();

		return 0;
	}

	/**
	 * Parse int from input field to settings
	 *
	 * @param editor Editor
	 * @param key    Key
	 * @param text   Value Field
	 */
	private void storeFieldSize(SharedPreferences.Editor editor, String key, EditText text) {
		int value;
		try {
			value = Integer.parseInt(String.valueOf(text.getText()));
		} catch (NumberFormatException ex) {
			value = 20;
		}

		editor.putInt(key, value);
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
