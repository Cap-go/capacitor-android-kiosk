package ee.forgr.plugin.android_kiosk;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

/** Starts the launcher activity from a {@link android.app.Service} or other non-Activity context. */
public final class KioskRelaunchHelper {

    private KioskRelaunchHelper() {}

    /**
     * True when the device is in an interactive state (user-present; screen typically on). When
     * false, {@link #startLaunchActivity} does nothing so periodic relaunch does not cycle the app
     * in the background while the display is off.
     */
    public static boolean isDisplayInteractive(Context context) {
        if (context == null) {
            return false;
        }
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (pm == null) {
            return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return pm.isInteractive();
        }
        return legacyScreenOn(pm);
    }

    @SuppressWarnings("deprecation")
    private static boolean legacyScreenOn(PowerManager pm) {
        return pm.isScreenOn();
    }

    public static void startLaunchActivity(Context context, Intent launchIntent) {
        if (context == null || launchIntent == null) {
            return;
        }
        if (!isDisplayInteractive(context)) {
            return;
        }
        KioskLaunchIntents.addWatchdogLaunchFlags(launchIntent);
        context.startActivity(launchIntent);
    }
}
