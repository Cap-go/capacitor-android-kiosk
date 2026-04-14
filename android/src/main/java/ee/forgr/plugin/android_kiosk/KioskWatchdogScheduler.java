package ee.forgr.plugin.android_kiosk;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

/**
 * Sliding {@link PendingIntent#getBroadcast} alarm. Each tick tries to start the launcher activity
 * and then re-arms the next tick. Many OEMs (Xiaomi HyperOS, MIUI, etc.) block background activity
 * starts; the alarm still repeats. Use default launcher + battery/autostart settings or device-owner
 * kiosk where you need a hard guarantee.
 */
public final class KioskWatchdogScheduler {

    private static final String TAG = "KioskWatchdog";
    static final String KEY_WATCHDOG_ENABLED = "watchdog_relaunch_enabled";
    static final String KEY_WATCHDOG_INTERVAL_MS = "watchdog_interval_ms";

    private static final long DEFAULT_INTERVAL_MS = 15 * 60 * 1000L;
    private static final long MIN_INTERVAL_MS = 5 * 60 * 1000L;
    private static final long MAX_INTERVAL_MS = 60 * 60 * 1000L;

    private static final int REQUEST_BROADCAST = 3001;

    private KioskWatchdogScheduler() {}

    public static long clampIntervalMs(long intervalMs) {
        if (intervalMs < MIN_INTERVAL_MS) return MIN_INTERVAL_MS;
        if (intervalMs > MAX_INTERVAL_MS) return MAX_INTERVAL_MS;
        return intervalMs;
    }

    public static void enable(Context context, long intervalMs) {
        Context app = context.getApplicationContext();
        long ms = clampIntervalMs(intervalMs);
        SharedPreferences prefs = app.getSharedPreferences(KioskPrefs.PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_WATCHDOG_ENABLED, true).putLong(KEY_WATCHDOG_INTERVAL_MS, ms).apply();
        Log.i(TAG, "relaunch watchdog enabled: intervalMs=" + ms + " (~" + (ms / 60_000L) + " min)");
        scheduleNext(app);
    }

    public static void disable(Context context) {
        Context app = context.getApplicationContext();
        Log.i(TAG, "relaunch watchdog disabled: canceling alarm");
        app.getSharedPreferences(KioskPrefs.PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_WATCHDOG_ENABLED, false).apply();
        cancelPendingAlarm(app);
    }

    public static boolean isEnabled(Context context) {
        return context
            .getApplicationContext()
            .getSharedPreferences(KioskPrefs.PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_WATCHDOG_ENABLED, false);
    }

    public static void rescheduleIfEnabled(Context context) {
        Context app = context.getApplicationContext();
        if (!isEnabled(app)) return;
        scheduleNext(app);
    }

    static void scheduleNext(Context context) {
        Context app = context.getApplicationContext();
        SharedPreferences prefs = app.getSharedPreferences(KioskPrefs.PREFS_NAME, Context.MODE_PRIVATE);
        if (!prefs.getBoolean(KEY_WATCHDOG_ENABLED, false)) {
            return;
        }

        long intervalMs = prefs.getLong(KEY_WATCHDOG_INTERVAL_MS, DEFAULT_INTERVAL_MS);
        intervalMs = clampIntervalMs(intervalMs);

        cancelPendingAlarm(app);

        Intent intent = new Intent(app, KioskWatchdogReceiver.class);
        int piFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            piFlags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pending = PendingIntent.getBroadcast(app, REQUEST_BROADCAST, intent, piFlags);

        AlarmManager am = (AlarmManager) app.getSystemService(Context.ALARM_SERVICE);
        if (am == null) {
            Log.w(TAG, "AlarmManager unavailable");
            return;
        }

        long triggerAt = SystemClock.elapsedRealtime() + intervalMs;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pending);
            } else {
                am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pending);
            }
        } catch (SecurityException e) {
            Log.w(TAG, "Cannot schedule watchdog alarm (exact alarm permission?): " + e.getMessage());
        }
    }

    /** Cancels only the sliding watchdog broadcast alarm (see {@link BootCompletedReceiver#cancelBootLaunchActivityAlarm}). */
    static void cancelPendingAlarm(Context context) {
        Context app = context.getApplicationContext();
        int piFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            piFlags |= PendingIntent.FLAG_IMMUTABLE;
        }

        Intent intent = new Intent(app, KioskWatchdogReceiver.class);
        PendingIntent pending = PendingIntent.getBroadcast(app, REQUEST_BROADCAST, intent, piFlags);
        AlarmManager am = (AlarmManager) app.getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            am.cancel(pending);
        }
        pending.cancel();
    }
}
