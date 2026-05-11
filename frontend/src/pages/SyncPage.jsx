import { usePendingOps, useOnlineStatus } from '../hooks/useAppHooks';
import { syncService } from '../services/apiServices';
import { EmptyState } from '../components/UIComponents';
import { useAuth } from '../context/AuthContext';
import { useState } from 'react';

export default function SyncPage() {
  const { ops, removeOp, clearOps } = usePendingOps();
  const { isOnline } = useOnlineStatus();
  const { user } = useAuth();
  const [syncing, setSyncing] = useState(false);
  const [result, setResult] = useState(null);

  const handleSync = async () => {
    if (!isOnline || ops.length === 0) return;
    setSyncing(true);
    setResult(null);
    try {
      const batch = ops.map((o) => ({
        entidad: o.entidad,
        accion: o.accion,
        datosJson: o.datosJson,
      }));
      const res = await syncService.pushBatch(batch);
      clearOps();
      setResult({ type: 'success', message: `${batch.length} operaciones sincronizadas correctamente` });
    } catch (err) {
      setResult({ type: 'error', message: 'Error al sincronizar: ' + (err.response?.data?.mensaje || err.message) });
    } finally { setSyncing(false); }
  };

  return (
    <div className="content-wrapper">
      <div className="page-header">
        <h2><i className="bi bi-arrow-repeat me-2"></i>Sincronización</h2>
      </div>

      {/* Estado de conexión */}
      <div className={`card mb-3 border-0 ${isOnline ? 'bg-success' : 'bg-danger'} bg-opacity-10`}>
        <div className="card-body d-flex align-items-center gap-3 py-3">
          <i className={`bi ${isOnline ? 'bi-wifi' : 'bi-wifi-off'} fs-3 ${isOnline ? 'text-success' : 'text-danger'}`}></i>
          <div>
            <div className="fw-bold">{isOnline ? 'Conectado' : 'Sin conexión'}</div>
            <small className="text-muted">{isOnline ? 'Puede sincronizar datos pendientes' : 'Los cambios se guardarán localmente'}</small>
          </div>
        </div>
      </div>

      {result && (
        <div className={`alert alert-${result.type === 'error' ? 'danger' : 'success'} py-2 small`}>{result.message}</div>
      )}

      {/* Operaciones pendientes */}
      <div className="card card-agro mb-3">
        <div className="card-header d-flex justify-content-between align-items-center">
          <span><i className="bi bi-clock-history me-2"></i>Pendientes ({ops.length})</span>
          {ops.length > 0 && (
            <div className="d-flex gap-2">
              <button className="btn btn-sm btn-warning" onClick={handleSync} disabled={!isOnline || syncing}>
                {syncing ? <><span className="spinner-border spinner-border-sm me-1"></span>Sincronizando...</> : <><i className="bi bi-cloud-arrow-up me-1"></i>Sincronizar</>}
              </button>
              <button className="btn btn-sm btn-outline-danger" onClick={clearOps}>Limpiar</button>
            </div>
          )}
        </div>
        <div className="card-body p-0">
          {ops.length === 0 ? (
            <EmptyState icon="✅" text="No hay operaciones pendientes" />
          ) : (
            <div className="list-group list-group-flush">
              {ops.map((o) => (
                <div key={o.id} className="list-group-item d-flex justify-content-between align-items-center py-3">
                  <div>
                    <span className={`badge ${o.accion === 'CREATE' ? 'bg-success' : o.accion === 'UPDATE' ? 'bg-primary' : 'bg-danger'} me-2`}>
                      {o.accion}
                    </span>
                    <span className="fw-semibold">{o.entidad}</span>
                    <div className="text-muted small mt-1">
                      <i className="bi bi-clock me-1"></i>{new Date(o.timestamp).toLocaleString('es-CO')}
                    </div>
                  </div>
                  <button className="btn btn-sm btn-outline-secondary" onClick={() => removeOp(o.id)}>
                    <i className="bi bi-x"></i>
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
