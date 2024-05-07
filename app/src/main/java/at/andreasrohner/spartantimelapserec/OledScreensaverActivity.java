package at.andreasrohner.spartantimelapserec;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import at.andreasrohner.spartantimelapserec.databinding.ActivityOledScreensaverBinding;

/**
 * OLED State screen
 */
public class OledScreensaverActivity extends AppCompatActivity {

	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * Some older devices needs a small delay between UI widget updates
	 * and a change of the status and navigation bar.
	 */
	private static final int UI_ANIMATION_DELAY = 300;

	/**
	 * Refresh interval in ms
	 */
	private static final int REFRESH_INTERVAL = 10000;

	/**
	 * Date format
	 */
	private DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM, SimpleDateFormat.SHORT);

	private final Handler mHideHandler = new Handler(Looper.myLooper());

	private View mContentView;

	private final Runnable mHidePart2Runnable = new Runnable() {
		@SuppressLint("InlinedApi")
		@Override
		public void run() {
			// Delayed removal of status and navigation bar
			if (Build.VERSION.SDK_INT >= 30) {
				mContentView.getWindowInsetsController().hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
			} else {
				// Note that some of these constants are new as of API 16 (Jelly Bean)
				// and API 19 (KitKat). It is safe to use them, as they are inlined
				// at compile-time and do nothing on earlier devices.
				mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			}
		}
	};

	private View mControlsView;

	private final Runnable mShowPart2Runnable = new Runnable() {
		@Override
		public void run() {
			// Delayed display of UI elements
			ActionBar actionBar = getSupportActionBar();
			if (actionBar != null) {
				actionBar.show();
			}
			mControlsView.setVisibility(View.VISIBLE);
		}
	};

	private boolean mVisible;

	private final Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			hide();
		}
	};

	private ActivityOledScreensaverBinding binding;

	/**
	 * Status label
	 */
	private TextView mStatusLabel;

	/**
	 * Fullscreen layout
	 */
	private FrameLayout mFullscreenContent;

	/**
	 * Constructor
	 */
	public OledScreensaverActivity() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		binding = ActivityOledScreensaverBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		mVisible = true;
		mControlsView = binding.fullscreenContentControls;
		mContentView = binding.fullscreenContent;
		mStatusLabel = binding.statusLabel;
		mFullscreenContent = binding.fullscreenContent;

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		// Set up the user interaction to manually show or hide the system UI.
		mContentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				toggle();
			}
		});

		Timer t = new Timer();
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						updateText();
					}
				});
			}
		}, 10, REFRESH_INTERVAL);
	}

	/**
	 * Update Text, called in UI Thread
	 */
	private void updateText() {
		StringBuffer b = new StringBuffer();
		b.append(dateFormat.format(System.currentTimeMillis()));
		b.append('\n');
		b.append(BaseForegroundService.getStatus());
		if (ImageRecorderState.getRecordedImagesCount() > 0) {
			b.append("\n∑ ");
			b.append(ImageRecorderState.getRecordedImagesCount());
			b.append("\n");
			b.append(ImageRecorderState.getCurrentRecordedImage());
		}

		mStatusLabel.setText(b.toString());

		int w = mFullscreenContent.getWidth() / 2;
		int h = mFullscreenContent.getHeight() - mStatusLabel.getHeight();

		int left = (int) (Math.random() * w);
		int top = (int) (Math.random() * h);
		mStatusLabel.setPadding(left, top, 0, 0);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
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
	 * Show / hide menu
	 */
	private void toggle() {
		if (mVisible) {
			hide();
		} else {
			show();
		}
	}

	/**
	 * Hide menu
	 */
	private void hide() {
		// Hide UI first
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.hide();
		}
		mControlsView.setVisibility(View.GONE);
		mVisible = false;

		// Schedule a runnable to remove the status and navigation bar after a delay
		mHideHandler.removeCallbacks(mShowPart2Runnable);
		mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
	}

	/**
	 * Show menu
	 */
	private void show() {
		// Show the system bar
		if (Build.VERSION.SDK_INT >= 30) {
			mContentView.getWindowInsetsController().show(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
		} else {
			mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
		}
		mVisible = true;

		// Schedule a runnable to display UI elements after a delay
		mHideHandler.removeCallbacks(mHidePart2Runnable);
		mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
	}

	/**
	 * Schedules a call to hide() in delay milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}
}