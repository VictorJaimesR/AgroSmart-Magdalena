import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { authService } from '../services/apiServices';
import {
  clearAuthStorage,
  clearLegacyAuthStorage,
  getStoredAuthUser,
  saveAuthSession,
} from '../services/authStorage';
import { offlineService } from '../services/offlineService';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => getStoredAuthUser());
  const [loading, setLoading] = useState(false);
  const [initializing, setInitializing] = useState(true);

  useEffect(() => {
    const handleUnauthorized = () => {
      clearAuthStorage();
      setUser(null);
    };
    window.addEventListener('auth:unauthorized', handleUnauthorized);
    return () => window.removeEventListener('auth:unauthorized', handleUnauthorized);
  }, []);

  useEffect(() => {
    try {
      clearLegacyAuthStorage();

      const parsed = getStoredAuthUser();
      if (!parsed) {
        offlineService.setUserScope(null);
        clearAuthStorage();
        setUser(null);
        return;
      }

      const hasProductorRole = parsed.roles && (parsed.roles.includes('ROLE_AGRICULTOR') || parsed.roles.includes('AGRICULTOR'));
      if (hasProductorRole && !parsed.productorId) {
        offlineService.setUserScope(null);
        clearAuthStorage();
        setUser(null);
        return;
      }

      offlineService.setUserScope(parsed.id);
      setUser(parsed);
    } catch {
      offlineService.setUserScope(null);
      clearAuthStorage();
      setUser(null);
    } finally {
      setInitializing(false);
    }
  }, []);

  const login = useCallback(async (email, password) => {
    setLoading(true);
    try {
      const res = await authService.login(email, password);
      const { token, ...userData } = res.data.datos;
      saveAuthSession(token, userData);
      offlineService.setUserScope(userData.id);
      setUser(userData);
      return userData;
    } finally { setLoading(false); }
  }, []);

  const register = useCallback(async (data) => {
    setLoading(true);
    try {
      const res = await authService.register(data);
      const { token, ...userData } = res.data.datos;
      saveAuthSession(token, userData);
      offlineService.setUserScope(userData.id);
      setUser(userData);
      return userData;
    } finally { setLoading(false); }
  }, []);

  const logout = useCallback(() => {
    clearAuthStorage();
    offlineService.setUserScope(null);
    setUser(null);
  }, []);

  const hasRole = useCallback((role) => {
    if (!user?.roles) return false;
    const r = role.startsWith('ROLE_') ? role : `ROLE_${role}`;
    return user.roles.includes(r);
  }, [user]);

  const isAdmin = useCallback(() => hasRole('ADMIN'), [hasRole]);
  const isProductor = useCallback(() => hasRole('AGRICULTOR'), [hasRole]);
  const isTecnico = useCallback(() => hasRole('TECNICO'), [hasRole]);

  return (
    <AuthContext.Provider value={{
      user, loading, initializing, login, register, logout, hasRole, isAdmin, isProductor, isTecnico,
      isAuthenticated: !!user,
    }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
};
