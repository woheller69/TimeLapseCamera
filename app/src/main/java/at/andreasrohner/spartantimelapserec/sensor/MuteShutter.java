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

import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;

public class MuteShutter {
	private int mode;
	private int ringerMode;
	private static final int[] streams = new int[] { AudioManager.STREAM_DTMF,
			AudioManager.STREAM_MUSIC, AudioManager.STREAM_NOTIFICATION,
			AudioManager.STREAM_SYSTEM };
	private int[] streamVolumes = new int[streams.length];
	private AudioManager audioManager;
	private NotificationManager notificationManager;
	private final Handler handler = new Handler();
	private volatile boolean muted;

	public MuteShutter(Context context) {
		this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		muted = false;
	}

	private void storeSoundSettings() {
		mode = audioManager.getMode();
		ringerMode = audioManager.getRingerMode();

		for (int i = 0; i < streams.length; ++i)
			streamVolumes[i] = audioManager.getStreamVolume(streams[i]);
	}

	private void recoverSoundSettings() {
		audioManager.setMode(mode);
		audioManager.setRingerMode(ringerMode);

		for (int i = 0; i < streams.length; ++i)
			audioManager.setStreamVolume(streams[i], streamVolumes[i],
					AudioManager.FLAG_ALLOW_RINGER_MODES);
	}

	public void maxAllStreams() {
		for (int stream : streams) {
			audioManager.setStreamMute(stream, false);
			audioManager.setStreamVolume(stream, audioManager.getStreamMaxVolume(stream),
					AudioManager.FLAG_ALLOW_RINGER_MODES);
		}
	}

	public synchronized void unmuteShutter() {
		if (!muted)
			return;

		maxAllStreams();

		recoverSoundSettings();
		muted = false;
	}

	private void restartHandler() {
		// remove all Runnables
		handler.removeCallbacksAndMessages(null);
		handler.postDelayed(new Runnable() {
			public void run() {
				unmuteShutter();
			}
		}, 2000);
	}

	public synchronized void muteShutter() {
		if (muted) {
			restartHandler();
			return;
		}

		storeSoundSettings();

		for (int stream : streams) {
			audioManager.setStreamVolume(stream, 0,
					AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
			audioManager.setStreamMute(stream, true);
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
			if (notificationManager.isNotificationPolicyAccessGranted()) audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		} else {
			audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		}

		muted = true;

		restartHandler();
	}
}
