// Default is same-origin. Vercel builds set this from the API_URL environment variable
// so the Angular app can call a Railway backend on a different host.
export const environment = {
  production: true,
  apiUrl: 'https://lessonsync-production.up.railway.app/'
};
