import { UserManager } from 'oidc-client-ts';

const isDev = window.location.port === '5173';

const settings = {
  authority: isDev
    ? 'http://localhost:14082/realms/avitorealm'
    : `${window.location.origin}/auth/`,
  client_id: 'avitofrontend',
  redirect_uri: isDev
    ? 'http://localhost:5173/callback'
    : `${window.location.origin}/callback`,
  post_logout_redirect_uri: isDev
    ? 'http://localhost:5173/'
    : `${window.location.origin}/`,
  response_type: 'code',
  scope: 'openid profile email',
  automaticSilentRenew: true,
  silent_redirect_uri: isDev
    ? 'http://localhost:5173/silent-renew.html'
    : `${window.location.origin}/silent-renew.html`,
};

export const userManager = new UserManager(settings);

export const login = async () => {
  try {
    await userManager.signinRedirect();
  } catch (error) {
    console.error('Login error:', error);
    throw error;
  }
};

export const logout = async () => {
  try {
    await userManager.signoutRedirect();
  } catch (error) {
    console.error('Logout error:', error);
    throw error;
  }
};

export const completeLogin = async () => {
  try {
    const user = await userManager.signinRedirectCallback();
    if (window.history && window.history.replaceState) {
      window.history.replaceState({}, document.title, window.location.pathname);
    }
    return user;
  } catch (error) {
    console.error('Complete login error:', error);
    throw error;
  }
};

export const completeLogout = async () => {
  try {
    await userManager.signoutRedirectCallback();
  } catch (error) {
    console.error('Complete logout error:', error);
    throw error;
  }
};

export const getUser = async () => {
  try {
    return await userManager.getUser();
  } catch (error) {
    console.error('Get user error:', error);
    return null;
  }
};

export const removeUser = async () => {
  try {
    await userManager.removeUser();
  } catch (error) {
    console.error('Remove user error:', error);
  }
};
