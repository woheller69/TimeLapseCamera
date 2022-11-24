/*
 * Spartan Time Lapse Recorder - Minimalistic android time lapse recording app
 * Copyright (C) 2014  Andreas Rohner
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.andreasrohner.spartantimelapserec.sensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;

public class CameraSettings {

	private Camera.Parameters[] cameraParams;

	private synchronized Camera.Parameters getCameraParameters(int camId) {
		if (cameraParams == null)
			cameraParams = new Camera.Parameters[Camera.getNumberOfCameras()];

		Camera.Parameters params = cameraParams[camId];
		if (params == null) {
			Camera camera = Camera.open(camId);
			params = camera.getParameters();
			camera.release();
			cameraParams[camId] = params;
		}

		return params;
	}

	private Set<String> getStringSet(SharedPreferences prefs, String key,
			Set<String> defValues) {
		return prefs.getStringSet(key, defValues);
	}

	private void putStringSet(SharedPreferences prefs, String key,
			Set<String> set) {
		prefs.edit().putStringSet(key, set).commit();
		return;

	}

	private void addProfileFrameRate(int camId, List<Integer> list, int profile) {
		try {
			if (CamcorderProfile.hasProfile(camId, profile)) {
				CamcorderProfile p = CamcorderProfile.get(camId, profile);
				list.add(p.videoFrameRate);
			}
		} catch (Exception e) {
		}
	}

	private List<Integer> getFrameRatesFromCameraProfile(int camId) {
		List<Integer> list = new ArrayList<Integer>();

		addProfileFrameRate(camId, list, CamcorderProfile.QUALITY_HIGH);
		addProfileFrameRate(camId, list, CamcorderProfile.QUALITY_LOW);
		addProfileFrameRate(camId, list, CamcorderProfile.QUALITY_CIF);
		addProfileFrameRate(camId, list, CamcorderProfile.QUALITY_1080P);
		addProfileFrameRate(camId, list, CamcorderProfile.QUALITY_720P);
		addProfileFrameRate(camId, list, CamcorderProfile.QUALITY_480P);
		addProfileFrameRate(camId, list, CamcorderProfile.QUALITY_QCIF);
		addProfileFrameRate(camId, list, CamcorderProfile.QUALITY_QVGA);

		// deduplicate sizes
		Set<Integer> set = new TreeSet<Integer>(list);
		list.clear();
		list.addAll(set);

		// sort
		Collections.sort(list);

		return list;
	}

	private List<Integer> getFrameRatesFromCamera(int camId) {
		Camera.Parameters params = getCameraParameters(camId);

		List<int[]> ranges = params.getSupportedPreviewFpsRange();
		List<Integer> fpsList = new ArrayList<Integer>();
		int prevFps = -1;

		if (ranges == null)
			return getFrameRatesFromCameraProfile(camId);

		for (int i = 0; i < ranges.size(); ++i) {
			int fps = ranges.get(i)[Camera.Parameters.PREVIEW_FPS_MAX_INDEX] / 1000;
			if (prevFps == fps)
				continue;
			prevFps = fps;

			fpsList.add(fps);
		}

		return fpsList;
	}

	public void prefetch(final SharedPreferences prefs) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < Camera.getNumberOfCameras(); ++i) {
					getFrameRates(prefs, i);
				}
			}
		}).start();
	}

	public List<Integer> getFrameRates(SharedPreferences prefs, int camId) {
		List<Integer> fpsIntList;
		Set<String> fpsList = getStringSet(prefs, "pref_frame_rate_values_"
				+ camId, null);

		if (fpsList == null) {
			fpsList = new TreeSet<String>();
			fpsIntList = getFrameRatesFromCamera(camId);

			for (Integer fps : fpsIntList) {
				fpsList.add(fps.toString());
			}

			putStringSet(prefs, "pref_frame_rate_values_" + camId, fpsList);
			return fpsIntList;
		}

		fpsIntList = new ArrayList<Integer>();
		for (String fps : fpsList) {
			try {
				fpsIntList.add(Integer.valueOf(fps));
			} catch (NumberFormatException e) {
			}
		}

		Collections.sort(fpsIntList);

		return fpsIntList;
	}

	private void addProfileFrameSize(int camId, List<int[]> list, int profile) {
		try {
			if (CamcorderProfile.hasProfile(camId, profile)) {
				CamcorderProfile p = CamcorderProfile.get(camId, profile);
				list.add(new int[] { p.videoFrameWidth, p.videoFrameHeight });
			}
		} catch (Exception e) {
		}
	}

	private void prepareSizeList(List<int[]> sizes) {
		Comparator<int[]> comp = new Comparator<int[]>() {
			@Override
			public int compare(int[] lhs, int[] rhs) {
				if (lhs[0] > rhs[0])
					return 1;
				if (lhs[0] < rhs[0])
					return -1;
				if (lhs[1] > rhs[1])
					return 1;
				if (lhs[1] < rhs[1])
					return -1;
				return 0;
			}
		};

		// deduplicate sizes
		Set<int[]> set = new TreeSet<int[]>(comp);
		set.addAll(sizes);
		sizes.clear();
		sizes.addAll(set);

		// sort
		Collections.sort(sizes, comp);
	}

	private List<int[]> getFrameSizesFromProfiles(int camId, boolean timeLapse) {
		List<int[]> list = new ArrayList<int[]>();

		if (timeLapse) {
			addProfileFrameSize(camId, list,
					CamcorderProfile.QUALITY_TIME_LAPSE_HIGH);
			addProfileFrameSize(camId, list,
					CamcorderProfile.QUALITY_TIME_LAPSE_LOW);
			addProfileFrameSize(camId, list,
					CamcorderProfile.QUALITY_TIME_LAPSE_CIF);
			addProfileFrameSize(camId, list,
					CamcorderProfile.QUALITY_TIME_LAPSE_1080P);
			addProfileFrameSize(camId, list,
					CamcorderProfile.QUALITY_TIME_LAPSE_720P);
			addProfileFrameSize(camId, list,
					CamcorderProfile.QUALITY_TIME_LAPSE_480P);
			addProfileFrameSize(camId, list,
					CamcorderProfile.QUALITY_TIME_LAPSE_QCIF);
			addProfileFrameSize(camId, list,
					CamcorderProfile.QUALITY_TIME_LAPSE_QVGA);
		} else {
			addProfileFrameSize(camId, list, CamcorderProfile.QUALITY_HIGH);
			addProfileFrameSize(camId, list, CamcorderProfile.QUALITY_LOW);
			addProfileFrameSize(camId, list, CamcorderProfile.QUALITY_CIF);
			addProfileFrameSize(camId, list, CamcorderProfile.QUALITY_1080P);
			addProfileFrameSize(camId, list, CamcorderProfile.QUALITY_720P);
			addProfileFrameSize(camId, list, CamcorderProfile.QUALITY_480P);
			addProfileFrameSize(camId, list, CamcorderProfile.QUALITY_QCIF);
			addProfileFrameSize(camId, list, CamcorderProfile.QUALITY_QVGA);
		}

		prepareSizeList(list);

		return list;
	}

	public List<int[]> getFrameSizes(SharedPreferences prefs, int camId,
			boolean timeLapse) {

		Set<String> sizes = getStringSet(prefs, "pref_frame_size_values_"
				+ camId, null);
		List<int[]> sizesList = new ArrayList<int[]>();

		if (sizes == null) {
			Camera.Parameters params = getCameraParameters(camId);

			List<Camera.Size> suppSizes = params.getSupportedVideoSizes();
			if (suppSizes == null)
				return getFrameSizesFromProfiles(camId, timeLapse);

			sizes = new TreeSet<String>();
			for (Size s : suppSizes) {
				sizes.add(s.width + "x" + s.height);
				sizesList.add(new int[] { s.width, s.height });
			}

			putStringSet(prefs, "pref_frame_size_values_" + camId, sizes);

			prepareSizeList(sizesList);

			return sizesList;
		}

		for (String s : sizes) {
			String[] tmp = s.split("x");
			sizesList.add(new int[] { Integer.valueOf(tmp[0]),
					Integer.valueOf(tmp[1]) });
		}

		prepareSizeList(sizesList);

		return sizesList;
	}

	public List<int[]> getPictureSizes(SharedPreferences prefs, int camId) {
		Set<String> sizes = getStringSet(prefs, "pref_picture_size_values_"
				+ camId, null);
		List<int[]> sizesList = new ArrayList<int[]>();

		if (sizes == null) {
			Camera.Parameters params = getCameraParameters(camId);

			sizes = new TreeSet<String>();
			for (Size s : params.getSupportedPictureSizes()) {
				sizes.add(s.width + "x" + s.height);
				sizesList.add(new int[] { s.width, s.height });
			}

			putStringSet(prefs, "pref_picture_size_values_" + camId, sizes);

			prepareSizeList(sizesList);

			return sizesList;
		}

		for (String s : sizes) {
			String[] tmp = s.split("x");
			sizesList.add(new int[] { Integer.valueOf(tmp[0]),
					Integer.valueOf(tmp[1]) });
		}

		prepareSizeList(sizesList);

		return sizesList;
	}

	public int getMinExposureCompensation(int camId){
		Camera.Parameters params = getCameraParameters(camId);
		return params.getMinExposureCompensation();
	}

	public int getMaxExposureCompensation(int camId){
		Camera.Parameters params = getCameraParameters(camId);
		return params.getMaxExposureCompensation();
	}

	public int getMaxZoom(int camId){
		Camera.Parameters params = getCameraParameters(camId);
		if (params.isZoomSupported())
			return params.getMaxZoom();
		else
			return 0;
	}

}
