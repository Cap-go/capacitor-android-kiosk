import { CapacitorUpdater } from '@capgo/capacitor-updater';
import { Capacitor } from '@capacitor/core';
import { CapacitorAndroidKiosk } from '@capgo/capacitor-android-kiosk';
import type { AllowedKeysOptions, EnterKioskModeOptions } from '@capgo/capacitor-android-kiosk';
import './style.css';

const platformChip = document.getElementById('platform-chip') as HTMLSpanElement;
const kioskChip = document.getElementById('kiosk-chip') as HTMLSpanElement;
const launcherChip = document.getElementById('launcher-chip') as HTMLSpanElement;
const unsupportedPanel = document.getElementById('unsupported-panel') as HTMLElement;
const unsupportedMessage = document.getElementById('unsupported-message') as HTMLParagraphElement;
const androidPanel = document.getElementById('android-panel') as HTMLElement;
const keysPanel = document.getElementById('keys-panel') as HTMLElement;
const resultLog = document.getElementById('result-log') as HTMLPreElement;

const optRestoreReboot = document.getElementById('opt-restore-reboot') as HTMLInputElement;
const optRelaunch = document.getElementById('opt-relaunch') as HTMLInputElement;
const optRelaunchInterval = document.getElementById('opt-relaunch-interval') as HTMLInputElement;

const isAndroid = Capacitor.getPlatform() === 'android';

const appendLog = (title: string, payload: unknown): void => {
  const stamp = new Date().toISOString().slice(11, 19);
  const body = typeof payload === 'string' ? payload : JSON.stringify(payload, null, 2);
  const entry = `[${stamp}] ${title}\n${body}`;
  resultLog.textContent = resultLog.textContent === 'Ready.' ? entry : `${resultLog.textContent}\n\n${entry}`;
  resultLog.scrollTop = resultLog.scrollHeight;
};

const setChip = (chip: HTMLSpanElement, label: string, state: 'on' | 'off' | 'warn' | 'unknown'): void => {
  chip.textContent = label;
  chip.dataset.state = state;
};

const setAndroidUiEnabled = (enabled: boolean): void => {
  const controls = [
    ...androidPanel.querySelectorAll('button, input'),
    ...keysPanel.querySelectorAll('button, input'),
  ] as Array<HTMLButtonElement | HTMLInputElement>;
  controls.forEach((el) => {
    el.disabled = !enabled;
  });
};

const readEnterOptions = (): EnterKioskModeOptions => {
  const minutes = Number.parseInt(optRelaunchInterval.value, 10);
  const relaunchIntervalMinutes = Number.isFinite(minutes) ? Math.min(60, Math.max(5, minutes)) : 15;
  optRelaunchInterval.value = String(relaunchIntervalMinutes);
  return {
    restoreAfterReboot: optRestoreReboot.checked,
    relaunch: optRelaunch.checked,
    relaunchIntervalMinutes,
  };
};

const readAllowedKeys = (): AllowedKeysOptions => {
  const options: AllowedKeysOptions = {};
  keysPanel.querySelectorAll<HTMLInputElement>('input[data-key]').forEach((input) => {
    const key = input.dataset.key as keyof AllowedKeysOptions;
    options[key] = input.checked;
  });
  return options;
};

const refreshStatus = async (): Promise<void> => {
  try {
    const [{ isInKioskMode }, { isLauncher }] = await Promise.all([
      CapacitorAndroidKiosk.isInKioskMode(),
      CapacitorAndroidKiosk.isSetAsLauncher(),
    ]);
    setChip(kioskChip, `Kiosk: ${isInKioskMode ? 'active' : 'off'}`, isInKioskMode ? 'on' : 'off');
    setChip(launcherChip, `Launcher: ${isLauncher ? 'yes' : 'no'}`, isLauncher ? 'on' : 'off');
    appendLog('refreshStatus', { isInKioskMode, isLauncher });
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    setChip(kioskChip, 'Kiosk: error', 'warn');
    setChip(launcherChip, 'Launcher: error', 'warn');
    appendLog('refreshStatus error', message);
  }
};

const initPlatformUi = (): void => {
  const platform = Capacitor.getPlatform();
  platformChip.textContent = `Platform: ${platform}`;
  if (!isAndroid) {
    unsupportedPanel.classList.remove('hidden');
    unsupportedMessage.textContent =
      platform === 'ios'
        ? 'This plugin is Android-only. On iOS, use Guided Access for kiosk-style locking. Web preview cannot enter lock task mode.'
        : 'This plugin is Android-only. Use the native Android build to test kiosk APIs.';
    setChip(kioskChip, 'Kiosk: n/a', 'warn');
    setChip(launcherChip, 'Launcher: n/a', 'warn');
    setAndroidUiEnabled(false);
    return;
  }
  unsupportedPanel.classList.add('hidden');
  setAndroidUiEnabled(true);
};

document.getElementById('btn-enter-kiosk')?.addEventListener('click', async () => {
  const options = readEnterOptions();
  try {
    await CapacitorAndroidKiosk.enterKioskMode(options);
    appendLog('enterKioskMode', { ok: true, options });
    await refreshStatus();
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    appendLog('enterKioskMode error', message);
  }
});

document.getElementById('btn-exit-kiosk')?.addEventListener('click', async () => {
  try {
    await CapacitorAndroidKiosk.exitKioskMode();
    appendLog('exitKioskMode', { ok: true });
    await refreshStatus();
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    appendLog('exitKioskMode error', message);
  }
});

document.getElementById('btn-set-launcher')?.addEventListener('click', async () => {
  try {
    await CapacitorAndroidKiosk.setAsLauncher();
    appendLog('setAsLauncher', { ok: true, note: 'System launcher picker opened when supported.' });
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    appendLog('setAsLauncher error', message);
  }
});

document.getElementById('btn-apply-keys')?.addEventListener('click', async () => {
  const options = readAllowedKeys();
  try {
    await CapacitorAndroidKiosk.setAllowedKeys(options);
    appendLog('setAllowedKeys', options);
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    appendLog('setAllowedKeys error', message);
  }
});

document.getElementById('btn-refresh')?.addEventListener('click', () => {
  void refreshStatus();
});

document.getElementById('btn-version')?.addEventListener('click', async () => {
  try {
    const result = await CapacitorAndroidKiosk.getPluginVersion();
    appendLog('getPluginVersion', result);
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    appendLog('getPluginVersion error', message);
  }
});

document.getElementById('btn-clear-log')?.addEventListener('click', () => {
  resultLog.textContent = 'Ready.';
});

initPlatformUi();
void refreshStatus();

if (Capacitor.isNativePlatform()) {
  CapacitorUpdater.notifyAppReady().catch((error: unknown) => {
    console.error('Capgo notifyAppReady failed', error);
  });
}
