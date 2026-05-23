const AUTH_TOKEN_KEY = 'agrosmart_token';
const AUTH_USER_KEY = 'agrosmart_user';

function getStorage(type) {
  if (typeof window === 'undefined') return null;

  try {
    return window[type];
  } catch {
    return null;
  }
}

export function clearLegacyAuthStorage() {
  const storage = getStorage('localStorage');
  storage?.removeItem(AUTH_TOKEN_KEY);
  storage?.removeItem(AUTH_USER_KEY);
}

export function clearAuthStorage() {
  const session = getStorage('sessionStorage');
  session?.removeItem(AUTH_TOKEN_KEY);
  session?.removeItem(AUTH_USER_KEY);
  clearLegacyAuthStorage();
}

export function getStoredAuthToken() {
  return getStorage('sessionStorage')?.getItem(AUTH_TOKEN_KEY) || null;
}

export function getStoredAuthUser() {
  const token = getStoredAuthToken();
  const rawUser = getStorage('sessionStorage')?.getItem(AUTH_USER_KEY);

  if (!token || !rawUser) return null;

  try {
    return JSON.parse(rawUser);
  } catch {
    clearAuthStorage();
    return null;
  }
}

export function saveAuthSession(token, userData) {
  clearAuthStorage();

  const session = getStorage('sessionStorage');
  if (!session) return;

  session.setItem(AUTH_TOKEN_KEY, token);
  session.setItem(AUTH_USER_KEY, JSON.stringify(userData));
}
