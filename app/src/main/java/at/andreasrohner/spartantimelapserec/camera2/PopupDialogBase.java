package at.andreasrohner.spartantimelapserec.camera2;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.ImageButton;

import at.andreasrohner.spartantimelapserec.R;

/**
 * Base dialog for camera popup settings
 */
public abstract class PopupDialogBase implements View.OnClickListener {

	/**
	 * Context
	 */
	protected final Context context;

	/**
	 * View
	 */
	protected final View view;

	/**
	 * Dialog Builder
	 */
	protected final AlertDialog.Builder builder;

	/**
	 * Alert Dialog
	 */
	private final AlertDialog alert;

	/**
	 * Constructor
	 *
	 * @param context Context
	 * @param button  Button
	 */
	public PopupDialogBase(Context context, ImageButton button) {
		this.context = context;
		button.setOnClickListener(this);

		this.view = View.inflate(context, getDialogId(), null);

		builder = new AlertDialog.Builder(context);
		builder.setTitle(context.getString(getTitleId()));
		builder.setMessage(context.getString(getMessageId()));

		builder.setView(view);
		builder.setPositiveButton(context.getString(R.string.dialog_OK_button), (dialog, which) -> {
			storeValue();
		});
		builder.setNegativeButton(context.getString(R.string.dialog_CANCEL_button), (dialog, which) -> dialog.cancel());
		this.alert = builder.create();
	}

	/**
	 * Store the selected value
	 */
	protected abstract void storeValue();

	/**
	 * @return The ID of the Dialog
	 */
	public abstract int getDialogId();

	/**
	 * @return The ID of the Title
	 */
	public abstract int getTitleId();

	/**
	 * @return The ID of the message
	 */
	public abstract int getMessageId();

	@Override
	public void onClick(View v) {
		alert.show();
	}
}
