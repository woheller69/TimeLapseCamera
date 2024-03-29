package at.andreasrohner.spartantimelapserec;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;

import java.io.File;

import androidx.core.app.NotificationManagerCompat;
import at.andreasrohner.spartantimelapserec.data.RecSettings;
import at.andreasrohner.spartantimelapserec.recorder.Recorder;

public class ForegroundService extends Service implements Handler.Callback {

    public static final String ACTION_STOP_SERVICE = "TimeLapse.action.STOP_SERVICE";

    public static final int LED_NOTIFICATION_ID = 1001;

    public static boolean mIsRunning = false;

    private RecSettings settings;

    private Recorder recorder;

    private HandlerThread handlerThread;

    private WakeLock mWakeLock;

    private static statusListener listener;

    private NotificationManager mNotificationManager;

    public interface statusListener {

        void onServiceStatusChange(boolean status);
    }

    public static void registerStatusListener(statusListener l) {
        listener = l;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || !ACTION_STOP_SERVICE.equals(intent.getAction())) {
            initNotif();
            mIsRunning = true;

            settings = new RecSettings();
            settings.load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

            if (settings.isSchedRecEnabled() && settings.getSchedRecTime() > System.currentTimeMillis() + 10000) {
                AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent newintent = new Intent(getApplicationContext(), ScheduleReceiver.class);
                PendingIntent alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, newintent, PendingIntent.FLAG_IMMUTABLE);
                alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, settings.getSchedRecTime(), alarmIntent);

            } else {

                PowerManager mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
                mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
                mWakeLock.acquire();

                handlerThread = new HandlerThread("recordingVideo");
                handlerThread.start();

                Context context = getApplicationContext();
                Handler handler = new Handler(handlerThread.getLooper(), this);

                recorder = Recorder.getInstance(settings, context, handler, mWakeLock);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        recorder.start();
                    }
                });
                updateNotif();
            }

            if (listener != null)
                listener.onServiceStatusChange(true);
            return START_STICKY;
        } else {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            intent = new Intent(this, ScheduleReceiver.class);
            PendingIntent alarmIntent;
            alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
            alarmManager.cancel(alarmIntent);

            stop();
            return START_NOT_STICKY;
        }
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void stop() {

        File projectDir = null;

        if (recorder != null) {
            recorder.stop();
            projectDir = recorder.getOutputDir();
            recorder = null;
        }

        if (mWakeLock != null && mWakeLock.isHeld())
            mWakeLock.release();

        if (projectDir != null && projectDir.exists())
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(projectDir)));

        mIsRunning = false;
        if (listener != null)
            listener.onServiceStatusChange(false);
        stopForeground(true);
        stopSelf();
    }

    private static final int NOTIF_ID = 123;

    private static final String CHANNEL_ID = "TimeLapseID";

    private void updateNotif() {

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pi = PendingIntent.getActivity(this, NOTIF_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new Builder(this, CHANNEL_ID).setSilent(true).setOnlyAlertOnce(true).setPriority(NotificationCompat.PRIORITY_DEFAULT) // For N and below
              .setContentIntent(pi).setSmallIcon(R.drawable.ic_camera).setAutoCancel(false).setOngoing(true).setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setContentText(getString(R.string.info_recording_running)).setContentTitle(getString(R.string.app_name)).build();

        mNotificationManager.notify(NOTIF_ID, notification);
    }

    private void initNotif() {

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            mNotificationManager.createNotificationChannel(new NotificationChannel(CHANNEL_ID, "TimeLapse", NotificationManager.IMPORTANCE_DEFAULT));
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pi = PendingIntent.getActivity(this, NOTIF_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // For N and below
        Notification notification = new Builder(this, CHANNEL_ID).setSilent(true).setOnlyAlertOnce(true).setPriority(NotificationCompat.PRIORITY_DEFAULT) // For N and below
              .setContentIntent(pi).setSmallIcon(R.drawable.ic_camera).setAutoCancel(false).setOngoing(true).setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setContentText(getString(R.string.notification_preparing)).setContentTitle(getString(R.string.app_name)).build();

        if (VERSION.SDK_INT >= VERSION_CODES.Q) {
            startForeground(NOTIF_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA);
        } else {
            startForeground(NOTIF_ID, notification);
        }

    }

    @Override
    public boolean handleMessage(Message m) {
        String status = m.getData().getString("status");
        String tag = m.getData().getString("tag");
        String msg = m.getData().getString("msg");

        if ("error".equals(status)) {
            Log.e(tag, "Error: " + msg);
            stop();
        } else if ("success".equals(status)) {
            Log.e(tag, "Success");
            stop();
        } else if ("image".equals(status)) {
            int count = m.getData().getInt("id");
            Log.e(tag, "Image count: " + count);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // clear previous notification
            notificationManager.cancel(LED_NOTIFICATION_ID);

            Builder notification = new Builder(this, CHANNEL_ID)
                  .setPriority(NotificationCompat.PRIORITY_HIGH) // For N and below
                  .setSmallIcon(R.drawable.ic_camera)
                  .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                  .setContentText(String.valueOf(count))
                  .setContentTitle(getString(R.string.notification_took_img))
                  .setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_SOUND);

            // Show different notifications
            if (count % 2 == 0) {
                notification.setLights(Color.MAGENTA, 100, 100);
            } else {
                notification.setLights(Color.CYAN, 100, 100);
            }

            notificationManager.notify(LED_NOTIFICATION_ID, notification.build());
        }

        return true;
    }

}

