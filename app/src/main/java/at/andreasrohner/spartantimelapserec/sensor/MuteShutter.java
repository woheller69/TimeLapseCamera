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
import android.media.AudioManager;
import android.os.Handler;

public class MuteShutter {
	private int mode;
	private int ringerMode;
	private static final int[] streams = new int[] { AudioManager.STREAM_DTMF,
			AudioManager.STREAM_MUSIC, AudioManager.STREAM_NOTIFICATION,
			AudioManager.STREAM_SYSTEM };
	private int[] streamVolumes = new int[streams.length];
	private AudioManager manager;
	private final Handler handler = new Handler();
	private volatile boolean muted;

	public MuteShutter(Context context) {
		this.manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		muted = false;
	}

	private void storeSoundSettings() {
		mode = manager.getMode();
		ringerMode = manager.getRingerMode();

		for (int i = 0; i < streams.length; ++i)
			streamVolumes[i] = manager.getStreamVolume(streams[i]);
	}

	private void recoverSoundSettings() {
		manager.setMode(mode);
		manager.setRingerMode(ringerMode);

		for (int i = 0; i < streams.length; ++i)
			manager.setStreamVolume(streams[i], streamVolumes[i],
					AudioManager.FLAG_ALLOW_RINGER_MODES);
	}

	public void maxAllStreams() {
		for (int stream : streams) {
			manager.setStreamMute(stream, false);
			manager.setStreamVolume(stream, manager.getStreamMaxVolume(stream),
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
			manager.setStreamVolume(stream, 0,
					AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
			manager.setStreamMute(stream, true);
		}

		manager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		muted = true;

		restartHandler();
	}
}
