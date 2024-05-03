package at.andreasrohner.spartantimelapserec.camera2;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.Button;

import at.andreasrohner.spartantimelapserec.R;

/**
 * Autofocus config Popup Dialog
 */
public class PopupDialogAfMf extends PopupDialogBase {

	/**
	 * Autofocus mode button
	 */
	private final Button btAutofocus;

	/**
	 * Autofocus field mode button
	 */
	private final Button btAfField;

	/**
	 * Manualfocus mode button
	 */
	private final Button btAfManual;

	/**
	 * Default background color
	 */
	private final Drawable defaultBackgroundColor;

	/**
	 * Current active mode
	 */
	private String currentMode;

	/**
	 * Constructor
	 *
	 * @param context Context
	 * @param camera  Camera
	 */
	public PopupDialogAfMf(Context context, Camera2Wrapper camera) {
		super(context);

		this.btAutofocus = (Button) view.findViewById(R.id.bt_afmf_autofocus);
		this.defaultBackgroundColor = this.btAutofocus.getBackground();
		this.btAutofocus.setOnClickListener(l -> {
			currentMode = "auto";
			updateButtonColor();
		});
		this.btAfField = (Button) view.findViewById(R.id.bt_afmf_af_field);
		this.btAfField.setOnClickListener(l -> {
			currentMode = "field";
			updateButtonColor();
		});
		this.btAfManual = (Button) view.findViewById(R.id.bt_afmf_af_manual);
		this.btAfManual.setOnClickListener(l -> {
			currentMode = "manual";
			updateButtonColor();
		});
	}

	/**
	 * Mark the current button
	 */
	private void updateButtonColor() {
		this.btAutofocus.setBackground(defaultBackgroundColor);
		this.btAfField.setBackground(defaultBackgroundColor);
		this.btAfManual.setBackground(defaultBackgroundColor);

		if ("auto".equals(currentMode)) {
			this.btAutofocus.setBackgroundColor(0xFF006600);
		} else if ("field".equals(currentMode)) {
			this.btAfField.setBackgroundColor(0xFF006600);
		} else if ("manual".equals(currentMode)) {
			this.btAfManual.setBackgroundColor(0xFF006600);
		}
	}

	@Override
	protected int storeValue() {
		/*
		int iso;
		int index = input.getValue();
		if (index == 0) {
			// Auto
			iso = -1;
		} else {
			iso = Integer.parseInt(displayValues.get(index));
		}

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("pref_camera_iso", iso);
		editor.apply();
*/
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
