package at.andreasrohner.spartantimelapserec.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;

import java.util.LinkedHashMap;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import at.andreasrohner.spartantimelapserec.R;

/**
 * Camera 2 Settings
 */
public class Camera2SettingsActivity extends AbstractSettingsActivity {

	/**
	 * Constructor
	 */
	public Camera2SettingsActivity() {
		super(new SettingsFragment());
	}

	/**
	 * Settings fragment
	 */
	public static class SettingsFragment extends AbstractSettingsFragment {

		/**
		 * Constructor
		 */
		public SettingsFragment() {
			super(R.xml.camera2_preferences);
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			CameraManager manager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
			final ListPreference listPreference = (ListPreference) findPreference("pref_camera");

			try {
				Map<String, String> cameras = new LinkedHashMap<>();
				String firstCam = null;
				for (String cameraId : manager.getCameraIdList()) {
					if (firstCam == null) {
						firstCam = cameraId;
					}
					CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
					Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);

					String cam;
					if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
						cam = cameraId + ": " + getString(R.string.pref_camera_front);
					} else {
						cam = cameraId + ": " + getString(R.string.pref_camera_back);
					}

					cameras.put(cameraId, cam);
				}

				listPreference.setEntries(cameras.values().toArray(new String[0]));
				listPreference.setDefaultValue(firstCam);
				listPreference.setEntryValues(cameras.keySet().toArray(new String[0]));
			} catch (CameraAccessException e) {
				String[] array = {"Error loading camera list"};
				listPreference.setEntries(array);
				listPreference.setEntryValues(array);
				listPreference.setDefaultValue(array[0]);
				return;
			}
		}

		@Override
		protected void updateValues() {
			// Nothing to do
			ListPreference pref = (ListPreference) findPreference("pref_camera");
			pref.setSummary(pref.getValue());
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
			if ("pref_camera".equals(key)) {
				updateValues();
			}
		}
	}
}