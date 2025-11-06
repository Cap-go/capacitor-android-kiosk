# Integration Guide for @capgo/capacitor-android-kiosk

This guide provides detailed integration instructions for the Android Kiosk Mode plugin.

## Installation

```bash
npm install @capgo/capacitor-android-kiosk
npx cap sync
```

## Android Setup

### Step 1: Update AndroidManifest.xml

Add the launcher intent filter to your main activity in `android/app/src/main/AndroidManifest.xml`:

```xml
<activity
    android:name=".MainActivity"
    android:configChanges="orientation|keyboardHidden|keyboard|screenSize|locale|smallestScreenSize|screenLayout|uiMode"
    android:label="@string/title_activity_main"
    android:launchMode="singleTask"
    android:theme="@style/AppTheme.NoActionBarLaunch">

    <!-- Default launcher intent -->
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>

    <!-- Add this to make app available as device launcher/home app -->
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.HOME" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</activity>
```

### Step 2: Update MainActivity.java

Add hardware key blocking support in `android/app/src/main/java/.../MainActivity.java`:

```java
package com.example.app;

import android.os.Bundle;
import android.view.KeyEvent;
import com.getcapacitor.BridgeActivity;
import ee.forgr.plugin.android_kiosk.CapacitorAndroidKioskPlugin;

public class MainActivity extends BridgeActivity {

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Get the kiosk plugin instance
        CapacitorAndroidKioskPlugin kioskPlugin = null;
        try {
            kioskPlugin = (CapacitorAndroidKioskPlugin)
                this.getBridge().getPlugin("CapacitorAndroidKiosk").getInstance();
        } catch (Exception e) {
            // Plugin not found or error getting instance
        }

        // Check if key should be blocked
        if (kioskPlugin != null && kioskPlugin.shouldBlockKey(event.getKeyCode())) {
            return true; // Block the key event
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        // Get the kiosk plugin instance
        CapacitorAndroidKioskPlugin kioskPlugin = null;
        try {
            kioskPlugin = (CapacitorAndroidKioskPlugin)
                this.getBridge().getPlugin("CapacitorAndroidKiosk").getInstance();
        } catch (Exception e) {
            // Plugin not found
        }

        // Check if we're in kiosk mode
        if (kioskPlugin != null) {
            // Let the plugin handle back press (it will block if in kiosk mode)
            // Don't call super to prevent default back behavior
        } else {
            super.onBackPressed();
        }
    }
}
```

### Step 3: Configure Capacitor

No additional configuration needed in `capacitor.config.ts`.

## Usage Examples

### Example 1: Basic Kiosk Setup

```typescript
import { CapacitorAndroidKiosk } from '@capgo/capacitor-android-kiosk';

async function initializeKiosk() {
  // Check if app is set as launcher
  const { isLauncher } = await CapacitorAndroidKiosk.isSetAsLauncher();

  if (!isLauncher) {
    alert('Please set this app as your device launcher');
    await CapacitorAndroidKiosk.setAsLauncher();
    return;
  }

  // Enter kiosk mode
  await CapacitorAndroidKiosk.enterKioskMode();
  console.log('Kiosk mode active');
}

// Initialize on app start
initializeKiosk();
```

### Example 2: Kiosk with Volume Control

```typescript
async function setupKioskWithVolumeControl() {
  // Allow volume buttons but block everything else
  await CapacitorAndroidKiosk.setAllowedKeys({
    volumeUp: true,
    volumeDown: true,
    back: false,
    home: false,
    recent: false,
    power: false
  });

  await CapacitorAndroidKiosk.enterKioskMode();
}
```

### Example 3: Exit Kiosk with PIN

```typescript
async function exitKioskWithPin() {
  const pin = prompt('Enter PIN to exit kiosk mode:');

  if (pin === '1234') {
    await CapacitorAndroidKiosk.exitKioskMode();
    alert('Exited kiosk mode');
  } else {
    alert('Incorrect PIN');
  }
}
```

### Example 4: React Hook

```typescript
import { useEffect, useState } from 'react';
import { CapacitorAndroidKiosk } from '@capgo/capacitor-android-kiosk';

function useKioskMode() {
  const [isInKiosk, setIsInKiosk] = useState(false);
  const [isLauncher, setIsLauncher] = useState(false);

  useEffect(() => {
    checkStatus();
  }, []);

  async function checkStatus() {
    const { isInKioskMode } = await CapacitorAndroidKiosk.isInKioskMode();
    const { isLauncher } = await CapacitorAndroidKiosk.isSetAsLauncher();

    setIsInKiosk(isInKioskMode);
    setIsLauncher(isLauncher);
  }

  async function enterKiosk() {
    await CapacitorAndroidKiosk.enterKioskMode();
    await checkStatus();
  }

  async function exitKiosk() {
    await CapacitorAndroidKiosk.exitKioskMode();
    await checkStatus();
  }

  return {
    isInKiosk,
    isLauncher,
    enterKiosk,
    exitKiosk,
    checkStatus
  };
}

// Usage in component
function KioskApp() {
  const { isInKiosk, isLauncher, enterKiosk, exitKiosk } = useKioskMode();

  return (
    <div>
      <p>Kiosk Mode: {isInKiosk ? 'Active' : 'Inactive'}</p>
      <p>Is Launcher: {isLauncher ? 'Yes' : 'No'}</p>
      <button onClick={enterKiosk}>Enter Kiosk</button>
      <button onClick={exitKiosk}>Exit Kiosk</button>
    </div>
  );
}
```

### Example 5: Vue 3 Composition API

```typescript
import { ref, onMounted } from 'vue';
import { CapacitorAndroidKiosk } from '@capgo/capacitor-android-kiosk';

export function useKioskMode() {
  const isInKiosk = ref(false);
  const isLauncher = ref(false);

  async function checkStatus() {
    const kioskStatus = await CapacitorAndroidKiosk.isInKioskMode();
    const launcherStatus = await CapacitorAndroidKiosk.isSetAsLauncher();

    isInKiosk.value = kioskStatus.isInKioskMode;
    isLauncher.value = launcherStatus.isLauncher;
  }

  async function enterKiosk() {
    await CapacitorAndroidKiosk.enterKioskMode();
    await checkStatus();
  }

  async function exitKiosk() {
    await CapacitorAndroidKiosk.exitKioskMode();
    await checkStatus();
  }

  onMounted(() => {
    checkStatus();
  });

  return {
    isInKiosk,
    isLauncher,
    enterKiosk,
    exitKiosk,
    checkStatus
  };
}
```

## Testing

### Development Testing

1. Build and run your app on an Android device or emulator
2. Call `setAsLauncher()` to open home screen settings
3. Select your app as the default launcher
4. Test kiosk mode functionality

### Exiting Kiosk Mode for Testing

During development, you can exit kiosk mode in several ways:

1. **Programmatically**: Call `exitKioskMode()` in your app
2. **Via Settings**: Set a different app as launcher
3. **ADB**: Use `adb shell am start -a android.settings.HOME_SETTINGS`

### ADB Commands

```bash
# Check if app is launcher
adb shell cmd package get-home-app

# Open home settings
adb shell am start -a android.settings.HOME_SETTINGS

# Force stop app
adb shell am force-stop com.example.app

# Clear app as default launcher
adb shell pm clear-default-launcher
```

## Troubleshooting

### Hardware keys not blocked

**Issue**: Hardware buttons still work in kiosk mode

**Solution**: Ensure you've overridden `dispatchKeyEvent` in MainActivity.java as shown in Step 2

### Can't set as launcher

**Issue**: App doesn't appear in launcher selection

**Solution**: Verify the HOME intent filter is added to AndroidManifest.xml

### App crashes on entering kiosk mode

**Issue**: App crashes when calling `enterKioskMode()`

**Solution**: Check Android version compatibility and permissions

### Back button exits app

**Issue**: Back button exits the app even in kiosk mode

**Solution**: Override `onBackPressed()` in MainActivity.java

## Best Practices

1. **Always provide an exit mechanism**: Implement a PIN, gesture, or admin interface to exit kiosk mode
2. **Test on multiple devices**: Different Android versions may behave differently
3. **Handle configuration changes**: Ensure kiosk mode persists through orientation changes
4. **Battery management**: Consider implementing screen timeout or brightness control
5. **Error handling**: Always wrap plugin calls in try-catch blocks
6. **User communication**: Clearly communicate to users when they're in kiosk mode

## Security Considerations

1. **Exit mechanism**: Always implement a secure way to exit kiosk mode
2. **Admin protection**: Protect admin functions with authentication
3. **Updates**: Plan how to update the app while in kiosk mode
4. **Recovery**: Have a plan for recovering devices if the app crashes
5. **Permissions**: Only request necessary permissions

## Platform Differences

### Android
- Full kiosk mode support
- Requires being set as launcher for full functionality
- Hardware key blocking supported
- Works on Android 6.0 (API 23) through Android 15 (API 35)

### iOS
- Not supported - use Guided Access instead
- Plugin will return error messages directing to Guided Access
- Guided Access URL: https://support.apple.com/en-us/HT202612

### Web
- Not supported - stub implementation only
- All methods return warnings and default values
