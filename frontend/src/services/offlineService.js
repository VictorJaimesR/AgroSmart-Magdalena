// ==============================================================================
// Servicio de almacenamiento offline
// Usa localStorage para guardar operaciones pendientes y datos recientes
// ==============================================================================

const PENDING_KEY = 'agrosmart_pending_ops';
const CACHE_KEY = 'agrosmart_cache_';

let activeScope = 'anonymous';

function storageKey() {
  return `${PENDING_KEY}_${activeScope}`;
}

function readJson(key, fallback) {
  try {
    return JSON.parse(localStorage.getItem(key) || JSON.stringify(fallback));
  } catch {
    return fallback;
  }
}

function writePendingOps(ops) {
  localStorage.setItem(storageKey(), JSON.stringify(ops));
  window.dispatchEvent(new Event('pendingOpsChanged'));
}

function createClientId(entidad = 'op') {
  const prefix = String(entidad).toLowerCase();
  if (typeof crypto !== 'undefined' && crypto.randomUUID) return `${prefix}-${crypto.randomUUID()}`;
  return `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2)}`;
}

function normalizeData(op, clientId, localId) {
  const raw = op.datosJson ? readInlineJson(op.datosJson) : (op.datos || op.data || {});
  const data = { ...raw };
  data.clientId = clientId;
  if (localId) data.localId = localId;
  return data;
}

function readInlineJson(raw) {
  try {
    return JSON.parse(raw);
  } catch {
    return {};
  }
}

function replaceLocalReferences(op, resolvedIds) {
  if (resolvedIds.size === 0) return op;

  const data = readInlineJson(op.datosJson);
  ['id', 'fincaId', 'parcelaId', 'cultivoId'].forEach((field) => {
    const value = data[field];
    if (typeof value === 'string' && resolvedIds.has(value)) {
      data[field] = resolvedIds.get(value);
    }
  });

  return { ...op, datosJson: JSON.stringify(data) };
}

export const offlineService = {
  setUserScope(userId) {
    activeScope = userId ? `user_${userId}` : 'anonymous';

    const legacyOps = readJson(PENDING_KEY, []);
    if (legacyOps.length > 0 && this.getPendingOps().length === 0) {
      localStorage.setItem(storageKey(), JSON.stringify(legacyOps));
      localStorage.setItem(PENDING_KEY, '[]');
      window.dispatchEvent(new Event('pendingOpsChanged'));
    }
  },

  // --- Cola de operaciones pendientes ---
  getPendingOps() {
    return readJson(storageKey(), []);
  },

  addPendingOp(op) {
    const ops = this.getPendingOps();
    const clientId = op.clientId || createClientId(op.entidad);
    const localId = op.localId || (op.accion === 'CREATE' ? `local-${op.entidad.toLowerCase()}-${clientId}` : null);
    const data = normalizeData(op, clientId, localId);

    ops.push({
      ...op,
      id: op.id || clientId,
      clientId,
      localId,
      datosJson: JSON.stringify(data),
      timestamp: new Date().toISOString(),
      lastError: null,
    });
    writePendingOps(ops);
    return { clientId, localId, data };
  },

  removePendingOp(id) {
    const ops = this.getPendingOps().filter((o) => o.id !== id);
    writePendingOps(ops);
  },

  clearPendingOps() {
    writePendingOps([]);
  },

  getPendingCount() {
    return this.getPendingOps().length;
  },

  buildSyncBatch(ops = this.getPendingOps()) {
    return ops.map((o) => ({
      clientId: o.clientId || String(o.id),
      entidad: o.entidad,
      accion: o.accion,
      datosJson: o.datosJson,
    }));
  },

  applySyncResults(results = []) {
    const byClientId = new Map(results.filter((r) => r.clientId).map((r) => [r.clientId, r]));
    const resolvedIds = new Map(
      results
        .filter((r) => r.estado === 'SINCRONIZADO' && r.localId && r.serverId)
        .map((r) => [r.localId, r.serverId])
    );
    let synced = 0;
    let failed = 0;

    const nextOps = this.getPendingOps()
      .map((op) => {
        const result = byClientId.get(op.clientId || String(op.id));
        const updatedOp = replaceLocalReferences(op, resolvedIds);
        if (!result) return updatedOp;

        if (result.estado === 'SINCRONIZADO') {
          synced += 1;
          return null;
        }

        if (result.estado === 'ERROR') {
          failed += 1;
          return {
            ...updatedOp,
            lastError: result.mensajeError || 'No se pudo sincronizar esta operacion',
          };
        }

        return op;
      })
      .filter(Boolean);

    writePendingOps(nextOps);
    return { synced, failed, remaining: nextOps.length };
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
