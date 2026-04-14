package ee.forgr.plugin.android_kiosk;

import android.content.SharedPreferences;

/**
 * SharedPreferences keys for kiosk. {@code restore_after_reboot} and {@code kiosk_session_active} are
 * separate: the former drives boot relaunch only; the latter gates keep-alive bring-to-front while
 * a kiosk session is active.
 */
public final class KioskPrefs {

    public static final String PREFS_NAME = "CapacitorAndroidKiosk";
    public static final String KEY_RESTORE_AFTER_REBOOT = "restore_after_reboot";
    public static final String KEY_KIOSK_SESSION_ACTIVE = "kiosk_session_active";
    public static final String KEY_BOOT_RESTORE_PENDING = "boot_restore_pending";
    public static final String KEY_ALLOWED_KEYS = "allowed_keys";
    static final String LEGACY_KEY_KIOSK_ACTIVE = "kiosk_active";

    private KioskPrefs() {}

    /**
     * Whether to launch the app after {@code BOOT_COMPLETED}. Prefers {@link #KEY_RESTORE_AFTER_REBOOT};
     * falls back to legacy {@link #LEGACY_KEY_KIOSK_ACTIVE} when the new key is absent.
     */
    public static boolean shouldRestoreAfterBoot(SharedPreferences prefs) {
        if (prefs.contains(KEY_RESTORE_AFTER_REBOOT)) {
            return prefs.getBoolean(KEY_RESTORE_AFTER_REBOOT, false);
        }
        return prefs.getBoolean(LEGACY_KEY_KIOSK_ACTIVE, false);
    }
}
