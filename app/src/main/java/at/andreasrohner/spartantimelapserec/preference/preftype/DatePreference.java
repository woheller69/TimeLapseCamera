package at.andreasrohner.spartantimelapserec.preference.preftype;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

/**
 * Allow to select a date
 */
public class DatePreference extends DialogPreference {

	/**
	 * Date
	 */
	private String dateValue;

	/**
	 * Constructor
	 *
	 * @param context Context
	 * @param attrs   AttributeSet
	 */
	public DatePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getString(index);
	}

	@Override
	protected void onSetInitialValue(Object defaultValue) {
		setDate(getPersistedString((String) defaultValue));
	}

	/**
	 * Gets the date as a string from the current data storage.
	 *
	 * @return string representation of the date.
	 */
	public String getDate() {
		return dateValue;
	}

	/**
	 * Saves the date as a string in the current data storage.
	 *
	 * @param text string representation of the date to save.
	 */
	public void setDate(String text) {
		final boolean wasBlocking = shouldDisableDependents();

		this.dateValue = text;

		setSummary(text);

		persistString(text);

		final boolean isBlocking = shouldDisableDependents();
		if (isBlocking != wasBlocking) {
			notifyDependencyChange(isBlocking);
		}

		notifyChanged();
	}

	/**
	 * A simple {@link androidx.preference.Preference.SummaryProvider} implementation for an
	 * {@link DatePreference}. If no value has been set, the summary displayed will be 'Not
	 * set', otherwise the summary displayed will be the value set for this preference.
	 */
	@SuppressWarnings("unused") // Loaded by reflection
	public static final class SimpleSummaryProvider implements SummaryProvider<DatePreference> {

		/**
		 * Instance
		 */
		private static SimpleSummaryProvider simpleSummaryProvider;

		/**
		 * Constructor
		 */
		private SimpleSummaryProvider() {
		}

		/**
		 * Retrieve a singleton instance of this simple
		 * {@link androidx.preference.Preference.SummaryProvider} implementation.
		 *
		 * @return a singleton instance of this simple
		 * {@link androidx.preference.Preference.SummaryProvider} implementation
		 */
		public static SimpleSummaryProvider getInstance() {
			if (simpleSummaryProvider == null) {
				simpleSummaryProvider = new SimpleSummaryProvider();
			}
			return simpleSummaryProvider;
		}

		@Override
		public CharSequence provideSummary(DatePreference preference) {
			if (TextUtils.isEmpty(preference.getDate())) {
				return "";
			} else {
				return preference.getDate();
			}
		}
	}
}