package at.andreasrohner.spartantimelapserec.camera2;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageButton;

import androidx.appcompat.app.ActionBar;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.RecordingMenuHelper;
import at.andreasrohner.spartantimelapserec.ServiceHelper;

/**
 * Preview with Camera 2 Activity
 */
public class Preview2Activity extends AbstractPreview2Activity {

	/**
	 * Handle buttons and button actions of the camera preview
	 */
	protected CameraControlButtonHandler cameraControlButtonHandler;

	/**
	 * Touch / focus handler
	 */
	protected CameraFocusOnTouchHandler touchFocusHandler;

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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.preview2menu, menu);

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
		touchFocusHandler.setFocusChangeListener(state -> updateFocusDisplay(state));
	}
}