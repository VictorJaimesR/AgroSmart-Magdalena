import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { fincaService } from '../services/apiServices';
import { offlineService } from '../services/offlineService';
import { LoadingSpinner, EmptyState, ConfirmModal } from '../components/UIComponents';
import { useToast } from '../context/ToastContext';
import { useAuth } from '../context/AuthContext';

export default function FincasPage() {
  const [fincas, setFincas] = useState([]);
  const [pendingFincas, setPendingFincas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [deleteId, setDeleteId] = useState(null);
  const { addToast } = useToast();
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => { loadFincas(); loadPending(); }, [page]);

  const loadPending = () => {
    const ops = offlineService.getPendingOps();
    const creates = ops.filter(o => o.entidad === 'FINCA' && o.accion === 'CREATE')
                      .map(o => ({...JSON.parse(o.datosJson), id: 'pending-'+o.id, isPending: true}));
    setPendingFincas(creates);
  };

  const loadFincas = async () => {
    setLoading(true);
    try {
      const productorId = user?.productorId;
      if (!productorId) {
        setFincas([]);
        setTotalPages(0);
        return;
      }
      const res = await fincaService.listarPorProductor(productorId, page);
      setFincas(res.data?.datos?.content || []);
      setTotalPages(res.data?.datos?.totalPages || 0);
    } catch { setFincas([]); }
    finally { setLoading(false); }
  };

  const handleDelete = async () => {
    try {
      await fincaService.eliminar(deleteId);
      loadFincas();
    } catch {}
    setDeleteId(null);
  };

  if (loading) return <LoadingSpinner />;

  return (
    <div className="content-wrapper">
      <div className="page-header">
        <h2><i className="bi bi-geo-alt me-2"></i>Mis Fincas</h2>
        <Link to="/fincas/nueva" className="btn btn-agro"><i className="bi bi-plus-circle"></i>Nueva Finca</Link>
      </div>

      {fincas.length === 0 && pendingFincas.length === 0 ? (
        <EmptyState icon="🏡" text="Aún no tienes fincas registradas"
          action={<Link to="/fincas/nueva" className="btn btn-agro">Registrar primera finca</Link>} />
      ) : (
        <>
          {/* Vista lista para móviles */}
          <div className="row g-3">
            {[...pendingFincas, ...fincas].map((f) => (
              <div key={f.id} className="col-12 col-md-6">
                <div className="card card-agro h-100" onClick={() => navigate(`/fincas/${f.id}`)}>
                  <div className="card-body">
                    <div className="d-flex justify-content-between align-items-start mb-2">
                      <h6 className="fw-bold mb-0">{f.nombre}</h6>
                      {f.isPending ? (
                         <span className="badge bg-warning text-dark small"><i className="bi bi-clock me-1"></i>Pendiente</span>
                      ) : (
                         <span className="badge bg-success bg-opacity-10 text-success small">Activa</span>
                      )}
                    </div>
                    <div className="d-flex flex-wrap gap-3 text-muted small mb-2">
                      <span><i className="bi bi-rulers me-1"></i>{f.areaTotal} {f.unidadArea || 'ha'}</span>
                      <span><i className="bi bi-geo-alt me-1"></i>{f.municipio}</span>
                      <span><i className="bi bi-grid me-1"></i>{f.cantidadParcelas || 0} parcelas</span>
                      {f.latitud && f.longitud && (
                        <a href={`https://www.google.com/maps/search/?api=1&query=${f.latitud},${f.longitud}`} 
                           target="_blank" rel="noreferrer" className="text-primary text-decoration-none"
                           onClick={(e) => e.stopPropagation()}>
                          <i className="bi bi-map me-1"></i>Ver Mapa
                        </a>
                      )}
                    </div>
                    {f.descripcion && <p className="text-muted small mb-0">{f.descripcion.substring(0, 80)}</p>}
                  </div>
                  <div className="card-footer bg-transparent border-0 d-flex gap-2 pt-0 pb-3 px-3">
                    <Link to={f.isPending ? "#" : `/fincas/${f.id}/editar`} className={`btn btn-sm btn-agro-outline flex-grow-1 ${f.isPending ? 'disabled' : ''}`}
                      onClick={(e) => { e.stopPropagation(); if(f.isPending) e.preventDefault(); }}>
                      <i className="bi bi-pencil"></i> Editar
                    </Link>
                    <Link to={f.isPending ? "#" : `/fincas/${f.id}/parcelas`} className={`btn btn-sm btn-outline-primary flex-grow-1 ${f.isPending ? 'disabled' : ''}`}
                      onClick={(e) => { e.stopPropagation(); if (f.isPending) e.preventDefault(); }}>
                      <i className="bi bi-grid-3x3-gap"></i> Parcelas
                    </Link>
                    <button className="btn btn-sm btn-outline-danger flex-grow-1"
                      disabled={f.isPending}
                      onClick={(e) => { e.stopPropagation(); setDeleteId(f.id); }}>
                      <i className="bi bi-trash"></i> Eliminar
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>

          {/* Paginación */}
          {totalPages > 1 && (
            <nav className="mt-4 d-flex justify-content-center">
              <div className="btn-group">
                <button
                 className="btn btn-sm btn-agro-outline" disabled={page === 0} onClick={() => setPage(page - 1)}>
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

      <ConfirmModal show={!!deleteId} title="Eliminar finca"
        message="¿Está seguro de eliminar esta finca? Los lotes y cultivos asociados también se desactivarán."
        onConfirm={handleDelete} onCancel={() => setDeleteId(null)} />
    </div>
  );
}
