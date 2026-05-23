import { useState, useEffect } from 'react';
import { alertaService, fincaService, supervisionService } from '../services/apiServices';
import { LoadingSpinner, EmptyState } from '../components/UIComponents';
import { useAuth } from '../context/AuthContext';

const TIPO_ICONS = {
  SEQUIA: '☀️', INUNDACION: '🌊', HELADA: '❄️', TORMENTA: '⛈️',
  VIENTO_FUERTE: '💨', ONDA_DE_CALOR: '🔥', GRANIZO: '🧊', OTRO: '⚠️',
};

const TIPO_COLOR = {
  SEQUIA: 'warning', INUNDACION: 'primary', HELADA: 'info', TORMENTA: 'danger',
  VIENTO_FUERTE: 'secondary', ONDA_DE_CALOR: 'danger', GRANIZO: 'info', OTRO: 'warning',
};

export default function AlertasPage() {
  const { user, isProductor, isTecnico } = useAuth();
  const [alertas, setAlertas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [tab, setTab] = useState('activas');
  const [municipiosFinca, setMunicipiosFinca] = useState([]);

  // Cargar municipios según el rol
  useEffect(() => {
    const cargarMunicipios = async () => {
      try {
        if (isProductor() && user?.productorId) {
          const res = await fincaService.listarPorProductor(user.productorId, 0);
          const fincas = res.data?.datos?.content || [];
          const municipios = [...new Set(fincas.map(f => f.municipio).filter(Boolean))];
          setMunicipiosFinca(municipios);
        } else if (isTecnico()) {
          const res = await supervisionService.listarMisFincas();
          const fincas = res.data?.datos || [];
          const municipios = [...new Set(fincas.map(f => f.municipio).filter(Boolean))];
          setMunicipiosFinca(municipios);
        }
      } catch {
        setMunicipiosFinca([]);
      }
    };
    cargarMunicipios();
  }, [user]);

  useEffect(() => { loadData(); }, [page, tab, municipiosFinca]);

  const loadData = async () => {
    setLoading(true);
    try {
      const res = tab === 'historial'
        ? await alertaService.listarHistorial(page)
        : await alertaService.listarActivas(page);

      let todasAlertas = res.data?.datos?.content || [];

      // Filtrar por municipios del agricultor o técnico
      if ((isProductor() || isTecnico()) && municipiosFinca.length > 0) {
        todasAlertas = todasAlertas.filter(a =>
          a.municipiosAfectados?.some(m =>
            municipiosFinca.some(mf =>
              mf.toLowerCase().includes(m.toLowerCase()) ||
              m.toLowerCase().includes(mf.toLowerCase())
            )
          )
        );
      }

      setAlertas(todasAlertas);
      setTotalPages(res.data?.datos?.totalPages || 0);
    } catch {
      setAlertas([]);
    } finally {
      setLoading(false);
    }
  };

  const emptyText = () => {
    if (tab === 'historial') return 'No hay historial de alertas';
    if ((isProductor() || isTecnico()) && municipiosFinca.length > 0)
      return `No hay alertas activas en tus municipios (${municipiosFinca.join(', ')})`;
    return '¡No hay alertas activas!';
  };

  return (
    <div className="content-wrapper">
      <div className="page-header d-flex justify-content-between align-items-start flex-wrap gap-2">
        <div>
          <h2><i className="bi bi-exclamation-triangle me-2"></i>Alertas Climáticas</h2>
          <p className="text-muted small mb-0">
            {(isProductor() || isTecnico()) && municipiosFinca.length > 0
              ? `Mostrando alertas para: ${municipiosFinca.join(', ')}`
              : 'Alertas climáticas de la región del Magdalena'}
          </p>
        </div>
      </div>

      <ul className="nav nav-pills mb-3 gap-1">
        <li>
          <button
            className={`btn btn-sm ${tab === 'activas' ? 'btn-danger' : 'btn-outline-danger'}`}
            onClick={() => { setTab('activas'); setPage(0); }}
          >
            <i className="bi bi-bell-fill me-1"></i>Activas
          </button>
        </li>
        <li>
          <button
            className={`btn btn-sm ${tab === 'historial' ? 'btn-agro' : 'btn-agro-outline'}`}
            onClick={() => { setTab('historial'); setPage(0); }}
          >
            <i className="bi bi-clock-history me-1"></i>Historial
          </button>
        </li>
      </ul>

      {loading ? <LoadingSpinner /> : alertas.length === 0 ? (
        <EmptyState icon="🌤️" text={emptyText()} />
      ) : (
        <div className="d-flex flex-column gap-3">
          {alertas.map((a) => {
            const color = TIPO_COLOR[a.tipo] || 'warning';
            return (
              <div key={a.id} className={`card border-0 shadow-sm border-start border-4 border-${color}`}>
                <div className="card-body">
                  <div className="d-flex align-items-start gap-3">
                    <span className="fs-3">{TIPO_ICONS[a.tipo] || '⚠️'}</span>
                    <div className="flex-grow-1">
                      <div className="d-flex justify-content-between align-items-start gap-2 mb-1">
                        <h6 className="fw-bold mb-0">{a.titulo}</h6>
                        <span className={`badge bg-${a.activa ? 'danger' : 'secondary'} flex-shrink-0`}>
                          {a.activa ? 'Activa' : 'Expirada'}
                        </span>
                      </div>
                      <p className="text-muted small mb-2">{a.descripcion}</p>
                      <div className="d-flex flex-wrap gap-1 mb-2">
                        {a.municipiosAfectados?.map((m, i) => (
                          <span key={i} className="badge bg-light text-dark border small">
                            <i className="bi bi-geo-alt me-1"></i>{m}
                          </span>
                        ))}
                      </div>
                      <div className="text-muted small">
                        <i className="bi bi-calendar me-1"></i>
                        Emitida: {new Date(a.fechaEmision).toLocaleDateString('es-CO')}
                        {a.fechaExpiracion && (
                          <> · Expira: {new Date(a.fechaExpiracion).toLocaleDateString('es-CO')}</>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {totalPages > 1 && (
        <nav className="mt-4 d-flex justify-content-center">
          <div className="btn-group">
            <button className="btn btn-sm btn-agro-outline" disabled={page === 0}
              onClick={() => setPage(page - 1)}>
              <i className="bi bi-chevron-left"></i>
            </button>
            <span className="btn btn-sm btn-agro disabled">{page + 1} / {totalPages}</span>
            <button className="btn btn-sm btn-agro-outline" disabled={page >= totalPages - 1}
              onClick={() => setPage(page + 1)}>
              <i className="bi bi-chevron-right"></i>
            </button>
          </div>
        </nav>
      )}
    </div>
  );
}