package at.andreasrohner.spartantimelapserec.camera2.pupcfg;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.List;

import at.andreasrohner.spartantimelapserec.R;

/**
 * Helper to control selection
 *
 * @param <T> Type of the Spinner model
 */
public class SpinnerHelper<T extends IdData> {

	/**
	 * Spinner
	 */
	private final Spinner spinner;

	/**
	 * Context
	 */
	private final Context context;

	/**
	 * Data List
	 */
	private List<T> data;

	/**
	 * Value change Listener
	 */
	private ChangeListener valueChangeListener;

	/**
	 * Constructor
	 *
	 * @param spinner Spinner
	 * @param context Context
	 */
	public SpinnerHelper(Spinner spinner, Context context) {
		this.spinner = spinner;
		this.context = context;

		this.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				valueChanged();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				valueChanged();
			}
		});
	}

	/**
	 * Set the data list
	 *
	 * @param data Data List
	 */
	public void setData(List<T> data) {
		this.data = data;

		ArrayAdapter<CharSequence> adapter = new ArrayAdapter(context, R.layout.center_selection_text, data);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		this.spinner.setAdapter(adapter);
	}

	/**
	 * Set listener which is called on value changed
	 *
	 * @param valueChangeListener Change listener
	 */
	public void setValueChangeListener(ChangeListener valueChangeListener) {
		this.valueChangeListener = valueChangeListener;
	}

	private void valueChanged() {
		if (this.valueChangeListener == null) {
			return;
		}

		this.valueChangeListener.valueChanged();
	}

	/**
	 * @return Gets the selected item
	 */
	public T getSelectedItem() {
		return (T) this.spinner.getSelectedItem();
	}

	/**
	 * Select entry by id
	 *
	 * @param id ID
	 */
	public void selectById(String id) {
		if (id == null) {
			return;
		}

		int selectedCameraIndex = -1;
		for (int i = 0; i < data.size(); i++) {
			if (id.equals(data.get(i).getId())) {
				selectedCameraIndex = i;
				break;
			}
		}

		if (selectedCameraIndex != -1) {
			this.spinner.setSelection(selectedCameraIndex);
		}
	}
}
