export interface Source {
  id: string;
  name: string;
  checked: boolean;
}

export interface Product {
  id: string;
  name: string;
  type: string;
  features: string;
}

export interface PushSettings {
  frequency: string;
  channels: string[]; // e.g., ['wechat', 'site', 'sms', 'email']
}

export interface SettingsData {
  policySources: Source[];
  competitorSources: Source[];
  myProducts: Product[];
  pushSettings: PushSettings;
}
