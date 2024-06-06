package at.andreasrohner.spartantimelapserec.state;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.camera2.pupcfg.SpinnerHelper;

/**
 * Show log activity
 */
public class LogActivity extends AppCompatActivity {

	/**
	 * Log list
	 */
	private ListView logList;

	/**
	 * Constructor
	 */
	public LogActivity() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log);
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		this.logList = (ListView) findViewById(R.id.log_list);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.logmenu, menu);
		return true;
	}

	/**
	 * Configure displayed log level
	 *
	 * @param item Menu
	 */
	public void actionViewLogLevel(MenuItem item) {
		configureLogLevel("log_display_level", R.string.action_view_log_level, (newLevel) -> StateLog.loadLogLevel(LogActivity.this));
	}

	/**
	 * Enable / Disable Logfile
	 *
	 * @param item Menu
	 */
	public void actionLogToFile(MenuItem item) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.action_log_to_file);
		builder.setMessage(getText(R.string.log_file_dialog_enabled_info).toString().replace("{}", LogFileWriter.getLogFolder().getAbsolutePath()));

		View view = View.inflate(this, R.layout.dialog_logfile, null);
		builder.setView(view);
		CheckBox checkbox = (CheckBox) view.findViewById(R.id.log_enabled);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean enabled = prefs.getBoolean("log_file_enabled", false);
		checkbox.setChecked(enabled);

		builder.setPositiveButton(R.string.dialog_OK_button, (dialog, which) -> {
			boolean newEnabled = checkbox.isChecked();
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean("log_file_enabled", newEnabled);
			editor.apply();
			LogFileWriter.setEnabled(newEnabled);
		});
		builder.setNegativeButton(R.string.dialog_CANCEL_button, (dialog, which) -> {
			dialog.cancel();
		});
		builder.show();
	}

	/**
	 * Configure logged log level
	 *
	 * @param item Menu
	 */
	public void actionLogLogLevel(MenuItem item) {
		configureLogLevel("log_file_level", R.string.action_file_log_level, (newLevel) -> LogFileWriter.setLevel(newLevel));
	}

	/**
	 * Show the level Config
	 *
	 * @param configKey Config Key
	 * @param title     Title ID
	 * @param callback  Log level change callback
	 */
	private void configureLogLevel(String configKey, int title, LogLevelChanged callback) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(this.getString(title));
		builder.setMessage(R.string.log_dialog_info);

		View view = View.inflate(this, R.layout.dialog_loglevel, null);
		builder.setView(view);
		Spinner logLevel = (Spinner) view.findViewById(R.id.log_level);

		SpinnerHelper<LogLevel> selection = new SpinnerHelper<>((Spinner) view.findViewById(R.id.log_level), this);
		ArrayList<LogLevel> displayLevel = new ArrayList<>();
		displayLevel.add(LogLevel.DEBUG);
		displayLevel.add(LogLevel.INFO);
		displayLevel.add(LogLevel.WARN);
		displayLevel.add(LogLevel.ERROR);
		selection.setData(displayLevel);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		int level = prefs.getInt(configKey, LogLevel.INFO.LEVEL);
		selection.selectById(String.valueOf(level));

		builder.setPositiveButton(R.string.dialog_OK_button, (dialog, which) -> {
			int newLevel = selection.getSelectedItem().LEVEL;
			SharedPreferences.Editor editor = prefs.edit();
			editor.putInt(configKey, newLevel);
			editor.apply();
			callback.logLevelChanged(newLevel);
		});
		builder.setNegativeButton(R.string.dialog_CANCEL_button, (dialog, which) -> {
			dialog.cancel();
		});
		builder.show();
	}

	@Override
	protected void onResume() {
		super.onResume();

		this.logList.setAdapter(new LogDisplayAdapter(this, StateLog.getInstance().getLog()));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Log level has been changed
	 */
	private interface LogLevelChanged {

		/**
		 * Callback if the level is changed
		 *
		 * @param newLevel New Log Level
		 */
		void logLevelChanged(int newLevel);
	}
}