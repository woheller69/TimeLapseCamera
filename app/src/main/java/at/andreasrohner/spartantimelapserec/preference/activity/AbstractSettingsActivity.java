package at.andreasrohner.spartantimelapserec.preference.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.preference.AbstractSettingsFragment;

/**
 * Base class for settings activities
 */
public abstract class AbstractSettingsActivity extends AppCompatActivity {

	/**
	 * Settings Fragment
	 */
	private final AbstractSettingsFragment fragment;

	/**
	 * Constructor
	 *
	 * @param fragment Settings Fragment
	 */
	public AbstractSettingsActivity(AbstractSettingsFragment fragment) {
		this.fragment = fragment;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_activity);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().replace(R.id.settings, fragment).commit();
		}
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
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
}
