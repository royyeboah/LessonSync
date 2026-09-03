import { writeFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const apiUrl = (process.env.API_URL || process.env.NG_APP_API_URL || '').replace(/\/$/, '');
if (!apiUrl) {
  process.exit(0);
}

const dest = join(dirname(fileURLToPath(import.meta.url)), '../src/environments/environment.prod.ts');
writeFileSync(
  dest,
  `// Generated at build time from the API_URL environment variable.
export const environment = {
  production: true,
  apiUrl: ${JSON.stringify(apiUrl)}
};
`
);
