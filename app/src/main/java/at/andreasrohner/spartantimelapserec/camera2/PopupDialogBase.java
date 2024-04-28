package at.andreasrohner.spartantimelapserec.camera2;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;

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
	 * Dialog end listener
	 */
	private DialogResult dialogResult;

	/**
	 * Constructor
	 *
	 * @param context Context
	 */
	public PopupDialogBase(Context context) {
		this.context = context;

		this.view = View.inflate(context, getDialogId(), null);

		builder = new AlertDialog.Builder(context);
		builder.setTitle(context.getString(getTitleId()));
		builder.setMessage(context.getString(getMessageId()));

		builder.setView(view);
		builder.setPositiveButton(context.getString(R.string.dialog_OK_button), (dialog, which) -> {
			storeValue();
			if (dialogResult != null) {
				dialogResult.dialogFinished(true);
			}
		});
		builder.setNegativeButton(context.getString(R.string.dialog_CANCEL_button), (dialog, which) -> {
			dialog.cancel();
			if (dialogResult != null) {
				dialogResult.dialogFinished(true);
			}
		});
		this.alert = builder.create();
	}

	/**
	 * @param dialogResult Dialog end listener
	 */
	public void setDialogResult(DialogResult dialogResult) {
		this.dialogResult = dialogResult;
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
		showDialog();
	}

	/**
	 * Display the dialog
	 */
	public void showDialog() {
		alert.show();
	}

	/**
	 * Dialog end listener
	 */
	public interface DialogResult {

		/**
		 * Called if the dialog closed
		 *
		 * @param accepted True if the value was accepted
		 */
		void dialogFinished(boolean accepted);
	}
}
