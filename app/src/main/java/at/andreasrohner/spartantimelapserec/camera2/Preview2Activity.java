package at.andreasrohner.spartantimelapserec.camera2;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

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
	 * Listen for focus changed
	 */
	protected FocusChangeListener focusChangedListener = null;

	/**
	 * Toolbar
	 */
	private LinearLayout buttonLayout;

	/**
	 * Recording Info
	 */
	private LinearLayout recordingInfo;

	/**
	 * Recoding info Text View
	 */
	private TextView recordingInfoText;

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

		((ImageButton) findViewById(R.id.btn_takepicture)).setOnClickListener(v -> takePicture(null));

		buttonLayout = (LinearLayout) findViewById(R.id.buttonLayout);
		recordingInfo = (LinearLayout) findViewById(R.id.recordingInfo);
		recordingInfoText = (TextView) findViewById(R.id.recordingInfoText);

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
		runOnUiThread(() -> textureView.setOnTouchListener(touchFocusHandler));
	}

	@Override
	protected void onCameraConfigured() {
		touchFocusHandler = new CameraFocusOnTouchHandler(getApplicationContext(), characteristics, captureRequestBuilder, this.cameraCaptureSession, backgroundHandler, scaling);
		touchFocusHandler.loadLastFocusConfig();
		textureView.setOnTouchListener(touchFocusHandler);
		touchFocusHandler.setFocusChangeListener(this);
	}

	/**
	 * @param focusChangedListener Listen for focus changed
	 */
	public void setFocusChangedListener(FocusChangeListener focusChangedListener) {
		this.focusChangedListener = focusChangedListener;
	}

	@Override
	public void focusChanged(FocusState state) {
		updateFocusDisplay(state);

		FocusChangeListener l = focusChangedListener;
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
	 * Update recording info text
	 *
	 * @param text Text
	 */
	public void updateRecordingText(String text) {
		runOnUiThread(() -> updateRecordingTextUi(text));
	}

	/**
	 * Enable recording mode
	 */
	private void enableRecordingModeUi() {
		buttonLayout.setVisibility(View.GONE);
		recordingInfo.setVisibility(View.VISIBLE);
		textureView.setOnTouchListener(null);
		recordingInfoText.setText("");
	}

	/**
	 * Disable recording mode
	 */
	private void disableRecordingModeUi() {
		buttonLayout.setVisibility(View.VISIBLE);
		recordingInfo.setVisibility(View.GONE);
		textureView.setOnTouchListener(touchFocusHandler);
	}

	/**
	 * Update recording info text
	 *
	 * @param text Text
	 */
	private void updateRecordingTextUi(String text) {
		recordingInfoText.setText(text);
	}
}