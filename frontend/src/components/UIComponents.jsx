/** Spinner de carga */
export function LoadingSpinner({ text = 'Cargando...' }) {
  return (
    <div className="loading-container">
      <div className="spinner-agro"></div>
      <span className="text-muted">{text}</span>
    </div>
  );
}

/** Estado vacío */
export function EmptyState({ icon = '📋', text = 'No hay datos disponibles', action }) {
  return (
    <div className="empty-state">
      <div className="empty-icon">{icon}</div>
      <div className="empty-text">{text}</div>
      {action && <div className="mt-3">{action}</div>}
    </div>
  );
}

/** Banner de conectividad */
export function ConnectivityBanner({ isOnline, show }) {
  if (!show) return null;
  return (
    <div className={`connectivity-banner ${isOnline ? 'online' : 'offline'}`}>
      {isOnline ? (
        <><i className="bi bi-wifi me-1"></i>Conexión restaurada</>
      ) : (
        <><i className="bi bi-wifi-off me-1"></i>Sin conexión — Los cambios se guardarán localmente</>
      )}
    </div>
  );
}

/** Toast container */
export function ToastContainer({ toasts }) {
  if (!toasts.length) return null;
  return (
    <div className="toast-container">
      {toasts.map((t) => (
        <div key={t.id} className={`alert alert-${t.type === 'error' ? 'danger' : t.type} alert-dismissible py-2 px-3 mb-2 shadow-sm`}
          style={{ minWidth: 250, fontSize: '0.85rem' }}>
          {t.message}
        </div>
      ))}
    </div>
  );
}

/** Confirm dialog simple */
export function ConfirmModal({ show, title, message, onConfirm, onCancel }) {
  if (!show) return null;
  return (
    <div className="modal d-block" style={{ background: 'rgba(0,0,0,0.5)' }}>
      <div className="modal-dialog modal-dialog-centered modal-sm">
        <div className="modal-content border-0 shadow">
          <div className="modal-header border-0 pb-0">
            <h6 className="modal-title fw-bold">{title}</h6>
          </div>
          <div className="modal-body pt-2"><p className="mb-0 text-muted small">{message}</p></div>
          <div className="modal-footer border-0 pt-0">
            <button className="btn btn-sm btn-secondary" onClick={onCancel}>Cancelar</button>
            <button className="btn btn-sm btn-danger" onClick={onConfirm}>Confirmar</button>
          </div>
        </div>
      </div>
    </div>
  );
}
