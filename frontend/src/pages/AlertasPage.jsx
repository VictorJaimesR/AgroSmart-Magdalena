import { useState, useEffect } from 'react';
import { alertaService } from '../services/apiServices';
import { LoadingSpinner, EmptyState } from '../components/UIComponents';

const TIPO_ICONS = {
  SEQUIA: '☀️', INUNDACION: '🌊', HELADA: '❄️', TORMENTA: '⛈️',
  VIENTO_FUERTE: '💨', ONDA_DE_CALOR: '🔥', GRANIZO: '🧊', OTRO: '⚠️',
};

export default function AlertasPage() {
  const [alertas, setAlertas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [tab, setTab] = useState('activas');

  useEffect(() => { loadData(); }, [page, tab]);

  const loadData = async () => {
    setLoading(true);
    try {
      const res = tab === 'historial'
        ? await alertaService.listarHistorial(page)
        : await alertaService.listarActivas(page);
      setAlertas(res.data?.datos?.content || []);
      setTotalPages(res.data?.datos?.totalPages || 0);
    } catch { setAlertas([]); }
    finally { setLoading(false); }
  };

  return (
    <div className="content-wrapper">
      <div className="page-header">
        <h2><i className="bi bi-exclamation-triangle me-2"></i>Alertas Climáticas</h2>
        <div className="text-muted small">Estas alertas son de alcance regional y aplican a la zona, no a usuarios individuales.</div>
      </div>

      <ul className="nav nav-pills mb-3 gap-1">
        <li><button className={`btn btn-sm ${tab === 'activas' ? 'btn-danger' : 'btn-outline-danger'}`}
          onClick={() => { setTab('activas'); setPage(0); }}>
          <i className="bi bi-bell-fill me-1"></i>Activas
        </button></li>
        <li><button className={`btn btn-sm ${tab === 'historial' ? 'btn-agro' : 'btn-agro-outline'}`}
          onClick={() => { setTab('historial'); setPage(0); }}>
          <i className="bi bi-clock-history me-1"></i>Historial
        </button></li>
      </ul>

      {loading ? <LoadingSpinner /> : alertas.length === 0 ? (
        <EmptyState icon="🌤️" text={tab === 'activas' ? '¡No hay alertas activas!' : 'No hay historial de alertas'} />
      ) : (
        <div className="d-flex flex-column gap-3">
          {alertas.map((a) => (
            <div key={a.id} className={`card border-0 shadow-sm ${a.activa ? 'border-start border-danger border-4' : ''}`}>
              <div className="card-body">
                <div className="d-flex align-items-start gap-3">
                  <span className="fs-3">{TIPO_ICONS[a.tipo] || '⚠️'}</span>
                  <div className="flex-grow-1">
                    <div className="d-flex justify-content-between align-items-start">
                      <h6 className="fw-bold mb-1">{a.titulo}</h6>
                      {a.activa ? <span className="badge bg-danger">Activa</span> : <span className="badge bg-secondary">Expirada</span>}
                    </div>
                    <p className="text-muted small mb-2">{a.descripcion}</p>
                    <div className="d-flex flex-wrap gap-2 mb-2">
                      {a.municipiosAfectados?.map((m, i) => (
                        <span key={i} className="badge bg-light text-dark border small">
                          <i className="bi bi-geo-alt me-1"></i>{m}
                        </span>
                      ))}
                    </div>
                    <div className="text-muted small">
                      <i className="bi bi-calendar me-1"></i>
                      Emitida: {new Date(a.fechaEmision).toLocaleDateString('es-CO')}
                      {a.fechaExpiracion && <> · Expira: {new Date(a.fechaExpiracion).toLocaleDateString('es-CO')}</>}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {totalPages > 1 && (
        <nav className="mt-4 d-flex justify-content-center">
          <div className="btn-group">
            <button className="btn btn-sm btn-agro-outline" disabled={page === 0} onClick={() => setPage(page - 1)}><i className="bi bi-chevron-left"></i></button>
            <span className="btn btn-sm btn-agro disabled">{page + 1} / {totalPages}</span>
            <button className="btn btn-sm btn-agro-outline" disabled={page >= totalPages - 1} onClick={() => setPage(page + 1)}><i className="bi bi-chevron-right"></i></button>
          </div>
        </nav>
      )}
    </div>
  );
}
