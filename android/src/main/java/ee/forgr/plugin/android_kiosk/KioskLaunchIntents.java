package ee.forgr.plugin.android_kiosk;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import java.util.List;

/** Resolves the same launcher intent the watchdog / keep-alive use. */
public final class KioskLaunchIntents {

    private KioskLaunchIntents() {}

    /**
     * @return a new intent suitable for {@link android.app.PendingIntent#getActivity} or {@link
     *     Context#startActivity}, or null
     */
    public static Intent resolveLaunchIntent(Context context) {
        if (context == null) {
            return null;
        }
        PackageManager pm = context.getPackageManager();
        String packageName = context.getPackageName();

        Intent intent = pm.getLaunchIntentForPackage(packageName);
        if (intent != null) {
            return new Intent(intent);
        }

        Intent query = new Intent(Intent.ACTION_MAIN);
        query.addCategory(Intent.CATEGORY_LAUNCHER);
        query.setPackage(packageName);
        List<android.content.pm.ResolveInfo> list = pm.queryIntentActivities(query, 0);
        if (list == null || list.isEmpty()) {
            return null;
        }

        android.content.pm.ResolveInfo info = list.get(0);
        android.content.ComponentName component = new android.content.ComponentName(info.activityInfo.packageName, info.activityInfo.name);
        return new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setComponent(component);
    }

    /** Flags required when starting the launcher activity from a non-Activity context or from an alarm PI. */
    public static void addWatchdogLaunchFlags(Intent intent) {
        if (intent == null) {
            return;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        // API 31+: include CLEAR_TOP so a cold start from background (alarm/job/watchdog) targets the
        // existing task reliably; gated to match platform relaunch behavior, not because the flag is missing pre-S.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
    }
}
