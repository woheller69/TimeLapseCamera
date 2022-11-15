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

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.view.OrientationEventListener;

public class OrientationSensor extends OrientationEventListener {
	private int mOrientation;

	public OrientationSensor(Context context) {
		super(context);

	}

	@Override
	public void onOrientationChanged(int orientation) {
		if (orientation != ORIENTATION_UNKNOWN)
			mOrientation = orientation;
	}

	public int getCameraRotation(int cameraId) {
		Camera.CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(cameraId, info);

		mOrientation = (mOrientation + 45) / 90 * 90;
		int rotation = 0;
		if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
			rotation = (info.orientation - mOrientation + 360) % 360;
		} else { // back-facing camera
			rotation = (info.orientation + mOrientation) % 360;
		}

		return rotation;
	}
}
