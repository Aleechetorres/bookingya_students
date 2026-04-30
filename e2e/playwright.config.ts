import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  timeout: 30000,
  use: {
    baseURL: 'http://localhost:8080/api/',
    extraHTTPHeaders: {
      'Content-Type': 'application/json',
    },
  },
  reporter: [['html', { outputFolder: 'playwright-report' }], ['list']],
});
