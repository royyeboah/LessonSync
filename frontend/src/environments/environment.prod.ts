// A production build is expected to be served from the same origin as the API, which keeps the
// session cookie same-site and avoids CORS entirely.
export const environment = {
  production: true,
  apiUrl: 'https://lessonsync-production.up.railway.app/'
};
