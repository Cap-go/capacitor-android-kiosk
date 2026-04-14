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
        // On Android 12+ (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S), background starts from
        // alarm/PI/watchdog behave more strictly; adding FLAG_ACTIVITY_CLEAR_TOP only from S onward
        // keeps relaunch intent behavior aligned with how we cold-start from non-Activity contexts
        // without changing older-task semantics on pre-S. The flag exists on all API levels; the guard
        // is intentional, not a capability check.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
    }
}
