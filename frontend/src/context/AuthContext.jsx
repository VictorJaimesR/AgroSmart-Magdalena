import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { authService } from '../services/apiServices';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try { return JSON.parse(localStorage.getItem('agrosmart_user')); }
    catch { return null; }
  });
  const [loading, setLoading] = useState(false);
  const [initializing, setInitializing] = useState(true);

  useEffect(() => {
    const handleUnauthorized = () => {
      localStorage.removeItem('agrosmart_token');
      localStorage.removeItem('agrosmart_user');
      setUser(null);
    };
    window.addEventListener('auth:unauthorized', handleUnauthorized);
    return () => window.removeEventListener('auth:unauthorized', handleUnauthorized);
  }, []);

  useEffect(() => {
    try {
      const token = localStorage.getItem('agrosmart_token');
      const userData = localStorage.getItem('agrosmart_user');
      if (token && userData) {
        const parsed = JSON.parse(userData);
        if (!parsed.productorId) {
          localStorage.removeItem('agrosmart_token');
          localStorage.removeItem('agrosmart_user');
          setUser(null);
        }
      }
    } catch {
      localStorage.removeItem('agrosmart_token');
      localStorage.removeItem('agrosmart_user');
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
      localStorage.setItem('agrosmart_token', token);
      localStorage.setItem('agrosmart_user', JSON.stringify(userData));
      setUser(userData);
      return userData;
    } finally { setLoading(false); }
  }, []);

  const register = useCallback(async (data) => {
    setLoading(true);
    try {
      const res = await authService.register(data);
      const { token, ...userData } = res.data.datos;
      localStorage.setItem('agrosmart_token', token);
      localStorage.setItem('agrosmart_user', JSON.stringify(userData));
      setUser(userData);
      return userData;
    } finally { setLoading(false); }
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('agrosmart_token');
    localStorage.removeItem('agrosmart_user');
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
