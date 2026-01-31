# capacitor-android-kiosk
  <a href="https://capgo.app/"><img src='https://raw.githubusercontent.com/Cap-go/capgo/main/assets/capgo_banner.png' alt='Capgo - Instant updates for capacitor'/></a>

<div align="center">
  <h2><a href="https://capgo.app/?ref=plugin_android_kiosk"> ➡️ Get Instant updates for your App with Capgo</a></h2>
  <h2><a href="https://capgo.app/consulting/?ref=plugin_android_kiosk"> Missing a feature? We'll build the plugin for you 💪</a></h2>
</div>
Android Kiosk Mode plugin for Capacitor - Lock device into kiosk mode with launcher functionality
Compatible with Capacitor 8/7

## Documentation

The most complete doc is available here: https://capgo.app/docs/plugins/android-kiosk/

## Compatibility

| Plugin version | Capacitor compatibility | Maintained |
| -------------- | ----------------------- | ---------- |
| v8.\*.\*       | v8.\*.\*                | ✅          |
| v7.\*.\*       | v7.\*.\*                | On demand   |
| v6.\*.\*       | v6.\*.\*                | ❌          |
| v5.\*.\*       | v5.\*.\*                | ❌          |

> **Note:** The major version of this plugin follows the major version of Capacitor. Use the version that matches your Capacitor installation (e.g., plugin v8 for Capacitor 8). Only the latest major version is actively maintained.

## Install

```bash
npm install @capgo/capacitor-android-kiosk
npx cap sync
```

## Platform Support

This plugin is **Android-only**. For iOS kiosk mode functionality, please use the device's built-in [Guided Access](https://support.apple.com/en-us/HT202612) feature.

## Features

- **Kiosk Mode**: Hide system UI and enter immersive fullscreen mode
- **Launcher Integration**: Set your app as the device launcher/home app
- **Hardware Key Control**: Block or allow specific hardware buttons
- **Status Detection**: Check if kiosk mode is active or if app is set as launcher
- **Android 6.0+**: Supports Android API 23 through Android 15 (API 35)

## Usage

### Basic Kiosk Mode

```typescript
import { CapacitorAndroidKiosk } from '@capgo/capacitor-android-kiosk';

// Enter kiosk mode
await CapacitorAndroidKiosk.enterKioskMode();

// Exit kiosk mode
await CapacitorAndroidKiosk.exitKioskMode();

// Check if in kiosk mode
const { isInKioskMode } = await CapacitorAndroidKiosk.isInKioskMode();
console.log('Kiosk mode active:', isInKioskMode);
```

### Launcher Functionality

For full kiosk mode functionality, you need to set your app as the device launcher:

```typescript
// Open home screen settings for user to select your app as launcher
await CapacitorAndroidKiosk.setAsLauncher();

// Check if app is set as launcher
const { isLauncher } = await CapacitorAndroidKiosk.isSetAsLauncher();
console.log('App is launcher:', isLauncher);
```

### Hardware Key Control

```typescript
// Allow only volume keys
await CapacitorAndroidKiosk.setAllowedKeys({
  volumeUp: true,
  volumeDown: true,
  back: false,
  home: false,
  recent: false
});

// Block all keys (default)
await CapacitorAndroidKiosk.setAllowedKeys({});
```

### Complete Example

```typescript
async function setupKioskMode() {
  try {
    // Check if already set as launcher
    const { isLauncher } = await CapacitorAndroidKiosk.isSetAsLauncher();

    if (!isLauncher) {
      // Prompt user to set as launcher
      await CapacitorAndroidKiosk.setAsLauncher();
      alert('Please select this app as your Home app');
      return;
    }

    // Configure allowed keys
    await CapacitorAndroidKiosk.setAllowedKeys({
      volumeUp: true,
      volumeDown: true,
      back: false,
      home: false,
      recent: false,
      power: false
    });

    // Enter kiosk mode
    await CapacitorAndroidKiosk.enterKioskMode();
    console.log('Kiosk mode activated');

  } catch (error) {
    console.error('Failed to setup kiosk mode:', error);
  }
}
```

## API

<docgen-index>

* [`isInKioskMode()`](#isinkioskmode)
* [`isSetAsLauncher()`](#issetaslauncher)
* [`enterKioskMode()`](#enterkioskmode)
* [`exitKioskMode()`](#exitkioskmode)
* [`setAsLauncher()`](#setaslauncher)
* [`setAllowedKeys(...)`](#setallowedkeys)
* [`getPluginVersion()`](#getpluginversion)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

Capacitor Android Kiosk Plugin for controlling kiosk mode and launcher functionality.
This plugin is Android-only. For iOS kiosk mode, use the device's Guided Access feature.

### isInKioskMode()

```typescript
isInKioskMode() => Promise<{ isInKioskMode: boolean; }>
```

Checks if the app is currently running in kiosk mode.

**Returns:** <code>Promise&lt;{ isInKioskMode: boolean; }&gt;</code>

**Since:** 1.0.0

--------------------


### isSetAsLauncher()

```typescript
isSetAsLauncher() => Promise<{ isLauncher: boolean; }>
```

Checks if the app is set as the device launcher (home app).

**Returns:** <code>Promise&lt;{ isLauncher: boolean; }&gt;</code>

**Since:** 1.0.0

--------------------


### enterKioskMode()

```typescript
enterKioskMode() => Promise<void>
```

Enters kiosk mode, hiding system UI and blocking hardware buttons.
The app must be set as the device launcher for this to work effectively.

**Since:** 1.0.0

--------------------


### exitKioskMode()

```typescript
exitKioskMode() => Promise<void>
```

Exits kiosk mode, restoring normal system UI and hardware button functionality.

**Since:** 1.0.0

--------------------


### setAsLauncher()

```typescript
setAsLauncher() => Promise<void>
```

Opens the device's home screen settings to allow user to set this app as the launcher.
This is required for full kiosk mode functionality.

**Since:** 1.0.0

--------------------


### setAllowedKeys(...)

```typescript
setAllowedKeys(options: AllowedKeysOptions) => Promise<void>
```

Sets which hardware keys are allowed to function in kiosk mode.
By default, all hardware keys are blocked in kiosk mode.

| Param         | Type                                                              | Description                    |
| ------------- | ----------------------------------------------------------------- | ------------------------------ |
| **`options`** | <code><a href="#allowedkeysoptions">AllowedKeysOptions</a></code> | Configuration for allowed keys |

**Since:** 1.0.0

--------------------


### getPluginVersion()

```typescript
getPluginVersion() => Promise<{ version: string; }>
```

Get the native Capacitor plugin version.

**Returns:** <code>Promise&lt;{ version: string; }&gt;</code>

**Since:** 1.0.0

--------------------


### Interfaces


#### AllowedKeysOptions

Configuration options for allowed hardware keys in kiosk mode.

| Prop             | Type                 | Description                      | Default            |
| ---------------- | -------------------- | -------------------------------- | ------------------ |
| **`volumeUp`**   | <code>boolean</code> | Allow volume up button           | <code>false</code> |
| **`volumeDown`** | <code>boolean</code> | Allow volume down button         | <code>false</code> |
| **`back`**       | <code>boolean</code> | Allow back button                | <code>false</code> |
| **`home`**       | <code>boolean</code> | Allow home button                | <code>false</code> |
| **`recent`**     | <code>boolean</code> | Allow recent apps button         | <code>false</code> |
| **`power`**      | <code>boolean</code> | Allow power button               | <code>false</code> |
| **`camera`**     | <code>boolean</code> | Allow camera button (if present) | <code>false</code> |
| **`menu`**       | <code>boolean</code> | Allow menu button (if present)   | <code>false</code> |

</docgen-api>

## Android Configuration

### 1. MainActivity Setup

To enable full hardware key blocking, you need to override `dispatchKeyEvent` in your `MainActivity.java`:

```java
import android.view.KeyEvent;
import ee.forgr.plugin.android_kiosk.CapacitorAndroidKioskPlugin;

public class MainActivity extends BridgeActivity {
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Get the kiosk plugin
        CapacitorAndroidKioskPlugin kioskPlugin = (CapacitorAndroidKioskPlugin)
            this.getBridge().getPlugin("CapacitorAndroidKiosk").getInstance();

        if (kioskPlugin != null && kioskPlugin.shouldBlockKey(event.getKeyCode())) {
            return true; // Block the key
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        // Don't call super.onBackPressed() to disable back button
        // Or call the plugin's handleOnBackPressed
    }
}
```

### 2. AndroidManifest.xml

Add launcher intent filter to make your app selectable as a launcher:

```xml
<activity
    android:name=".MainActivity"
    ...>

    <!-- Existing intent filter -->
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>

    <!-- Add this to make app selectable as launcher -->
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.HOME" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</activity>
```

## Important Notes

1. **Launcher Requirement**: For full kiosk mode functionality (blocking home button, preventing task switching), your app must be set as the device launcher.

2. **Testing**: When testing, you can exit kiosk mode programmatically or by setting another app as the launcher.

3. **Android Versions**: The plugin uses modern Android APIs for Android 11+ and falls back to older methods for compatibility with Android 6.0+.

4. **Security**: This plugin is designed for legitimate kiosk applications. Ensure you provide users with a way to exit kiosk mode.

5. **Battery**: Kiosk mode keeps the screen on. Consider implementing your own screen timeout or brightness management.

## iOS Alternative

For iOS devices, use the built-in [Guided Access](https://support.apple.com/en-us/HT202612) feature:

1. Go to Settings > Accessibility > Guided Access
2. Turn on Guided Access
3. Set a passcode
4. Open your app
5. Triple-click the side button
6. Adjust settings and start Guided Access

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md)

## License

MIT

## Author

Martin Donadieu <martin@capgo.app>
