package at.andreasrohner.spartantimelapserec.camera2;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.ActionBar;
import androidx.core.view.MenuCompat;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.RecordingMenuHelper;
import at.andreasrohner.spartantimelapserec.ServiceHelper;

/**
 * Preview with Camera 2 Activity
 */
public class Preview2Activity extends AbstractPreview2Activity implements FocusChangeListener {

	/**
	 * Handle buttons and button actions of the camera preview
	 */
	protected CameraControlButtonHandler cameraControlButtonHandler;

	/**
	 * Touch / focus handler
	 */
	protected CameraFocusOnTouchHandler touchFocusHandler;

	/**
	 * Listen for one focus changed, then remove listener
	 */
	protected FocusChangeListener onceFocusChangedListener = null;

	/**
	 * Toolbar
	 */
	private LinearLayout cameraToolbar;

	/**
	 * Constructor
	 */
	public Preview2Activity() {
	}

	@Override
	protected void loadContentView() {
		setContentView(R.layout.activity_preview2);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Camera2PreviewRecorder.setPreview2Activity(this);

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		cameraControlButtonHandler = new CameraControlButtonHandler(this);
		cameraControlButtonHandler.setConfigChangeListener(this);

		((ImageButton) findViewById(R.id.btn_takepicture)).setOnClickListener(v -> takePicture());

		cameraToolbar = (LinearLayout) findViewById(R.id.cameraToolbar);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.preview2menu, menu);
		MenuCompat.setGroupDividerEnabled(menu, true);

		RecordingMenuHelper menuHelper = new RecordingMenuHelper(menu, getApplicationContext());
		menuHelper.setIdStart(R.id.prv_action_start);
		menuHelper.setIdStop(R.id.prv_action_stop);
		menuHelper.update();

		return true;
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
	 * Start Recording
	 *
	 * @param item Item
	 */
	public void actionStart(MenuItem item) {
		ServiceHelper helper = new ServiceHelper(getApplicationContext());
		helper.start(ServiceHelper.ServiceStartType.PREVIEW);

		invalidateOptionsMenu();
	}

	/**
	 * Stop Recording
	 *
	 * @param item Item
	 */
	public void actionStop(MenuItem item) {
		ServiceHelper helper = new ServiceHelper(getApplicationContext());
		helper.stop("Stop button pressed", false);

		invalidateOptionsMenu();
	}

	/**
	 * Focus again with the same settings
	 *
	 * @param item Item
	 */
	public void actionRefocus(MenuItem item) {
		focus();
	}

	/**
	 * Focus to the last configured settings
	 */
	public void focus() {
		touchFocusHandler.loadLastFocusConfig();
	}

	@Override
	protected synchronized void openCamera() {
		super.openCamera();
		cameraControlButtonHandler.cameraOpened(camera);
	}

	@Override
	public void takeImageFinished() {
		super.takeImageFinished();
		// Attach the Listener again, which was removed until the picture was saved
		textureView.setOnTouchListener(touchFocusHandler);
	}

	@Override
	protected void onCameraConfigured() {
		touchFocusHandler = new CameraFocusOnTouchHandler(getApplicationContext(), characteristics, captureRequestBuilder, this.cameraCaptureSession, backgroundHandler, scaling);
		touchFocusHandler.loadLastFocusConfig();
		textureView.setOnTouchListener(touchFocusHandler);
		touchFocusHandler.setFocusChangeListener(this);
	}

	/**
	 * @param onceFocusChangedListener Listen for one focus changed, then remove listener
	 */
	public void setOnceFocusChangedListener(FocusChangeListener onceFocusChangedListener) {
		this.onceFocusChangedListener = onceFocusChangedListener;
	}

	@Override
	public void focusChanged(FocusState state) {
		updateFocusDisplay(state);

		FocusChangeListener l = onceFocusChangedListener;
		if (l != null) {
			l.focusChanged(state);
		}
	}

	/**
	 * Enable recording mode
	 */
	public void enableRecordingMode() {
		runOnUiThread(() -> enableRecordingModeUi());
	}

	/**
	 * Disable recording mode
	 */
	public void disableRecordingMode() {
		runOnUiThread(() -> disableRecordingModeUi());
	}

	/**
	 * Enable recording mode
	 */
	private void enableRecordingModeUi() {
		cameraToolbar.setVisibility(View.GONE);
		textureView.setOnTouchListener(null);
	}

	/**
	 * Disable recording mode
	 */
	private void disableRecordingModeUi() {
		cameraToolbar.setVisibility(View.VISIBLE);
		textureView.setOnTouchListener(touchFocusHandler);
	}
}