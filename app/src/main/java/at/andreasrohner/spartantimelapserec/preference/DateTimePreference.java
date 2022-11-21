/*
 * Spartan Time Lapse Recorder - Minimalistic android time lapse recording app
 * Copyright (C) 2014  Andreas Rohner
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.andreasrohner.spartantimelapserec.preference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.TimePicker;
import at.andreasrohner.spartantimelapserec.R;

public class DateTimePreference extends DialogPreference implements
		OnCheckedChangeListener {
	Calendar mCal;
	boolean mEnabled;
	DatePicker mDatePicker;
	TimePicker mTimePicker;

	@SuppressLint("NewApi")
	public DateTimePreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		mCal = Calendar.getInstance();

		setWidgetLayoutResource(R.layout.dialog_date_widget_preference);
		setDialogLayoutResource(R.layout.dialog_date_preference);
	}

	public long getTimeInMillis() {
		if (mDatePicker != null && mTimePicker != null)
			mCal.set(mDatePicker.getYear(), mDatePicker.getMonth(),
					mDatePicker.getDayOfMonth(), mTimePicker.getCurrentHour(),
					mTimePicker.getCurrentMinute());

		if (mCal.getTimeInMillis() < System.currentTimeMillis())
			mCal.setTimeInMillis(System.currentTimeMillis());

		mCal.set(Calendar.SECOND, 0);

		return mCal.getTimeInMillis();
	}

	public static long parseTime(String value) {
		long time = System.currentTimeMillis();
		try {
			long temp;
			if (value != null) {
				String[] parts = value.split("\\|");
				if (parts.length == 2) {
					temp = Long.valueOf(parts[1]);
					if (temp > time)
						time = temp;
				}
			}
		} catch (NumberFormatException e) {
		}

		return time;
	}

	public static boolean parseEnabled(String value) {
		if (value != null && value.charAt(0) == '1')
			return true;

		return false;
	}

	public String formatDateTime() {
		DateFormat f = SimpleDateFormat.getDateTimeInstance(
				SimpleDateFormat.MEDIUM, SimpleDateFormat.SHORT);
		String enabled = getContext().getString(R.string.pref_date_time_enabled);
		String disabled = getContext().getString(R.string.pref_date_time_disabled);

		return f.format(mCal.getTime()) + " ("
				+ (mEnabled ? enabled : disabled) + ")";
	}

	private void parseValue(String value) {
		mCal.setTimeInMillis(parseTime(value));
		mCal.set(Calendar.SECOND, 0);
		mEnabled = parseEnabled(value);
	}

	private String createValue() {
		String time = (mEnabled ? "1|" : "0|");
		time += String.valueOf(getTimeInMillis());

		return time;
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);

		CheckBox checkBox = (CheckBox) view
				.findViewById(R.id.dialog_date_preference_enabled);
		checkBox.setChecked(mEnabled);
		checkBox.setOnCheckedChangeListener(this);
	}

	@SuppressLint("NewApi")
	@Override
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);

		parseValue(getPersistedString(null));

		mDatePicker = (DatePicker) v.findViewById(R.id.dialog_date_preference_date);
		mTimePicker = (TimePicker) v.findViewById(R.id.dialog_date_preference_time);

		mDatePicker.setCalendarViewShown(false);
		mDatePicker.setMinDate(System.currentTimeMillis() - 1000);

		mDatePicker.updateDate(mCal.get(Calendar.YEAR),
				mCal.get(Calendar.MONTH), mCal.get(Calendar.DAY_OF_MONTH));

		mTimePicker.setIs24HourView(true);
		mTimePicker.setCurrentHour(mCal.get(Calendar.HOUR_OF_DAY));
		mTimePicker.setCurrentMinute(mCal.get(Calendar.MINUTE));
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult) {
			String time = createValue();

			if (callChangeListener(time)) {
				persistString(time);
			}
		}
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getString(index);
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		String time = null;

		if (restoreValue) {
			time = getPersistedString((String) defaultValue);
		} else {
			time = (String) defaultValue;
		}

		parseValue(time);
	}

	@Override
	public void onCheckedChanged(CompoundButton check, boolean value) {
		String persisted = getPersistedString(null);
		parseValue(persisted);

		mEnabled = value;

		String created = createValue();

		if (!persisted.equals(created)) {
			persistString(created);
		}
	}
}
