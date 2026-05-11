import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { cultivoService } from '../services/apiServices';
import { useAuth } from '../context/AuthContext';
import { offlineService } from '../services/offlineService';
import { LoadingSpinner, EmptyState, ConfirmModal } from '../components/UIComponents';

const ESTADOS = {
  PLANIFICADO: { label: 'Planificado', class: 'planificado' },
  SEMBRADO: { label: 'Sembrado', class: 'sembrado' },
  EN_CRECIMIENTO: { label: 'En crecimiento', class: 'en_crecimiento' },
  EN_COSECHA: { label: 'En cosecha', class: 'en_crecimiento' },
  COSECHADO: { label: 'Cosechado', class: 'cosechado' },
  ABANDONADO: { label: 'Abandonado', class: 'cosechado' },
};

export default function CultivosPage() {
  const [cultivos, setCultivos] = useState([]);
  const [pendingCultivos, setPendingCultivos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [deleteId, setDeleteId] = useState(null);
  const navigate = useNavigate();
  const { user } = useAuth();

  useEffect(() => { loadCultivos(); loadPending(); }, [page]);

  const loadPending = () => {
    const ops = offlineService.getPendingOps();
    const creates = ops.filter(o => o.entidad === 'CULTIVO' && o.accion === 'CREATE')
                      .map(o => ({...JSON.parse(o.datosJson), id: 'pending-'+o.id, isPending: true}));
    setPendingCultivos(creates);
  };

  const loadCultivos = async () => {
    setLoading(true);
    try {
      const productorId = user?.productorId;
      if (!productorId) {
        setCultivos([]);
        setTotalPages(0);
        return;
      }
      const res = await cultivoService.listarPorProductor(productorId, page);
      setCultivos(res.data?.datos?.content || []);
      setTotalPages(res.data?.datos?.totalPages || 0);
    } catch { setCultivos([]); }
    finally { setLoading(false); }
  };

  const handleDelete = async () => {
    try { await cultivoService.eliminar(deleteId); loadCultivos(); } catch {}
    setDeleteId(null);
  };

  if (loading) return <LoadingSpinner />;

  return (
    <div className="content-wrapper">
      <div className="page-header">
        <h2><i className="bi bi-flower1 me-2"></i>Mis Cultivos</h2>
        <Link to="/cultivos/nuevo" className="btn btn-agro"><i className="bi bi-plus-circle"></i>Nuevo Cultivo</Link>
      </div>

      {cultivos.length === 0 && pendingCultivos.length === 0 ? (
        <EmptyState icon="🌿" text="No tienes cultivos registrados"
          action={<Link to="/cultivos/nuevo" className="btn btn-agro">Registrar cultivo</Link>} />
      ) : (
        <>
          {/* Cards para móvil */}
          <div className="row g-3">
            {[...pendingCultivos, ...cultivos].map((c) => (
              <div key={c.id} className="col-12 col-md-6 col-lg-4">
                <div className="card card-agro h-100 overflow-hidden" onClick={() => navigate(`/cultivos/${c.id}`)}>
                  {c.imagenUrl && (
                    <div className="card-img-top overflow-hidden" style={{ height: '140px' }}>
                       <img src={c.imagenUrl} alt={c.nombre} className="w-100 h-100 object-fit-cover" />
                    </div>
                  )}
                  <div className="card-body">
                    <div className="d-flex justify-content-between align-items-start mb-2">
                      <div>
                        <h6 className="fw-bold mb-0">{c.nombre}</h6>
                        {c.variedad && <span className="text-muted small">{c.variedad}</span>}
                      </div>
                      {c.isPending ? (
                         <span className="badge bg-warning text-dark small"><i className="bi bi-clock me-1"></i>Pendiente</span>
                      ) : (
                        <span className={`badge-estado ${ESTADOS[c.estado]?.class || ''}`}>
                          {ESTADOS[c.estado]?.label || c.estado}
                        </span>
                      )}
                    </div>
                    <div className="d-flex flex-wrap gap-2 text-muted small">
                      <span><i className="bi bi-geo-alt me-1"></i>{c.fincaNombre}</span>
                      <span><i className="bi bi-grid me-1"></i>{c.parcelaNombre}</span>
                      {c.areaUtilizada && <span><i className="bi bi-rulers me-1"></i>{c.areaUtilizada} ha</span>}
                    </div>
                    <div className="text-muted small mt-2">
                      <i className="bi bi-calendar me-1"></i>Siembra: {c.fechaSiembra}
                    </div>
                  </div>
                  <div className="card-footer bg-transparent border-0 d-flex gap-2 pt-0 pb-3 px-3">
                    <Link to={c.isPending ? "#" : `/cultivos/${c.id}/editar`} className={`btn btn-sm btn-agro-outline flex-grow-1 ${c.isPending ? 'disabled' : ''}`}
                      onClick={(e) => { e.stopPropagation(); if(c.isPending) e.preventDefault(); }}>
                      <i className="bi bi-pencil"></i> Editar
                    </Link>
                    <button className="btn btn-sm btn-outline-danger" disabled={c.isPending}
                      onClick={(e) => { e.stopPropagation(); setDeleteId(c.id); }}>
                      <i className="bi bi-trash"></i>
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>

          {totalPages > 1 && (
            <nav className="mt-4 d-flex justify-content-center">
              <div className="btn-group">
                <button className="btn btn-sm btn-agro-outline" disabled={page === 0} onClick={() => setPage(page - 1)}>
                  <i className="bi bi-chevron-left"></i>
                </button>
                <span className="btn btn-sm btn-agro disabled">{page + 1} / {totalPages}</span>
                <button className="btn btn-sm btn-agro-outline" disabled={page >= totalPages - 1} onClick={() => setPage(page + 1)}>
                  <i className="bi bi-chevron-right"></i>
                </button>
              </div>
            </nav>
          )}
        </>
      )}

      <ConfirmModal show={!!deleteId} title="Eliminar cultivo"
        message="¿Está seguro de eliminar este cultivo?" onConfirm={handleDelete} onCancel={() => setDeleteId(null)} />
    </div>
  );
}
