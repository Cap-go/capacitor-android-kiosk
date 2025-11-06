import { registerPlugin } from '@capacitor/core';

import type { CapacitorAndroidKioskPlugin } from './definitions';

const CapacitorAndroidKiosk = registerPlugin<CapacitorAndroidKioskPlugin>('CapacitorAndroidKiosk', {
  web: () => import('./web').then((m) => new m.CapacitorAndroidKioskWeb()),
});

export * from './definitions';
export { CapacitorAndroidKiosk };
