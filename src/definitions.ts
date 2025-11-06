/**
 * Capacitor Android Kiosk Plugin for controlling kiosk mode and launcher functionality.
 * This plugin is Android-only. For iOS kiosk mode, use the device's Guided Access feature.
 *
 * @since 1.0.0
 */
export interface CapacitorAndroidKioskPlugin {
  /**
   * Checks if the app is currently running in kiosk mode.
   *
   * @returns Promise that resolves with kiosk mode status
   * @since 1.0.0
   * @example
   * ```typescript
   * const { isInKioskMode } = await CapacitorAndroidKiosk.isInKioskMode();
   * console.log('Kiosk mode active:', isInKioskMode);
   * ```
   */
  isInKioskMode(): Promise<{ isInKioskMode: boolean }>;

  /**
   * Checks if the app is set as the device launcher (home app).
   *
   * @returns Promise that resolves with launcher status
   * @since 1.0.0
   * @example
   * ```typescript
   * const { isLauncher } = await CapacitorAndroidKiosk.isSetAsLauncher();
   * console.log('Is launcher:', isLauncher);
   * ```
   */
  isSetAsLauncher(): Promise<{ isLauncher: boolean }>;

  /**
   * Enters kiosk mode, hiding system UI and blocking hardware buttons.
   * The app must be set as the device launcher for this to work effectively.
   *
   * @returns Promise that resolves when kiosk mode is activated
   * @throws Error if entering kiosk mode fails
   * @since 1.0.0
   * @example
   * ```typescript
   * await CapacitorAndroidKiosk.enterKioskMode();
   * console.log('Entered kiosk mode');
   * ```
   */
  enterKioskMode(): Promise<void>;

  /**
   * Exits kiosk mode, restoring normal system UI and hardware button functionality.
   *
   * @returns Promise that resolves when kiosk mode is exited
   * @throws Error if exiting kiosk mode fails
   * @since 1.0.0
   * @example
   * ```typescript
   * await CapacitorAndroidKiosk.exitKioskMode();
   * console.log('Exited kiosk mode');
   * ```
   */
  exitKioskMode(): Promise<void>;

  /**
   * Opens the device's home screen settings to allow user to set this app as the launcher.
   * This is required for full kiosk mode functionality.
   *
   * @returns Promise that resolves when settings are opened
   * @since 1.0.0
   * @example
   * ```typescript
   * await CapacitorAndroidKiosk.setAsLauncher();
   * // User will be prompted to select this app as the home app
   * ```
   */
  setAsLauncher(): Promise<void>;

  /**
   * Sets which hardware keys are allowed to function in kiosk mode.
   * By default, all hardware keys are blocked in kiosk mode.
   *
   * @param options Configuration for allowed keys
   * @returns Promise that resolves when allowed keys are configured
   * @since 1.0.0
   * @example
   * ```typescript
   * // Allow volume keys only
   * await CapacitorAndroidKiosk.setAllowedKeys({
   *   volumeUp: true,
   *   volumeDown: true,
   *   back: false,
   *   home: false,
   *   recent: false
   * });
   * ```
   */
  setAllowedKeys(options: AllowedKeysOptions): Promise<void>;

  /**
   * Get the native Capacitor plugin version.
   *
   * @returns Promise that resolves with the plugin version
   * @throws Error if getting the version fails
   * @since 1.0.0
   * @example
   * ```typescript
   * const { version } = await CapacitorAndroidKiosk.getPluginVersion();
   * console.log('Plugin version:', version);
   * ```
   */
  getPluginVersion(): Promise<{ version: string }>;
}

/**
 * Configuration options for allowed hardware keys in kiosk mode.
 *
 * @since 1.0.0
 */
export interface AllowedKeysOptions {
  /**
   * Allow volume up button
   * @default false
   */
  volumeUp?: boolean;

  /**
   * Allow volume down button
   * @default false
   */
  volumeDown?: boolean;

  /**
   * Allow back button
   * @default false
   */
  back?: boolean;

  /**
   * Allow home button
   * @default false
   */
  home?: boolean;

  /**
   * Allow recent apps button
   * @default false
   */
  recent?: boolean;

  /**
   * Allow power button
   * @default false
   */
  power?: boolean;

  /**
   * Allow camera button (if present)
   * @default false
   */
  camera?: boolean;

  /**
   * Allow menu button (if present)
   * @default false
   */
  menu?: boolean;
}
