package at.andreasrohner.spartantimelapserec.preference.preftype;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import at.andreasrohner.spartantimelapserec.camera2.Camera2Wrapper;
import at.andreasrohner.spartantimelapserec.camera2.PopupDialogIso;

/**
 * ISO preferences
 */
@SuppressWarnings("unused") // Loaded by reflection
public class IsoPreference extends BaseCameraPreference {

	/**
	 * Constructor
	 *
	 * @param context      Context
	 * @param attrs        AttributeSet
	 * @param defStyleAttr Style Attributes
	 * @param defStyleRes  Style Resources
	 */
	public IsoPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	/**
	 * Constructor
	 *
	 * @param context      Context
	 * @param attrs        AttributeSet
	 * @param defStyleAttr Style Attributes
	 */
	public IsoPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	/**
	 * Constructor
	 *
	 * @param context Context
	 * @param attrs   AttributeSet
	 */
	public IsoPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected PopupDialogIso createDialog(Camera2Wrapper camera) {
		return new PopupDialogIso(getContext(), camera);
	}
}
