// ==============================================================================
// Servicio de almacenamiento offline
// Usa localStorage para guardar operaciones pendientes y datos recientes
// ==============================================================================

const PENDING_KEY = 'agrosmart_pending_ops';
const CACHE_KEY = 'agrosmart_cache_';

export const offlineService = {
  // --- Cola de operaciones pendientes ---
  getPendingOps() {
    try {
      return JSON.parse(localStorage.getItem(PENDING_KEY) || '[]');
    } catch { return []; }
  },

  addPendingOp(op) {
    const ops = this.getPendingOps();
    ops.push({ ...op, id: Date.now(), timestamp: new Date().toISOString() });
    localStorage.setItem(PENDING_KEY, JSON.stringify(ops));
    window.dispatchEvent(new Event('pendingOpsChanged'));
  },

  removePendingOp(id) {
    const ops = this.getPendingOps().filter((o) => o.id !== id);
    localStorage.setItem(PENDING_KEY, JSON.stringify(ops));
    window.dispatchEvent(new Event('pendingOpsChanged'));
  },

  clearPendingOps() {
    localStorage.setItem(PENDING_KEY, '[]');
    window.dispatchEvent(new Event('pendingOpsChanged'));
  },

  getPendingCount() {
    return this.getPendingOps().length;
  },

  // --- Cache de datos recientes ---
  cacheData(key, data) {
    try {
      localStorage.setItem(CACHE_KEY + key, JSON.stringify({
        data,
        timestamp: Date.now(),
      }));
    } catch (e) {
      console.warn('Cache storage full, clearing old data');
      this.clearOldCache();
    }
  },

  getCachedData(key, maxAgeMs = 30 * 60 * 1000) { // 30 min default
    try {
      const raw = localStorage.getItem(CACHE_KEY + key);
      if (!raw) return null;
      const { data, timestamp } = JSON.parse(raw);
      if (Date.now() - timestamp > maxAgeMs) return null;
      return data;
    } catch { return null; }
  },

  clearOldCache() {
    const keys = Object.keys(localStorage).filter((k) => k.startsWith(CACHE_KEY));
    keys.forEach((k) => {
      try {
        const { timestamp } = JSON.parse(localStorage.getItem(k));
        if (Date.now() - timestamp > 60 * 60 * 1000) localStorage.removeItem(k);
      } catch { localStorage.removeItem(k); }
    });
  },
};
