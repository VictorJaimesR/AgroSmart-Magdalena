import { useState, useEffect, useCallback } from 'react';
import { offlineService } from '../services/offlineService';

import { authService, syncService } from '../services/apiServices';

/** Hook para detectar estado de conectividad y auto-sincronizar */
export function useOnlineStatus() {
  const [isOnline, setIsOnline] = useState(navigator.onLine);
  const [showBanner, setShowBanner] = useState(false);

  useEffect(() => {
    const handleOnline = async () => { 
      setIsOnline(true); 
      setShowBanner(true); 
      setTimeout(() => setShowBanner(false), 3000);
      
      // Auto-reintento
      const pending = offlineService.getPendingOps();
      if (pending.length > 0) {
        try {
          const batch = pending.map((o) => ({
            entidad: o.entidad,
            accion: o.accion,
            datosJson: o.datosJson,
          }));
          await syncService.pushBatch(batch);
          offlineService.clearPendingOps();
        } catch (e) { console.error('Auto-sync failed', e); }
      }
    };
    const handleOffline = () => { setIsOnline(false); setShowBanner(true); };

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);
    return () => { window.removeEventListener('online', handleOnline); window.removeEventListener('offline', handleOffline); };
  }, []);

  return { isOnline, showBanner };
}

/** Hook para gestionar cola de operaciones offline */
export function usePendingOps() {
  const [count, setCount] = useState(offlineService.getPendingCount());
  const [ops, setOps] = useState(offlineService.getPendingOps());

  useEffect(() => {
    const handler = () => {
      setCount(offlineService.getPendingCount());
      setOps(offlineService.getPendingOps());
    };
    window.addEventListener('pendingOpsChanged', handler);
    return () => window.removeEventListener('pendingOpsChanged', handler);
  }, []);

  const addOp = useCallback((op) => offlineService.addPendingOp(op), []);
  const removeOp = useCallback((id) => offlineService.removePendingOp(id), []);
  const clearOps = useCallback(() => offlineService.clearPendingOps(), []);

  return { count, ops, addOp, removeOp, clearOps };
}
