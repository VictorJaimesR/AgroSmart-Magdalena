import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { actividadService, fincaService } from '../services/apiServices';
import { useToast } from '../context/ToastContext';

const TIPO_LABEL = {
  RIEGO:                'Riego',
  FERTILIZACION:        'Fertilización',
  CONTROL_PLAGAS:       'Control de plagas',
  CONTROL_ENFERMEDADES: 'Control de enfermedades',
  PODA:                 'Poda',
  TRASPLANTE:           'Trasplante',
  ANALISIS_SUELO:       'Análisis de suelo',
  COSECHA_PARCIAL:      'Cosecha parcial',
  COSECHA_TOTAL:        'Cosecha total',
  LIMPIEZA:             'Limpieza / Deshierbe',
};

const TIPO_ICON = {
  RIEGO:                '💧',
  FERTILIZACION:        '🌿',
  CONTROL_PLAGAS:       '🐛',
  CONTROL_ENFERMEDADES: '🍄',
  PODA:                 '✂️',
  TRASPLANTE:           '🌱',
  ANALISIS_SUELO:       '🧪',
  COSECHA_PARCIAL:      '🌾',
  COSECHA_TOTAL:        '🌾',
  LIMPIEZA:             '🧹',
};

const TIPO_COLOR = {
  RIEGO:                '#0ea5e9',
  FERTILIZACION:        '#22c55e',
  CONTROL_PLAGAS:       '#f97316',
  CONTROL_ENFERMEDADES: '#a855f7',
  PODA:                 '#64748b',
  TRASPLANTE:           '#10b981',
  ANALISIS_SUELO:       '#f59e0b',
  COSECHA_PARCIAL:      '#eab308',
  COSECHA_TOTAL:        '#ca8a04',
  LIMPIEZA:             '#6b7280',
};

const ROL_BADGE = {
  TECNICO:    { label: 'Técnico',    cls: 'bg-primary' },
  AGRICULTOR: { label: 'Agricultor', cls: 'bg-success' },
  ADMIN:      { label: 'Admin',      cls: 'bg-secondary' },
};

function formatFecha(iso) {
  if (!iso) return '-';
  return new Date(iso).toLocaleString('es-CO', {
    year: 'numeric', month: 'short', day: 'numeric',
    hour: '2-digit', minute: '2-digit',
  });
}

export default function HistorialFincaPage() {
  const { fincaId } = useParams();
  const navigate    = useNavigate();
  const { addToast } = useToast();

  const [finca,       setFinca]       = useState(null);
  const [actividades, setActividades] = useState([]);
  const [resumen,     setResumen]     = useState({});
  const [loading,     setLoading]     = useState(true);
  const [page,        setPage]        = useState(0);
  const [totalPages,  setTotalPages]  = useState(0);
  const [filtroTipo,  setFiltroTipo]  = useState('');

  const loadFinca = useCallback(async () => {
    try {
      const res = await fincaService.obtener(fincaId);
      setFinca(res.data?.datos || null);
    } catch { /* silencioso */ }
  }, [fincaId]);

  const loadActividades = useCallback(async () => {
    setLoading(true);
    try {
      const res = await actividadService.listarPorFinca(fincaId, page, filtroTipo || null);
      const datos = res.data?.datos;
      setActividades(datos?.content || []);
      setTotalPages(datos?.totalPages || 0);
    } catch (err) {
      console.error(err);
      addToast('Error cargando historial', 'error');
      setActividades([]);
    } finally {
      setLoading(false);
    }
  }, [fincaId, page, filtroTipo]);

  const loadResumen = useCallback(async () => {
    try {
      const res = await actividadService.resumenPorFinca(fincaId);
      setResumen(res.data?.datos || {});
    } catch { /* silencioso */ }
  }, [fincaId]);

  useEffect(() => { loadFinca(); loadResumen(); }, [loadFinca, loadResumen]);
  useEffect(() => { loadActividades(); }, [loadActividades]);

  const totalActividades = Object.values(resumen).reduce((s, v) => s + v, 0);

  return (
    <div className="content-wrapper">
      {/* Header */}
      <div className="page-header d-flex justify-content-between align-items-start flex-wrap gap-2">
        <div>
          <h2><i className="bi bi-clock-history me-2"></i>Historial de Actividades</h2>
          {finca && (
            <p className="text-muted mb-0">
              <i className="bi bi-geo-alt me-1"></i>
              <strong>{finca.nombre}</strong>
              {finca.municipio && ` — ${finca.municipio}`}
            </p>
          )}
        </div>
        <div className="d-flex gap-2 flex-wrap">
          <button className="btn btn-agro-outline" onClick={() => navigate(-1)}>
            <i className="bi bi-arrow-left me-1"></i>Volver
          </button>
          <Link to={`/actividades/registrar?fincaId=${fincaId}`} className="btn btn-agro">
            <i className="bi bi-plus-circle me-1"></i>Registrar actividad
          </Link>
        </div>
      </div>

      {/* Resumen por tipo */}
      {totalActividades > 0 && (
        <div className="row g-2 mb-4">
          {Object.entries(resumen).map(([tipo, count]) => (
            <div key={tipo} className="col-6 col-sm-4 col-md-3 col-lg-2">
              <div
                className="card h-100 border-0 shadow-sm"
                style={{ borderLeft: `4px solid ${TIPO_COLOR[tipo] || '#6b7280'}`, cursor: 'pointer' }}
                onClick={() => setFiltroTipo(filtroTipo === tipo ? '' : tipo)}
              >
                <div className="card-body py-2 px-3">
                  <div className="d-flex align-items-center gap-2 mb-1">
                    <span style={{ fontSize: '1.1rem' }}>{TIPO_ICON[tipo]}</span>
                    <span className="fw-bold" style={{ color: TIPO_COLOR[tipo] }}>{count}</span>
                  </div>
                  <div className="text-muted small" style={{ fontSize: '0.72rem', lineHeight: 1.2 }}>
                    {TIPO_LABEL[tipo]}
                    {filtroTipo === tipo && <span className="ms-1 badge bg-warning text-dark" style={{ fontSize: '0.65rem' }}>Filtro activo</span>}
                  </div>
                </div>
              </div>
            </div>
          ))}
          <div className="col-6 col-sm-4 col-md-3 col-lg-2">
            <div className="card h-100 border-0 shadow-sm bg-light">
              <div className="card-body py-2 px-3 d-flex flex-column justify-content-center">
                <div className="fw-bold text-muted">{totalActividades}</div>
                <div className="text-muted small" style={{ fontSize: '0.72rem' }}>Total registros</div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Filtro activo */}
      {filtroTipo && (
        <div className="alert alert-info d-flex justify-content-between align-items-center py-2 mb-3">
          <span>
            {TIPO_ICON[filtroTipo]} Mostrando solo: <strong>{TIPO_LABEL[filtroTipo]}</strong>
          </span>
          <button className="btn btn-sm btn-outline-secondary" onClick={() => setFiltroTipo('')}>
            <i className="bi bi-x me-1"></i>Quitar filtro
          </button>
        </div>
      )}

      {/* Timeline */}
      <div className="card card-agro">
        <div className="card-body p-3 p-md-4">
          {loading ? (
            <div className="text-center py-5">
              <span className="spinner-border spinner-border-sm me-2"></span>Cargando historial...
            </div>
          ) : actividades.length === 0 ? (
            <div className="text-center py-5">
              <div style={{ fontSize: '3rem' }}>📋</div>
              <h5 className="mt-3 text-muted">Sin actividades registradas</h5>
              <p className="text-muted small">
                {filtroTipo
                  ? 'No hay actividades de este tipo en la finca.'
                  : 'Aún no se han registrado actividades en esta finca.'}
              </p>
              <Link to={`/actividades/registrar?fincaId=${fincaId}`} className="btn btn-agro">
                <i className="bi bi-plus-circle me-1"></i>Registrar primera actividad
              </Link>
            </div>
          ) : (
            <div className="timeline">
              {actividades.map((a, idx) => {
                const color = TIPO_COLOR[a.tipoActividad] || '#6b7280';
                const rol   = ROL_BADGE[a.registradoPorRol] || { label: a.registradoPorRol, cls: 'bg-secondary' };
                return (
                  <div key={a.id} className="timeline-item d-flex gap-3 mb-4">
                    {/* Indicador izquierdo */}
                    <div className="d-flex flex-column align-items-center" style={{ minWidth: 40 }}>
                      <div
                        className="rounded-circle d-flex align-items-center justify-content-center shadow-sm"
                        style={{ width: 40, height: 40, background: color, fontSize: '1.1rem', flexShrink: 0 }}
                      >
                        {TIPO_ICON[a.tipoActividad]}
                      </div>
                      {idx < actividades.length - 1 && (
                        <div style={{ width: 2, flex: 1, background: '#e2e8f0', minHeight: 24, marginTop: 4 }}></div>
                      )}
                    </div>

                    {/* Contenido */}
                    <div className="card border-0 shadow-sm flex-grow-1 mb-0" style={{ borderLeft: `3px solid ${color}` }}>
                      <div className="card-body py-2 px-3">
                        <div className="d-flex flex-wrap justify-content-between align-items-start gap-1 mb-1">
                          <div className="d-flex align-items-center gap-2 flex-wrap">
                            <strong style={{ color }}>{TIPO_LABEL[a.tipoActividad]}</strong>
                            <span className={`badge ${rol.cls}`} style={{ fontSize: '0.7rem' }}>{rol.label}</span>
                          </div>
                          <small className="text-muted">
                            <i className="bi bi-calendar3 me-1"></i>{formatFecha(a.fechaActividad)}
                          </small>
                        </div>

                        {/* Cultivo */}
                        <div className="text-muted small mb-1">
                          <i className="bi bi-flower1 me-1"></i>
                          <strong>{a.cultivoNombre}</strong>
                          {a.cultivoVariedad && <span> ({a.cultivoVariedad})</span>}
                        </div>

                        {/* Cantidad + producto */}
                        {(a.cantidad || a.producto) && (
                          <div className="d-flex flex-wrap gap-3 small mb-1">
                            {a.cantidad != null && (
                              <span>
                                <i className="bi bi-123 me-1 text-muted"></i>
                                <strong>{a.cantidad}</strong> {a.unidad}
                              </span>
                            )}
                            {a.producto && (
                              <span>
                                <i className="bi bi-bag me-1 text-muted"></i>
                                {a.producto}
                              </span>
                            )}
                          </div>
                        )}

                        {/* Observaciones */}
                        {a.observaciones && (
                          <p className="text-muted small mb-1 fst-italic">"{a.observaciones}"</p>
                        )}

                        {/* Registrado por */}
                        <div className="text-muted" style={{ fontSize: '0.72rem' }}>
                          <i className="bi bi-person me-1"></i>
                          Registrado por <strong>{a.registradoPorNombre}</strong>
                          {a.createdAt && <span> · <i className="bi bi-clock me-1"></i>{formatFecha(a.createdAt)}</span>}
                        </div>
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
        </div>
      </div>
    </div>
  );
}
