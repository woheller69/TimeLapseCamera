package at.andreasrohner.spartantimelapserec.preference.preftype;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import androidx.preference.PreferenceDialogFragmentCompat;

public class DatePreferenceDialogFragment extends PreferenceDialogFragmentCompat {

	/**
	 * Year
	 */
	private int lastYear;

	/**
	 * Mont
	 */
	private int lastMonth;

	/**
	 * Day
	 */
	private int lastDay;

	/**
	 * Date Picker
	 */
	private DatePicker datePicker;

	/**
	 * Constructor
	 */
	public DatePreferenceDialogFragment() {
	}

	/**
	 * Create a new instance
	 *
	 * @param key Key
	 * @return DatePreferenceDialogFragment
	 */
	public static DatePreferenceDialogFragment newInstance(String key) {
		final DatePreferenceDialogFragment fragment = new DatePreferenceDialogFragment();
		final Bundle b = new Bundle(1);
		b.putString(ARG_KEY, key);
		fragment.setArguments(b);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String dateValue = getDatePreference().getDate();

		if (dateValue == null || dateValue.isEmpty()) {
			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			dateValue = df.format(calendar.getTime());
		}

		lastYear = getYear(dateValue);
		lastMonth = getMonth(dateValue);
		lastDay = getDay(dateValue);
	}

	@Override
	protected View onCreateDialogView(Context context) {
		datePicker = new DatePicker(getContext());
		// Show spinner dialog for old APIs.
		datePicker.setCalendarViewShown(false);

		return datePicker;
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);

		datePicker.updateDate(lastYear, lastMonth - 1, lastDay);
	}

	@Override
	public void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			lastYear = datePicker.getYear();
			lastMonth = datePicker.getMonth() + 1;
			lastDay = datePicker.getDayOfMonth();

			String dateVal = lastYear + "-" + lastMonth + "-" + lastDay;

			final DatePreference preference = getDatePreference();
			if (preference.callChangeListener(dateVal)) {
				preference.setDate(dateVal);
			}
		}
	}

	private DatePreference getDatePreference() {
		return (DatePreference) getPreference();
	}

	private int getYear(String dateString) {
		String[] datePieces = dateString.split("-");
		return (Integer.parseInt(datePieces[0]));
	}

	private int getMonth(String dateString) {
		String[] datePieces = dateString.split("-");
		return (Integer.parseInt(datePieces[1]));
	}

	private int getDay(String dateString) {
		String[] datePieces = dateString.split("-");
		return (Integer.parseInt(datePieces[2]));
	}
}