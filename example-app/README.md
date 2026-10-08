# Example App for `@capgo/capacitor-android-kiosk`

This Vite + TypeScript project links to the local plugin (`file:..`) so you can exercise kiosk APIs on Android.

## Scripts

From the repository root:

```bash
bun run example:install
bun run example:build
```

From this folder:

```bash
bun install --frozen-lockfile
bun run start
bun run build
```

## What to try on Android

- Refresh status chips for kiosk and launcher state.
- Enter kiosk mode with optional reboot restore and relaunch watchdog settings.
- Open launcher settings, apply allowed hardware keys, and read the plugin version.
- Use the fixed **Exit kiosk mode** bar so you are never stuck in lock task mode.

iOS and web builds show a not-supported state; the plugin is Android-only.
