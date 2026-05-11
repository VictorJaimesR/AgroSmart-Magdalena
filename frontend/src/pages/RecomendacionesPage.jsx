import { useState, useEffect } from 'react';
import { recomendacionService } from '../services/apiServices';
import { useAuth } from '../context/AuthContext';
import { LoadingSpinner, EmptyState } from '../components/UIComponents';

export default function RecomendacionesPage() {
  const { user } = useAuth();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [tab, setTab] = useState('todas'); // todas | pendientes

  useEffect(() => { loadData(); }, [page, tab]);

  const loadData = async () => {
    setLoading(true);
    try {
      const res = tab === 'pendientes'
        ? await recomendacionService.listarPendientes(page)
        : await recomendacionService.listarPorProductor(user?.usuarioId || 1, page);
      setItems(res.data?.datos?.content || []);
      setTotalPages(res.data?.datos?.totalPages || 0);
    } catch { setItems([]); }
    finally { setLoading(false); }
  };

  const handleAplicar = async (id) => {
    try {
      await recomendacionService.aplicar(id, 'Aplicada por productor');
      loadData();
    } catch {}
  };

  const prioridadBadge = (p) => {
    const map = { BAJA: 'bg-info', MEDIA: 'bg-primary', ALTA: 'bg-warning text-dark', CRITICA: 'bg-danger' };
    return map[p] || 'bg-secondary';
  };

  return (
    <div className="content-wrapper">
      <div className="page-header">
        <h2><i className="bi bi-lightbulb me-2"></i>Recomendaciones</h2>
      </div>

      {/* Tabs */}
      <ul className="nav nav-pills mb-3 gap-1">
        <li><button className={`btn btn-sm ${tab === 'todas' ? 'btn-agro' : 'btn-agro-outline'}`}
          onClick={() => { setTab('todas'); setPage(0); }}>Todas</button></li>
        <li><button className={`btn btn-sm ${tab === 'pendientes' ? 'btn-agro' : 'btn-agro-outline'}`}
          onClick={() => { setTab('pendientes'); setPage(0); }}>Pendientes</button></li>
      </ul>

      {loading ? <LoadingSpinner /> : items.length === 0 ? (
        <EmptyState icon="💡" text="No hay recomendaciones" />
      ) : (
        <div className="d-flex flex-column gap-3">
          {items.map((r) => (
            <div key={r.id} className="card card-agro">
              <div className="card-body">
                <div className="d-flex justify-content-between align-items-start mb-2">
                  <div>
                    <h6 className="fw-bold mb-0">{r.titulo}</h6>
                    <span className="text-muted small">Cultivo: {r.cultivoNombre}</span>
                  </div>
                  <div className="d-flex gap-1">
                    <span className={`badge ${prioridadBadge(r.prioridad)} small`}>{r.prioridad}</span>
                    {r.aplicada && <span className="badge bg-success small">Aplicada</span>}
                  </div>
                </div>
                <p className="text-muted small mb-2">{r.descripcion}</p>
                <div className="d-flex justify-content-between align-items-center">
                  <span className="text-muted small">
                    <i className="bi bi-calendar me-1"></i>{new Date(r.fechaEmision).toLocaleDateString('es-CO')}
                    {r.tecnicoNombre && <> · <i className="bi bi-person me-1"></i>{r.tecnicoNombre}</>}
                  </span>
                  {!r.aplicada && (
                    <button className="btn btn-sm btn-success" onClick={() => handleAplicar(r.id)}>
                      <i className="bi bi-check2 me-1"></i>Marcar aplicada
                    </button>
                  )}
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
