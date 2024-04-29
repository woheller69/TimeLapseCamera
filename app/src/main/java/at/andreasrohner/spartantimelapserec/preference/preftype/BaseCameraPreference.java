package at.andreasrohner.spartantimelapserec.preference.preftype;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;
import at.andreasrohner.spartantimelapserec.camera2.Camera2Wrapper;
import at.andreasrohner.spartantimelapserec.camera2.FileNameController;
import at.andreasrohner.spartantimelapserec.camera2.PopupDialogIso;
import at.andreasrohner.spartantimelapserec.camera2.ProcessErrorHandler;
import at.andreasrohner.spartantimelapserec.preference.update.SummaryPreference;
import at.andreasrohner.spartantimelapserec.rest.HttpThread;

/**
 * Base class for Camera preferences
 */
@SuppressWarnings("unused") // Loaded by reflection
public abstract class BaseCameraPreference extends DialogPreference implements DialogDisplayPreference, SummaryPreference, TimeSpanDialog.ChangeListener {

	/**
	 * Log Tag
	 */
	private static final String TAG = HttpThread.class.getSimpleName();

	/**
	 * Constructor
	 *
	 * @param context      Context
	 * @param attrs        AttributeSet
	 * @param defStyleAttr Style Attributes
	 * @param defStyleRes  Style Resources
	 */
	public BaseCameraPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	/**
	 * Constructor
	 *
	 * @param context      Context
	 * @param attrs        AttributeSet
	 * @param defStyleAttr Style Attributes
	 */
	public BaseCameraPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	/**
	 * Constructor
	 *
	 * @param context Context
	 * @param attrs   AttributeSet
	 */
	public BaseCameraPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	@SuppressWarnings("ConstantConditions")
	public void updateSummary() {
		int value = getPreferenceManager().getSharedPreferences().getInt(getKey(), 0);
		this.setSummary(String.valueOf(value));
	}

	@Override
	public void showDialog() {
		FileNameController fileNameController = new FileNameController(getSharedPreferences());
		ProcessErrorHandler errorHandler = new ProcessErrorHandler() {
			@Override
			public void error(String msg, Exception e) {
				Log.e(TAG, msg, e);
			}
		};

		Camera2Wrapper camera = new Camera2Wrapper(getContext(), fileNameController, errorHandler);
		camera.open();
		PopupDialogIso dialog = createDialog(camera);
		dialog.showDialog();
		dialog.setDialogResultListener(b -> {
			camera.close();
			updateSummary();
		});
	}

	/**
	 * Create the Dialog
	 *
	 * @param camera Camera
	 * @return Dialog
	 */
	protected abstract PopupDialogIso createDialog(Camera2Wrapper camera);

	@Override
	public void valueChanged() {
		updateSummary();
	}
}
