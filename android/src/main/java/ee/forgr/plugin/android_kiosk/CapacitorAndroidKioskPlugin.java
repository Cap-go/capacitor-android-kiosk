package ee.forgr.plugin.android_kiosk;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.util.HashSet;
import java.util.Set;

@CapacitorPlugin(name = "CapacitorAndroidKiosk")
public class CapacitorAndroidKioskPlugin extends Plugin {

    private final String pluginVersion = "8.1.3";
    private boolean isInKioskMode = false;
    private final Set<Integer> allowedKeys = new HashSet<>();

    @Override
    public void load() {
        // Initialize with no allowed keys by default
        allowedKeys.clear();
    }

    @PluginMethod
    public void isInKioskMode(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("isInKioskMode", isInKioskMode);
        call.resolve(ret);
    }

    @PluginMethod
    public void isSetAsLauncher(PluginCall call) {
        JSObject ret = new JSObject();
        boolean isLauncher = checkIfLauncher();
        ret.put("isLauncher", isLauncher);
        call.resolve(ret);
    }

    @PluginMethod
    public void enterKioskMode(PluginCall call) {
        try {
            Activity activity = getActivity();
            if (activity == null) {
                call.reject("Activity not available");
                return;
            }

            activity.runOnUiThread(() -> {
                try {
                    View decorView = activity.getWindow().getDecorView();

                    // Hide system UI for different Android versions
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        // Android 11+ (API 30+)
                        activity.getWindow().setDecorFitsSystemWindows(false);
                        android.view.WindowInsetsController controller = decorView.getWindowInsetsController();
                        if (controller != null) {
                            controller.hide(android.view.WindowInsets.Type.statusBars() | android.view.WindowInsets.Type.navigationBars());
                            controller.setSystemBarsBehavior(android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                        }
                    } else {
                        // Android 10 and below
                        int uiOptions =
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                        decorView.setSystemUiVisibility(uiOptions);
                    }

                    // Keep screen on
                    activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                    // Prevent status bar from being pulled down
                    activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

                    isInKioskMode = true;
                    call.resolve();
                } catch (Exception e) {
                    call.reject("Failed to enter kiosk mode", e);
                }
            });
        } catch (Exception e) {
            call.reject("Failed to enter kiosk mode", e);
        }
    }

    @PluginMethod
    public void exitKioskMode(PluginCall call) {
        try {
            Activity activity = getActivity();
            if (activity == null) {
                call.reject("Activity not available");
                return;
            }

            activity.runOnUiThread(() -> {
                try {
                    View decorView = activity.getWindow().getDecorView();

                    // Restore system UI for different Android versions
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        // Android 11+ (API 30+)
                        activity.getWindow().setDecorFitsSystemWindows(true);
                        android.view.WindowInsetsController controller = decorView.getWindowInsetsController();
                        if (controller != null) {
                            controller.show(android.view.WindowInsets.Type.statusBars() | android.view.WindowInsets.Type.navigationBars());
                        }
                    } else {
                        // Android 10 and below
                        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    }

                    // Clear screen on flag
                    activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                    // Clear fullscreen flag
                    activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

                    isInKioskMode = false;
                    call.resolve();
                } catch (Exception e) {
                    call.reject("Failed to exit kiosk mode", e);
                }
            });
        } catch (Exception e) {
            call.reject("Failed to exit kiosk mode", e);
        }
    }

    @PluginMethod
    public void setAsLauncher(PluginCall call) {
        try {
            Context context = getContext();
            if (context == null) {
                call.reject("Context not available");
                return;
            }

            // Enable launcher intent filter
            ComponentName componentName = new ComponentName(context, getLauncherActivity());
            PackageManager packageManager = context.getPackageManager();
            packageManager.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            );

            // Open home screen settings
            Intent intent = new Intent(android.provider.Settings.ACTION_HOME_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to set as launcher", e);
        }
    }

    @PluginMethod
    public void setAllowedKeys(PluginCall call) {
        allowedKeys.clear();

        // Parse allowed keys from options
        if (call.getBoolean("volumeUp", false)) {
            allowedKeys.add(KeyEvent.KEYCODE_VOLUME_UP);
        }
        if (call.getBoolean("volumeDown", false)) {
            allowedKeys.add(KeyEvent.KEYCODE_VOLUME_DOWN);
        }
        if (call.getBoolean("back", false)) {
            allowedKeys.add(KeyEvent.KEYCODE_BACK);
        }
        if (call.getBoolean("home", false)) {
            allowedKeys.add(KeyEvent.KEYCODE_HOME);
        }
        if (call.getBoolean("recent", false)) {
            allowedKeys.add(KeyEvent.KEYCODE_APP_SWITCH);
        }
        if (call.getBoolean("power", false)) {
            allowedKeys.add(KeyEvent.KEYCODE_POWER);
        }
        if (call.getBoolean("camera", false)) {
            allowedKeys.add(KeyEvent.KEYCODE_CAMERA);
        }
        if (call.getBoolean("menu", false)) {
            allowedKeys.add(KeyEvent.KEYCODE_MENU);
        }

        call.resolve();
    }

    @PluginMethod
    public void getPluginVersion(PluginCall call) {
        try {
            JSObject ret = new JSObject();
            ret.put("version", pluginVersion);
            call.resolve(ret);
        } catch (Exception e) {
            call.reject("Could not get plugin version", e);
        }
    }

    /**
     * Handle key events to block hardware buttons when in kiosk mode
     * This method should be called from the main activity's dispatchKeyEvent
     */
    @Override
    protected void handleOnPause() {
        super.handleOnPause();
        if (isInKioskMode) {
            Activity activity = getActivity();
            if (activity != null) {
                // Bring app back to foreground if in kiosk mode
                ActivityManager activityManager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
                if (activityManager != null) {
                    activityManager.moveTaskToFront(activity.getTaskId(), 0);
                }
            }
        }
    }

    /**
     * Check if this app is set as the default launcher
     */
    private boolean checkIfLauncher() {
        try {
            Context context = getContext();
            if (context == null) {
                return false;
            }

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);

            PackageManager packageManager = context.getPackageManager();
            ComponentName componentName = intent.resolveActivity(packageManager);

            if (componentName == null) {
                return false;
            }

            return componentName.getPackageName().equals(context.getPackageName());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the launcher activity class
     * This should be the main activity of the Capacitor app
     */
    private Class<?> getLauncherActivity() {
        Activity activity = getActivity();
        if (activity != null) {
            return activity.getClass();
        }
        return null;
    }

    /**
     * Check if a key event should be blocked
     * Call this from your MainActivity's dispatchKeyEvent method
     */
    public boolean shouldBlockKey(int keyCode) {
        if (!isInKioskMode) {
            return false;
        }
        return !allowedKeys.contains(keyCode);
    }
}
