package ee.forgr.plugin.android_kiosk;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

/**
 * When the device boots, if kiosk mode was active before reboot (and restoreAfterReboot was
 * enabled), launch the app. Uses delayed start, AlarmManager, and JobScheduler for resilience.
 */
public class BootCompletedReceiver extends BroadcastReceiver {

    private static final String TAG = "KioskBootReceiver";
    private static final int DELAY_MS = 3000;
    private static final int ALARM_DELAY_MS = 60000;
    private static final int BOOT_LAUNCH_JOB_ID_SALT = 0x4B006F74;
    private static final int ALARM_REQUEST_CODE = 2001;

    /**
     * Deterministic per-package id for {@link BootLaunchJobService}; same value must be used for
     * schedule and cancel.
     */
    static int bootLaunchJobId(Context context) {
        String pkg = context.getApplicationContext().getPackageName();
        return (BOOT_LAUNCH_JOB_ID_SALT ^ pkg.hashCode()) & Integer.MAX_VALUE;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) return;
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        try {
            SharedPreferences prefs = context.getSharedPreferences(KioskPrefs.PREFS_NAME, Context.MODE_PRIVATE);
            boolean kioskWasActive = KioskPrefs.shouldRestoreAfterBoot(prefs);
            if (!kioskWasActive) {
                return;
            }

            Intent launchIntent = KioskLaunchIntents.resolveLaunchIntent(context);
            if (launchIntent == null) {
                Log.w(TAG, "No launcher intent for package");
                KioskWatchdogScheduler.rescheduleIfEnabled(context);
                return;
            }

            KioskLaunchIntents.addWatchdogLaunchFlags(launchIntent);
            prefs.edit().putBoolean(KioskPrefs.KEY_BOOT_RESTORE_PENDING, true).commit();
            scheduleAlarmLaunch(context, launchIntent);
            scheduleJobLaunch(context);
            KioskWatchdogScheduler.rescheduleIfEnabled(context);
            final BroadcastReceiver.PendingResult pendingResult = goAsync();

            new Handler(Looper.getMainLooper()).postDelayed(
                () -> {
                    try {
                        Context appCtx = context.getApplicationContext();
                        SharedPreferences delayedPrefs = appCtx.getSharedPreferences(KioskPrefs.PREFS_NAME, Context.MODE_PRIVATE);
                        if (!KioskPrefs.shouldRestoreAfterBoot(delayedPrefs)) {
                            return;
                        }
                        if (!delayedPrefs.getBoolean(KioskPrefs.KEY_BOOT_RESTORE_PENDING, false)) {
                            return;
                        }
                        context.startActivity(launchIntent);
                    } catch (Exception e) {
                        Log.e(TAG, "Delayed launch failed: " + e.getMessage(), e);
                    } finally {
                        pendingResult.finish();
                    }
                },
                DELAY_MS
            );
        } catch (Exception e) {
            Log.e(TAG, "Error in boot receiver: " + e.getMessage(), e);
        }
    }

    private void scheduleAlarmLaunch(Context context, Intent launchIntent) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;

            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }
            PendingIntent pending = PendingIntent.getActivity(context.getApplicationContext(), ALARM_REQUEST_CODE, launchIntent, flags);

            long triggerAt = SystemClock.elapsedRealtime() + ALARM_DELAY_MS;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pending);
            } else {
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pending);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to schedule alarm: " + e.getMessage(), e);
        }
    }

    /**
     * Cancels the boot-time activity alarm from {@link #scheduleAlarmLaunch}. Uses the same
     * request code, resolved launcher intent (with watchdog flags), {@link PendingIntent} flags,
     * and {@link PendingIntent#getActivity} as scheduling — not the watchdog broadcast PI.
     */
    static void cancelBootLaunchActivityAlarm(Context context) {
        if (context == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;
        try {
            Intent launchIntent = KioskLaunchIntents.resolveLaunchIntent(context);
            if (launchIntent == null) return;
            KioskLaunchIntents.addWatchdogLaunchFlags(launchIntent);

            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }
            PendingIntent pending = PendingIntent.getActivity(context.getApplicationContext(), ALARM_REQUEST_CODE, launchIntent, flags);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.cancel(pending);
            }
            pending.cancel();
        } catch (Exception e) {
            Log.e(TAG, "Failed to cancel boot launch alarm: " + e.getMessage(), e);
        }
    }

    private void scheduleJobLaunch(Context context) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;

            android.app.job.JobScheduler scheduler = (android.app.job.JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if (scheduler == null) return;

            int jobId = bootLaunchJobId(context);
            scheduler.cancel(jobId);

            ComponentName jobService = new ComponentName(context, BootLaunchJobService.class);
            android.app.job.JobInfo.Builder builder = new android.app.job.JobInfo.Builder(jobId, jobService)
                .setMinimumLatency(ALARM_DELAY_MS)
                .setOverrideDeadline(ALARM_DELAY_MS);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setRequiresDeviceIdle(false);
            }

            scheduler.schedule(builder.build());
        } catch (Exception e) {
            Log.e(TAG, "Failed to schedule job: " + e.getMessage(), e);
        }
    }

    /**
     * One-shot boot restore: clear pending flag and cancel alarm/job fallbacks so a successful
     * launch is not repeated. Safe to call when {@link KioskPrefs#KEY_BOOT_RESTORE_PENDING} is
     * already false.
     */
    static void completeBootRestore(Context context) {
        if (context == null) return;
        Context app = context.getApplicationContext();
        SharedPreferences prefs = app.getSharedPreferences(KioskPrefs.PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KioskPrefs.KEY_BOOT_RESTORE_PENDING, false).commit();
        cancelBootLaunchActivityAlarm(app);
        cancelBootLaunchJob(app);
    }

    /**
     * When the app reaches foreground after boot (e.g. alarm {@link PendingIntent} started the
     * activity), finish boot coordination if fallbacks were still armed.
     */
    static void completeBootRestoreIfPending(Context context) {
        if (context == null) return;
        Context app = context.getApplicationContext();
        SharedPreferences prefs = app.getSharedPreferences(KioskPrefs.PREFS_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(KioskPrefs.KEY_BOOT_RESTORE_PENDING, false)) {
            completeBootRestore(app);
        }
    }

    static void cancelBootLaunchJob(Context context) {
        if (context == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        try {
            android.app.job.JobScheduler scheduler = (android.app.job.JobScheduler) context
                .getApplicationContext()
                .getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if (scheduler != null) {
                scheduler.cancel(bootLaunchJobId(context));
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to cancel boot launch job: " + e.getMessage(), e);
        }
    }
}
