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

package at.andreasrohner.spartantimelapserec.recorder;

import android.content.Context;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.data.RecSettingsLegacy;

public class VideoTimeLapseRecorder extends VideoRecorder {

	public VideoTimeLapseRecorder(RecSettingsLegacy settings, Context context, Handler handler, File outputDir) {
		super(settings, context, handler, outputDir);
	}

	protected void doRecord() throws IllegalStateException, IOException {
		mMediaRecorder.setOrientationHint(getCameraRotation(mSettings.getCameraId()));
		// no need for more sensor data
		disableOrientationSensor();

		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

		CamcorderProfile p = CamcorderProfile.get(mSettings.getCameraId(), mSettings.getRecProfile());
		p.videoFrameWidth = mSettings.getFrameWidth();
		p.videoFrameHeight = mSettings.getFrameHeight();
		mMediaRecorder.setProfile(p);

		mMediaRecorder.setCaptureRate(1000 / ((double) mSettings.getCaptureRate()));

		if (mRate != -1)
			mMediaRecorder.setVideoFrameRate(mRate);
		mMediaRecorder.setOutputFile(getOutputFile("mp4").getAbsolutePath());
		mMediaRecorder.setVideoSize(mSettings.getFrameWidth(), mSettings.getFrameHeight());

		if (mSettings.getStopRecAfter() > 0) {
			if (mRate != -1) {
				mRate = CamcorderProfile.get(mSettings.getCameraId(), mSettings.getRecProfile()).videoFrameRate;
			}

			int duration = (int) (((double) (mSettings.getStopRecAfter() / mSettings.getCaptureRate())) / mRate * 1000); //Duration of the video
			if (duration < 500) {
				handleError(getClass().getSimpleName(), mContext.getString(R.string.pref_stop_recording_after) + " is too short in relation to the " + mContext.getString(R.string.pref_capture_rate));
				Toast.makeText(mContext, mContext.getString(R.string.error_too_short), Toast.LENGTH_SHORT).show();
				return;
			}

			mMediaRecorder.setMaxDuration(duration);
			mMediaRecorder.setOnInfoListener(this);
		}

		Log.i(getClass().getSimpleName(), "Starting video recording");
		mMediaRecorder.setOnErrorListener(this);
		if (mSettings.getVideoEncodingBitRate() > 0)
			mMediaRecorder.setVideoEncodingBitRate(mSettings.getVideoEncodingBitRate());
		mMediaRecorder.prepare();

		mMediaRecorder.start();
	}
}
