package at.andreasrohner.spartantimelapserec.camera2;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Build;
import android.util.Range;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import androidx.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.camera2.wrapper.Camera2Wrapper;

/**
 * Exposure Time Popup Dialog
 */
public class PopupDialogExposureTime extends PopupDialogBase {

	/**
	 * Log Tag
	 */
	private static final String TAG = PopupDialogExposureTime.class.getSimpleName();

	/**
	 * Input field
	 */
	private final NumberPicker absoluteExposureInput;

	/**
	 * Tabs
	 */
	private final TabLayout tabs;

	/**
	 * Relative exposure
	 */
	private final SeekBar relativeExposureInput;

	/**
	 * Current relative exposure
	 */
	private final TextView relativeExposureView;

	/**
	 * Info
	 */
	private final TextView relativeExposureInfo;

	/**
	 * Camera timing values
	 */
	private CameraTiming timing;

	/**
	 * Constructor
	 *
	 * @param context Context
	 * @param camera  Camera
	 */
	public PopupDialogExposureTime(Context context, Camera2Wrapper camera) {
		super(context);
		timing = new CameraTiming(context);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		this.absoluteExposureInput = (NumberPicker) view.findViewById(R.id.exposure_absolute);
		this.relativeExposureInput = (SeekBar) view.findViewById(R.id.exposure_relative);
		this.relativeExposureInput.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				updateRelativeText();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// Nothing to do
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// Nothing to do
			}
		});
		this.relativeExposureView = (TextView) view.findViewById(R.id.exposure_relative_view);
		this.relativeExposureInfo = (TextView) view.findViewById(R.id.exposure_relative_info);

		this.tabs = (TabLayout) view.findViewById(R.id.exposure_tabs);
		this.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				showHideInputs(tab.getPosition() == 0);
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {
				// Nothing to do
			}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {
				// Nothing to do
			}
		});

		Range<Integer> range = camera.getCharacteristics().get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			this.relativeExposureInput.setMin(range.getLower());
		}
		this.relativeExposureInput.setMax(range.getUpper());
		int rel = prefs.getInt("pref_camera_exposure_rel", 0);
		this.relativeExposureInput.setProgress(rel);

		Range<Long> ranges = camera.getCharacteristics().get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
		// Nanoseconds, 1/1000000000s
		long upper = ranges.getUpper();
		long lower = ranges.getLower();

		timing.buildRangeSelection(lower, upper);

		absoluteExposureInput.setMinValue(0);
		absoluteExposureInput.setMaxValue(timing.getDisplayValues().size() - 1);
		absoluteExposureInput.setDisplayedValues(timing.getDisplayValues().toArray(new String[] {}));

		long exposure = prefs.getLong("pref_camera_exposure", -1);
		int index = timing.getTimeValues().indexOf(exposure);

		if (index != -1) {
			absoluteExposureInput.setValue(index);
		} else {
			absoluteExposureInput.setValue(0);
		}

		showHideInputs(true);
	}

	/**
	 * Show hide controls
	 *
	 * @param absoluteExposure true: absolute, false: relative
	 */
	private void showHideInputs(boolean absoluteExposure) {
		if (absoluteExposure) {
			absoluteExposureInput.setVisibility(View.VISIBLE);
			relativeExposureInput.setVisibility(View.GONE);
			relativeExposureView.setVisibility(View.GONE);
			relativeExposureInfo.setVisibility(View.GONE);
		} else {
			absoluteExposureInput.setVisibility(View.GONE);
			relativeExposureInput.setVisibility(View.VISIBLE);
			relativeExposureView.setVisibility(View.VISIBLE);
			relativeExposureInfo.setVisibility(View.VISIBLE);
			updateRelativeText();
		}
	}

	/**
	 * Update Relative exposure value
	 */
	private void updateRelativeText() {
		relativeExposureView.setText(String.valueOf(relativeExposureInput.getProgress()));
	}

	@Override
	protected int storeValue() {
		int index = absoluteExposureInput.getValue();
		long exposure = timing.getTimeValues().get(index);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putLong("pref_camera_exposure", exposure);
		editor.putInt("pref_camera_exposure_rel", relativeExposureInput.getProgress());
		editor.apply();
		return 0;
	}

	@Override
	public int getDialogId() {
		return R.layout.dialog_cam_exposure;
	}

	@Override
	public int getTitleId() {
		return R.string.exposure_time;
	}

	@Override
	public int getMessageId() {
		return R.string.exposure_time_message;
	}
}
