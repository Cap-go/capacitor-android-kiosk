package ee.forgr.plugin.android_kiosk;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class KeepAliveService extends Service {

    private static final String TAG = "KeepAliveService";
    private static final String CHANNEL_ID = "KioskKeepAliveChannel";

    private static volatile boolean sRunning = false;
    private final Handler bringToFrontHandler = new Handler(Looper.getMainLooper());
    private Runnable bringToFrontRunnable;

    public static boolean isRunning() {
        return sRunning;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            if (sRunning) {
                Log.w(TAG, "Service already running, stopping duplicate instance");
                stopSelf();
                return;
            }

            sRunning = true;
            createNotificationChannel();
        } catch (Exception e) {
            Log.e(TAG, "Failed to create service: " + e.getMessage(), e);
            sRunning = false;
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            startForegroundWithNotification();
            scheduleBringToFront();
        } catch (Exception e) {
            Log.e(TAG, "Failed to start foreground in onStartCommand: " + e.getMessage(), e);
            stopSelf();
            return START_NOT_STICKY;
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (bringToFrontRunnable != null) {
            bringToFrontHandler.removeCallbacks(bringToFrontRunnable);
        }
        super.onDestroy();
        sRunning = false;
    }

    /**
     * When {@link KioskWatchdogScheduler}'s relaunch watchdog is enabled, periodically bring the main
     * launcher activity to front while the kiosk session is active. Uses the same prefs and interval
     * as the alarm watchdog; skipped entirely when relaunch is off.
     */
    private void scheduleBringToFront() {
        SharedPreferences prefs = getSharedPreferences(KioskPrefs.PREFS_NAME, MODE_PRIVATE);
        if (!prefs.getBoolean(KioskWatchdogScheduler.KEY_WATCHDOG_ENABLED, false)) {
            if (bringToFrontRunnable != null) {
                bringToFrontHandler.removeCallbacks(bringToFrontRunnable);
            }
            return;
        }

        if (bringToFrontRunnable == null) {
            bringToFrontRunnable = () -> {
                if (!sRunning) return;
                SharedPreferences p = getSharedPreferences(KioskPrefs.PREFS_NAME, MODE_PRIVATE);
                long nextDelayMs = relaunchIntervalMs(p);
                try {
                    if (
                        p.getBoolean(KioskWatchdogScheduler.KEY_WATCHDOG_ENABLED, false) &&
                        p.getBoolean(KioskPrefs.KEY_KIOSK_SESSION_ACTIVE, false)
                    ) {
                        try {
                            Intent launchIntent = KioskLaunchIntents.resolveLaunchIntent(KeepAliveService.this);
                            if (launchIntent != null) {
                                KioskRelaunchHelper.startLaunchActivity(KeepAliveService.this, launchIntent);
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Bring to front failed: " + e.getMessage());
                        }
                    }
                } finally {
                    if (sRunning && bringToFrontRunnable != null && p.getBoolean(KioskWatchdogScheduler.KEY_WATCHDOG_ENABLED, false)) {
                        bringToFrontHandler.postDelayed(bringToFrontRunnable, nextDelayMs);
                    }
                }
            };
        }
        bringToFrontHandler.removeCallbacks(bringToFrontRunnable);
        bringToFrontHandler.postDelayed(bringToFrontRunnable, relaunchIntervalMs(prefs));
    }

    private static long relaunchIntervalMs(SharedPreferences prefs) {
        long raw = prefs.getLong(KioskWatchdogScheduler.KEY_WATCHDOG_INTERVAL_MS, 15 * 60 * 1000L);
        return KioskWatchdogScheduler.clampIntervalMs(raw);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startForegroundWithNotification() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel();
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Kiosk Mode")
                .setContentText("App is running in kiosk mode")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setAutoCancel(false);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setCategory(NotificationCompat.CATEGORY_SERVICE);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setChannelId(CHANNEL_ID);
            }

            Notification notification = builder.build();

            // API 34+ requires a foreground service type matching the manifest (e.g. specialUse).
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
            } else {
                startForeground(1, notification);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to start foreground: " + e.getMessage(), e);
            throw e;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                NotificationManager manager = getSystemService(NotificationManager.class);
                if (manager == null) {
                    Log.e(TAG, "NotificationManager is null");
                    return;
                }

                if (manager.getNotificationChannel(CHANNEL_ID) != null) {
                    return;
                }

                NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Kiosk Keep Alive",
                    NotificationManager.IMPORTANCE_LOW
                );

                serviceChannel.setDescription("Keeps the app alive while in kiosk mode");
                serviceChannel.setSound(null, null);
                serviceChannel.setShowBadge(false);
                serviceChannel.enableLights(false);
                serviceChannel.enableVibration(false);

                manager.createNotificationChannel(serviceChannel);
            } catch (Exception e) {
                Log.e(TAG, "Failed to create notification channel: " + e.getMessage(), e);
                throw e;
            }
        }
    }
}
