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
    private static final int JOB_ID = 1001;
    private static final int ALARM_REQUEST_CODE = 2001;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) return;
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        try {
            SharedPreferences prefs = context.getSharedPreferences(KioskPrefs.PREFS_NAME, Context.MODE_PRIVATE);
            boolean kioskWasActive = KioskPrefs.shouldRestoreAfterBoot(prefs);
            if (!kioskWasActive) {
                KioskWatchdogScheduler.rescheduleIfEnabled(context);
                return;
            }

            Intent launchIntent = KioskLaunchIntents.resolveLaunchIntent(context);
            if (launchIntent == null) {
                Log.w(TAG, "No launcher intent for package");
                KioskWatchdogScheduler.rescheduleIfEnabled(context);
                return;
            }

            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }

            // 1. Delayed launch (gives system time to settle)
            new Handler(Looper.getMainLooper()).postDelayed(
                () -> {
                    try {
                        context.startActivity(launchIntent);
                    } catch (Exception e) {
                        Log.e(TAG, "Delayed launch failed: " + e.getMessage(), e);
                    }
                },
                DELAY_MS
            );

            // 2. AlarmManager second chance (60s later, works in Doze)
            scheduleAlarmLaunch(context, launchIntent);

            // 3. JobScheduler fallback (runs within 60s if boot broadcast was delayed)
            scheduleJobLaunch(context);

            // 4. Restore watchdog alarm (cleared at reboot) when kiosk restore is enabled
            KioskWatchdogScheduler.rescheduleIfEnabled(context);
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
            PendingIntent pending = PendingIntent.getActivity(context, ALARM_REQUEST_CODE, launchIntent, flags);

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

    private void scheduleJobLaunch(Context context) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;

            android.app.job.JobScheduler scheduler = (android.app.job.JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if (scheduler == null) return;

            ComponentName jobService = new ComponentName(context, BootLaunchJobService.class);
            android.app.job.JobInfo.Builder builder = new android.app.job.JobInfo.Builder(JOB_ID, jobService).setOverrideDeadline(
                ALARM_DELAY_MS
            );

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setRequiresDeviceIdle(false);
            }

            scheduler.schedule(builder.build());
        } catch (Exception e) {
            Log.e(TAG, "Failed to schedule job: " + e.getMessage(), e);
        }
    }
}
