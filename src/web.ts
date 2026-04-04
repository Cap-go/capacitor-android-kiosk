import { WebPlugin } from '@capacitor/core';

import type { AllowedKeysOptions, CapacitorAndroidKioskPlugin, EnterKioskModeOptions } from './definitions';

export class CapacitorAndroidKioskWeb extends WebPlugin implements CapacitorAndroidKioskPlugin {
  async isInKioskMode(): Promise<{ isInKioskMode: boolean }> {
    console.warn('Kiosk mode is not supported on web platform');
    return { isInKioskMode: false };
  }

  async isSetAsLauncher(): Promise<{ isLauncher: boolean }> {
    console.warn('Launcher functionality is not supported on web platform');
    return { isLauncher: false };
  }

  async enterKioskMode(options?: EnterKioskModeOptions): Promise<void> {
    void options;
    console.warn('Kiosk mode is not supported on web platform');
  }

  async exitKioskMode(): Promise<void> {
    console.warn('Kiosk mode is not supported on web platform');
  }

  async setAsLauncher(): Promise<void> {
    console.warn('Launcher functionality is not supported on web platform');
  }

  async setAllowedKeys(options: AllowedKeysOptions): Promise<void> {
    void options;
    console.warn('Hardware key control is not supported on web platform');
  }

  async getPluginVersion(): Promise<{ version: string }> {
    return { version: '1.0.0' };
  }
}
