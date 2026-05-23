import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { recomendacionService, cultivoService } from '../services/apiServices';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { LoadingSpinner, EmptyState } from '../components/UIComponents';

const PRIORIDAD_CONFIG = {
  BAJA: { cls: 'bg-info', label: '🟢 Baja' },
  MEDIA: { cls: 'bg-primary', label: '🔵 Media' },
  ALTA: { cls: 'bg-warning text-dark', label: '🟡 Alta' },
  CRITICA: { cls: 'bg-danger', label: '🔴 Crítica' },
};

export default function RecomendacionesPage() {
  const { user, isTecnico, isProductor } = useAuth();
  const { addToast } = useToast();

  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [tab, setTab] = useState('todas');
  const [aplicandoId, setAplicandoId] = useState(null);
  const [modalItem, setModalItem] = useState(null);

  useEffect(() => { loadData(); }, [page, tab]);

  const loadData = async () => {
    setLoading(true);
    try {
      let res;
      if (isTecnico()) {
        res = await recomendacionService.listarPendientes(page);
      } else {
        // Obtener los cultivos del agricultor primero
        const cultivosRes = await cultivoService.listarPorProductor(user?.productorId, 0);
        const cultivos = cultivosRes.data?.datos?.content || [];
        const cultivoIds = cultivos.map(c => c.id);

        if (cultivoIds.length === 0) {
          setItems([]);
          setTotalPages(0);
          setLoading(false);
          return;
        }

        if (tab === 'pendientes') {
          res = await recomendacionService.listarPendientes(page);
        } else {
          res = await recomendacionService.listarPorCultivos(cultivoIds, page);
        }
      }
      const data = res.data?.datos;
      setItems(data?.content || []);
      setTotalPages(data?.totalPages || 0);
    } catch {
      setItems([]);
    } finally {
      setLoading(false);
    }
  };

  const pConfig = (p) => PRIORIDAD_CONFIG[p] || { cls: 'bg-secondary', label: p };

  return (
    <div className="content-wrapper">
      {/* Header */}
      <div className="page-header d-flex justify-content-between align-items-start flex-wrap gap-2">
        <div>
          <h2><i className="bi bi-lightbulb me-2"></i>Recomendaciones</h2>
          <p className="text-muted mb-0">
            {isTecnico()
              ? 'Recomendaciones que has creado para los cultivos que supervisas'
              : 'Recomendaciones técnicas para tus cultivos'}
          </p>
        </div>
        {isTecnico() && (
          <Link to="/recomendaciones/nueva" className="btn btn-agro">
            <i className="bi bi-plus-circle me-1"></i>Nueva recomendación
          </Link>
        )}
      </div>

      {/* Tabs — solo para agricultor */}
      {isProductor() && (
        <div className="d-flex gap-2 mb-3">
          <button
            className={`btn btn-sm ${tab === 'todas' ? 'btn-agro' : 'btn-agro-outline'}`}
            onClick={() => { setTab('todas'); setPage(0); }}
          >
            Todas
          </button>
          <button
            className={`btn btn-sm ${tab === 'pendientes' ? 'btn-agro' : 'btn-agro-outline'}`}
            onClick={() => { setTab('pendientes'); setPage(0); }}
          >
            Pendientes
          </button>
        </div>
      )}

      {/* Contenido */}
      {loading ? (
        <LoadingSpinner />
      ) : items.length === 0 ? (
        <EmptyState
          icon="💡"
          text={isTecnico()
            ? 'No hay recomendaciones creadas aún'
            : 'No tienes recomendaciones por el momento'}
        />
      ) : (
        <div className="d-flex flex-column gap-3">
          {items.map(r => {
            const pc = pConfig(r.prioridad);
            return (
              <div
                key={r.id}
                className={`card card-agro ${!r.aplicada ? 'border-start border-4 border-success' : ''}`}
                style={{ cursor: 'pointer' }}
                onClick={() => setModalItem(r)}
              >
                <div className="card-body">
                  <div className="d-flex justify-content-between align-items-start mb-1 gap-2">
                    <h6 className="fw-bold mb-0">{r.titulo}</h6>
                    <div className="d-flex gap-1 flex-shrink-0">
                      <span className={`badge ${pc.cls} small`}>{pc.label}</span>
                      {r.aplicada
                        ? <span className="badge bg-success small"><i className="bi bi-check2 me-1"></i>Aplicada</span>
                        : <span className="badge bg-warning text-dark small">Pendiente</span>
                      }
                    </div>
                  </div>

                  <div className="text-muted small mb-2">
                    <i className="bi bi-flower1 me-1"></i>{r.cultivoNombre}
                    {r.tecnicoNombre && <> · <i className="bi bi-person me-1"></i>{r.tecnicoNombre}</>}
                  </div>

                  <p className="text-muted small mb-2" style={{
                    overflow: 'hidden',
                    display: '-webkit-box',
                    WebkitLineClamp: 2,
                    WebkitBoxOrient: 'vertical',
                  }}>
                    {r.descripcion}
                  </p>

                  <div className="d-flex justify-content-between align-items-center">
                    <span className="text-muted small">
                      <i className="bi bi-calendar me-1"></i>
                      {new Date(r.fechaEmision).toLocaleDateString('es-CO', {
                        day: '2-digit', month: 'short', year: 'numeric'
                      })}
                    </span>
                    {isProductor() && !r.aplicada && (
                      <button
                        className="btn btn-sm btn-success"
                        disabled={aplicandoId === r.id}
                        onClick={e => { e.stopPropagation(); handleAplicar(r.id); }}
                      >
                        {aplicandoId === r.id
                          ? <span className="spinner-border spinner-border-sm"></span>
                          : <><i className="bi bi-check2 me-1"></i>Marcar aplicada</>
                        }
                      </button>
                    )}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Paginación */}
      {totalPages > 1 && (
        <nav className="mt-4 d-flex justify-content-center">
          <div className="btn-group">
            <button className="btn btn-sm btn-agro-outline" disabled={page === 0} onClick={() => setPage(p => p - 1)}>
              <i className="bi bi-chevron-left"></i>
            </button>
            <span className="btn btn-sm btn-agro disabled">{page + 1} / {totalPages}</span>
            <button className="btn btn-sm btn-agro-outline" disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)}>
              <i className="bi bi-chevron-right"></i>
            </button>
          </div>
        </nav>
      )}

      {/* Modal detalle */}
      {modalItem && (
        <div className="modal d-block" tabIndex="-1" style={{ background: 'rgba(0,0,0,0.5)' }}
          onClick={() => setModalItem(null)}>
          <div className="modal-dialog modal-dialog-centered modal-lg"
            onClick={e => e.stopPropagation()}>
            <div className="modal-content border-0 shadow">
              <div className="modal-header border-0 pb-0">
                <div className="flex-grow-1">
                  <div className="d-flex gap-2 align-items-center flex-wrap mb-1">
                    <span className={`badge ${pConfig(modalItem.prioridad).cls}`}>
                      {pConfig(modalItem.prioridad).label}
                    </span>
                    {modalItem.aplicada
                      ? <span className="badge bg-success"><i className="bi bi-check2 me-1"></i>Aplicada</span>
                      : <span className="badge bg-warning text-dark">Pendiente</span>
                    }
                  </div>
                  <h5 className="modal-title fw-bold">{modalItem.titulo}</h5>
                </div>
                <button className="btn-close ms-2" onClick={() => setModalItem(null)}></button>
              </div>

              <div className="modal-body pt-2">
                <div className="row g-2 mb-3">
                  <div className="col-12 col-sm-6">
                    <small className="text-muted d-block">Cultivo</small>
                    <strong><i className="bi bi-flower1 me-1 text-success"></i>{modalItem.cultivoNombre}</strong>
                  </div>
                  {modalItem.tecnicoNombre && (
                    <div className="col-12 col-sm-6">
                      <small className="text-muted d-block">Técnico</small>
                      <strong><i className="bi bi-person me-1 text-success"></i>{modalItem.tecnicoNombre}</strong>
                    </div>
                  )}
                  <div className="col-12 col-sm-6">
                    <small className="text-muted d-block">Fecha de emisión</small>
                    <strong>{new Date(modalItem.fechaEmision).toLocaleString('es-CO')}</strong>
                  </div>
                </div>

                <div className="bg-light rounded p-3">
                  <small className="text-muted d-block mb-1 fw-semibold">Descripción / Instrucciones</small>
                  <p className="mb-0" style={{ whiteSpace: 'pre-wrap' }}>{modalItem.descripcion}</p>
                </div>
              </div>

              <div className="modal-footer border-0 pt-0 gap-2">
                {isProductor() && !modalItem.aplicada && (
                  <button
                    className="btn btn-success"
                    disabled={aplicandoId === modalItem.id}
                    onClick={() => handleAplicar(modalItem.id)}
                  >
                    {aplicandoId === modalItem.id
                      ? <span className="spinner-border spinner-border-sm me-2"></span>
                      : <i className="bi bi-check2-circle me-1"></i>
                    }
                    Marcar como aplicada
                  </button>
                )}
                <button className="btn btn-secondary" onClick={() => setModalItem(null)}>
                  Cerrar
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
