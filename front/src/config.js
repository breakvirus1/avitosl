
export const API_BASE_URL = '/api';

export const KEYCLOAK_CONFIG = {
  authority: 'http://localhost:14082/realms/avitorealm',
  client_id: 'avitofrontend',
  redirect_uri: 'http://localhost:5173/callback',
  post_logout_redirect_uri: 'http://localhost:5173/',
  response_type: 'code',
  scope: 'openid profile email',
  automaticSilentRenew: true,
  silent_redirect_uri: 'http://localhost:5173/silent-renew.html',
};