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

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import at.andreasrohner.spartantimelapserec.R;

public class SeekBarPreference extends DialogPreference implements
		SeekBar.OnSeekBarChangeListener {
	private static final int DEFAULT_VALUE = 0;

	private TextView mValueDisp;
	private SeekBar mSeekBar;
	private String mSuffix;
	private int mMaxValue, mMinValue, mValue = DEFAULT_VALUE;
	private OnFormatOutputValueListener mFormatListener;
	private float mLog;
	private int mStickyValue;

	public SeekBarPreference(Context context, AttributeSet attrs) {

		super(context, attrs);

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.TimeLapse);

		int resId = a.getResourceId(R.styleable.TimeLapse_suffix,0);
		if (resId == 0)
			mSuffix = a.getString(R.styleable.TimeLapse_suffix);
		else
			mSuffix = context.getString(resId);

		mMaxValue = a.getInteger(R.styleable.TimeLapse_max,  100);
		mMinValue = a.getInteger(R.styleable.TimeLapse_min,  0);
		mLog = a.getFloat(R.styleable.TimeLapse_log,  0);
		mStickyValue = a.getInteger(R.styleable.TimeLapse_stickyValue,  0);

		setDialogLayoutResource(R.layout.dialog_seekbar_preference);
	}

	public interface OnFormatOutputValueListener {
		String onFormatOutputValue(int value, String suffix);
	}

	public void setOnFormatOutputValueListener(
			OnFormatOutputValueListener listener) {
		mFormatListener = listener;
	}

	public void setMaxValue(int value) {
		mMaxValue = value;
		if (mSeekBar != null)
			mSeekBar.setMax(mMaxValue - mMinValue);
	}

	public void setMinValue(int value) {
		mMinValue = value;
	}

	private int getLogValue(int val) {
		if (mLog > 1)
			return (int) Math.round(((Math.log(val) / Math.log(mLog)) * 1000));
		return val;
	}

	private int getPowValue(int val) {
		if (mLog > 1) {
			val = (int) Math.round(Math.pow(mLog, ((double) val) / 1000));
			if (mStickyValue > 0) {
				int mod = val % mStickyValue;
				int area = 15 * mStickyValue / 100;
				if (val > mStickyValue) {
					if (mod < area || val > mStickyValue * 8)
						val -= mod;
					else if (mod > mStickyValue - area)
						val = val - mod + mStickyValue;
				}
			}
		}
		return val;
	}

	@Override
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);

		mValue = getPersistedInt(DEFAULT_VALUE);

		mValueDisp = (TextView) v
				.findViewById(R.id.dialog_seekbar_preference_value);

		mSeekBar = (SeekBar) v
				.findViewById(R.id.dialog_seekbar_preference_control);
		mSeekBar.setMax(getLogValue(mMaxValue - mMinValue));
		mSeekBar.setOnSeekBarChangeListener(this);
		mSeekBar.setProgress(getLogValue(mValue - mMinValue));
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			persistInt(mValue);
		}
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue,
			Object defaultValue) {
		if (restorePersistedValue) {
			// Restore existing state
			mValue = getPersistedInt(DEFAULT_VALUE);
		} else {
			// Set default state from the XML attribute
			mValue = (Integer) defaultValue;
			persistInt(mValue);
		}
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInteger(index, DEFAULT_VALUE);
	}

	@Override
	public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
		value = getPowValue(value);
		value += mMinValue;
		mValue = value;

		String text;
		if (mFormatListener != null) {
			text = mFormatListener.onFormatOutputValue(value, mSuffix);
		} else {
			text = String.valueOf(value);
			if (mSuffix != null)
				text += " " + mSuffix;
		}
		if (mValueDisp != null)
			mValueDisp.setText(text);
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
	}
}
