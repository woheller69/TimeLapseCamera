package at.andreasrohner.spartantimelapserec.state;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import at.andreasrohner.spartantimelapserec.R;

/**
 * Log list display
 */
public class LogDisplayAdapter extends ArrayAdapter<StateLogEntry> {

	/**
	 * Simple Date format
	 */
	private DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM, SimpleDateFormat.MEDIUM);

	/**
	 * Log Rows
	 */
	private List<StateLogEntry> logs;

	/**
	 * Constructor
	 *
	 * @param context Context
	 * @param logs    Log Entries
	 */
	public LogDisplayAdapter(Context context, List<StateLogEntry> logs) {
		super(context, R.layout.log_row, logs);
		this.logs = logs;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View rowView = inflater.inflate(R.layout.log_row, parent, false);

		StateLogEntry entry = logs.get(position);
		TextView logHeader = (TextView) rowView.findViewById(R.id.log_header);
		logHeader.setText(entry.getHeader());
		TextView logTimestamp = (TextView) rowView.findViewById(R.id.log_timestamp);
		logTimestamp.setText(dateFormat.format(entry.getTimestamp()));
		TextView logLine = (TextView) rowView.findViewById(R.id.log_line);
		logLine.setText(entry.getLine());

		switch (entry.getLevel()) {
			case ERROR:
				rowView.setBackgroundColor(0xFFffcccc);
				break;
			case WARN:
				rowView.setBackgroundColor(0xFFffe6cc);
				break;
			case MARK:
				rowView.setBackgroundColor(0xFF99ccff);
				break;
		}

		return rowView;
	}
}