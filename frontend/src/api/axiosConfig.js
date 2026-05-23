import axios from 'axios';
import { offlineService } from '../services/offlineService';
import { getStoredAuthToken, getStoredAuthUser } from '../services/authStorage';

const API_BASE = '/api';

const api = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
});

// Interceptor: agregar JWT a cada request
api.interceptors.request.use((config) => {
  const token = getStoredAuthToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  // Agregar usuarioId si esta disponible
  const userData = getStoredAuthUser();
  if (userData?.id) {
    config.headers['X-User-Id'] = userData.id;
  }
  if (userData?.email) {
    config.headers['X-User-Email'] = userData.email;
  }
  if (userData?.nombreCompleto) {
    config.headers['X-User-Name'] = userData.nombreCompleto;
  }

  return config;
});

// Interceptor: manejar errores y cache
api.interceptors.response.use(
  (response) => {
    // Cachear automaticamente todas las respuestas GET exitosas
    if (response.config.method === 'get') {
      offlineService.cacheData(response.config.url, response.data);
    }
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      // Disparar evento para que AuthContext haga el logout limpio
      window.dispatchEvent(new Event('auth:unauthorized'));
    }
    // Fallback offline para GET (Network Error o timeout)
    if ((!error.response || error.code === 'ERR_NETWORK') && error.config.method === 'get') {
      const cached = offlineService.getCachedData(error.config.url);
      if (cached) {
        return Promise.resolve({ data: cached, status: 200, fromCache: true });
      }
    }
    return Promise.reject(error);
  }
);

export default api;
