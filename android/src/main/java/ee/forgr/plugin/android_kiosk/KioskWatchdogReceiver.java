package ee.forgr.plugin.android_kiosk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Watchdog tick: best-effort {@link Context#startActivity} (skipped while the display is off via
 * {@link KioskRelaunchHelper}). When the watchdog is still enabled in prefs, re-arms the next alarm
 * via {@link KioskWatchdogScheduler#scheduleNext}; if the watchdog is disabled in prefs,
 * {@link #onReceive} returns early and does not schedule. OEMs may block the activity; the schedule
 * still advances when enabled.
 */
public class KioskWatchdogReceiver extends BroadcastReceiver {

    private static final String TAG = "KioskWatchdog";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null) {
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences(KioskPrefs.PREFS_NAME, Context.MODE_PRIVATE);
        if (!prefs.getBoolean(KioskWatchdogScheduler.KEY_WATCHDOG_ENABLED, false)) {
            Log.w(TAG, "watchdog disabled in prefs; not re-arming");
            return;
        }

        Intent launchIntent = KioskLaunchIntents.resolveLaunchIntent(context);
        if (launchIntent != null) {
            try {
                KioskRelaunchHelper.startLaunchActivity(context, launchIntent);
            } catch (Exception e) {
                Log.e(TAG, "watchdog startActivity failed: " + e.getMessage(), e);
            }
        } else {
            Log.w(TAG, "no launch intent");
        }

        KioskWatchdogScheduler.scheduleNext(context);
    }
}
