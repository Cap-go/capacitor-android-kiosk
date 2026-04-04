package ee.forgr.plugin.android_kiosk;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Fallback job scheduled after boot to launch the app when kiosk was active (restoreAfterReboot).
 */
public class BootLaunchJobService extends JobService {

    private static final String TAG = "BootLaunchJobService";

    @Override
    public boolean onStartJob(JobParameters params) {
        try {
            SharedPreferences prefs = getSharedPreferences(KioskPrefs.PREFS_NAME, Context.MODE_PRIVATE);
            if (!KioskPrefs.shouldRestoreAfterBoot(prefs)) {
                jobFinished(params, false);
                return false;
            }

            Intent launchIntent = KioskLaunchIntents.resolveLaunchIntent(this);
            if (launchIntent == null) {
                jobFinished(params, false);
                return false;
            }
            KioskLaunchIntents.addWatchdogLaunchFlags(launchIntent);
            startActivity(launchIntent);
        } catch (Exception e) {
            Log.e(TAG, "Job launch failed: " + e.getMessage(), e);
        }
        jobFinished(params, false);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
