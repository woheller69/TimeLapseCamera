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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.view.SurfaceHolder;
import at.andreasrohner.spartantimelapserec.PowerSavingReceiver;
import at.andreasrohner.spartantimelapserec.data.RecSettings;

public class PowerSavingImageRecorder extends ImageRecorder {
	private WakeLock mWakeLock;
	private AlarmManager mAlarmMgr;

	public PowerSavingImageRecorder(RecSettings settings,
			SurfaceHolder surfaceHolder, Context context, Handler handler,
			WakeLock wakeLock) {
		super(settings, surfaceHolder, context, handler);

		this.mWakeLock = wakeLock;

		mAlarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	}

	@Override
	public void stop() {
		if (mContext != null && mAlarmMgr != null) {
			Intent intent = new Intent(mContext, PowerSavingReceiver.class);
			PendingIntent alarmIntent = PendingIntent.getBroadcast(mContext, 0,
					intent, 0);
			mAlarmMgr.cancel(alarmIntent);
			mAlarmMgr = null;
		}

		super.stop();
	}

	protected void scheduleNextPicture() {
		if (mContext != null && mAlarmMgr != null) {
			Intent intent = new Intent(mContext, PowerSavingReceiver.class);
			PendingIntent alarmIntent = PendingIntent.getBroadcast(mContext, 0,
					intent, 0);

			long diffTime = SystemClock.elapsedRealtime() - mStartPreviewTime;
			long delay = mSettings.getCaptureRate() - diffTime;
			if (delay < 0)
				delay = 0;

			mAlarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
					SystemClock.elapsedRealtime() + delay, alarmIntent);
		}

		disableOrientationSensor();
		releaseCamera();
		unmuteShutter();

		mWakeLock.release();
	}
}
