package at.andreasrohner.spartantimelapserec.camera2;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import at.andreasrohner.spartantimelapserec.R;

/**
 * Setup external storage folder
 */
public class SetupExternalStorageActivity extends AppCompatActivity {

	/**
	 * Text display
	 */
	private TextView text;

	/**
	 * Preferences
	 */
	private SharedPreferences prefs;

	/**
	 * Enable storage checkbox
	 */
	private CheckBox enableExternalStorage;

	/**
	 * Constructor
	 */
	public SetupExternalStorageActivity() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		setContentView(R.layout.activity_setup_external_storage);
		Button bt = (Button) findViewById(R.id.btSetupExternalStorage);
		bt.setOnClickListener(l -> selectPath());

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		text = (TextView) findViewById(R.id.textTargetUrl);
		updateTextPath();

		enableExternalStorage = (CheckBox) findViewById(R.id.enableExternalStorage);
		enableExternalStorage.setChecked(prefs.getBoolean("external_storage_enabled", false));
		enableExternalStorage.setOnCheckedChangeListener((b, checked) -> {
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean("external_storage_enabled", checked);
			editor.apply();
			updateTextPath();
		});
	}

	/**
	 * Update displayed text
	 */
	private void updateTextPath() {
		String externalStoragePath = prefs.getString("external_storage_path", null);
		if (externalStoragePath == null) {
			text.setText("---");
		} else {
			text.setText(externalStoragePath);
		}
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
	 * Select Path
	 */
	private void selectPath() {
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
		selectPathResultLauncher.launch(intent);
	}

	/**
	 * Callback if the path is selected
	 */
	private ActivityResultLauncher<Intent> selectPathResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
		@Override
		public void onActivityResult(ActivityResult result) {
			if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) {
				return;
			}
			Uri currentUri = result.getData().getData();

			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("external_storage_path", String.valueOf(currentUri));
			editor.apply();
			updateTextPath();
		}
	});
}