import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { supervisionService, recomendacionService, alertaService, cultivoService } from '../services/apiServices';
import { LoadingSpinner } from '../components/UIComponents';

export default function DashboardTecnicoPage() {
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    fincasSupervisiadas: 0,
    productoresSupervisados: 0,
    recomendacionesEmitidas: 0,
    alertasActivas: 0,
  });
  const [misFincas, setMisFincas] = useState([]);
  const [alertasRecientes, setAlertasRecientes] = useState([]);

  useEffect(() => { loadDashboard(); }, []);

  const loadDashboard = async () => {
    try {
      // Fincas supervisadas
      let fincas = [];
      let municipiosFincas = [];
      try {
        const res = await supervisionService.listarMisFincas();
        fincas = res.data?.datos || [];
        setMisFincas(fincas.slice(0, 4));
        municipiosFincas = [...new Set(fincas.map(f => f.municipio).filter(Boolean))];
      } catch (err) { console.error(err); }

      // Productores únicos supervisados
      const productoresUnicos = new Set(fincas.map(f => f.productorNombre).filter(Boolean)).size;

      // Recomendaciones pendientes del técnico
      let recomendacionesEmitidas = 0;
      try {
        const res = await recomendacionService.listarPendientes(0);
        recomendacionesEmitidas = res.data?.datos?.totalElements || 0;
      } catch (err) { console.error(err); }

      // Alertas activas filtradas por municipios supervisados
      let alertasActivas = 0;
      try {
        const res = await alertaService.listarActivas(0);
        let todas = res.data?.datos?.content || [];
        if (municipiosFincas.length > 0) {
          todas = todas.filter(a =>
            a.municipiosAfectados?.some(m =>
              municipiosFincas.some(mf =>
                mf.toLowerCase().includes(m.toLowerCase()) ||
                m.toLowerCase().includes(mf.toLowerCase())
              )
            )
          );
        }
        alertasActivas = todas.length;
        setAlertasRecientes(todas.slice(0, 3));
      } catch (err) { console.error(err); }

      setStats({
        fincasSupervisiadas: fincas.length,
        productoresSupervisados: productoresUnicos,
        recomendacionesEmitidas,
        alertasActivas,
      });

    } catch (err) {
      console.error('Error in loadDashboard técnico:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <LoadingSpinner text="Cargando panel técnico..." />;

  return (
    <div className="content-wrapper">
      <div className="mb-4">
        <h2 className="fw-bold mb-1">Panel Técnico 👨‍🌾</h2>
        <p className="text-muted mb-0">Bienvenido, {user?.nombreCompleto?.split(' ')[0]}</p>
      </div>

      {/* Stats clicables */}
      <div className="row g-3 mb-4">
        <div className="col-6 col-md-3">
          <Link to="/tecnico/mis-fincas" style={{ textDecoration: 'none', color: 'inherit' }}>
            <div className="stat-card h-100" style={{ cursor: 'pointer' }}>
              <div className="stat-icon bg-green"><i className="bi bi-geo-alt-fill"></i></div>
              <div>
                <div className="stat-value">{stats.fincasSupervisiadas}</div>
                <div className="stat-label">Fincas monitoreadas</div>
              </div>
            </div>
          </Link>
        </div>

        <div className="col-6 col-md-3">
          <Link to="/productores" style={{ textDecoration: 'none', color: 'inherit' }}>
            <div className="stat-card h-100" style={{ cursor: 'pointer', borderLeftColor: 'var(--color-info)' }}>
              <div className="stat-icon bg-blue"><i className="bi bi-people-fill"></i></div>
              <div>
                <div className="stat-value">{stats.productoresSupervisados}</div>
                <div className="stat-label">Productores supervisados</div>
              </div>
            </div>
          </Link>
        </div>

        <div className="col-6 col-md-3">
          <Link to="/recomendaciones" style={{ textDecoration: 'none', color: 'inherit' }}>
            <div className="stat-card h-100" style={{ cursor: 'pointer', borderLeftColor: 'var(--color-accent)' }}>
              <div className="stat-icon bg-amber"><i className="bi bi-journal-check"></i></div>
              <div>
                <div className="stat-value">{stats.recomendacionesEmitidas}</div>
                <div className="stat-label">Recomendaciones</div>
              </div>
            </div>
          </Link>
        </div>

        <div className="col-6 col-md-3">
          <Link to="/alertas" style={{ textDecoration: 'none', color: 'inherit' }}>
            <div className="stat-card h-100" style={{ cursor: 'pointer', borderLeftColor: 'var(--color-danger)' }}>
              <div className="stat-icon bg-red"><i className="bi bi-exclamation-triangle-fill"></i></div>
              <div>
                <div className="stat-value">{stats.alertasActivas}</div>
                <div className="stat-label">Alertas activas</div>
              </div>
            </div>
          </Link>
        </div>
      </div>

      {/* Acciones rápidas */}
      <div className="card card-agro mb-4">
        <div className="card-header">
          <i className="bi bi-lightning-charge me-2"></i>Acciones Rápidas
        </div>
        <div className="card-body">
          <div className="row g-2">
            <div className="col-6 col-md-3">
              <Link to="/actividades/registrar" className="btn btn-agro-outline w-100 d-flex flex-column align-items-center py-3">
                <i className="bi bi-journal-plus fs-4 mb-1"></i>
                <small>Registrar Actividad</small>
              </Link>
            </div>
            <div className="col-6 col-md-3">
              <Link to="/recomendaciones/nueva" className="btn btn-agro-outline w-100 d-flex flex-column align-items-center py-3">
                <i className="bi bi-lightbulb fs-4 mb-1"></i>
                <small>Nueva Recomendación</small>
              </Link>
            </div>
            <div className="col-6 col-md-3">
              <Link to="/tecnico/fincas-disponibles" className="btn btn-agro-outline w-100 d-flex flex-column align-items-center py-3">
                <i className="bi bi-geo-alt fs-4 mb-1"></i>
                <small>Fincas Disponibles</small>
              </Link>
            </div>
            <div className="col-6 col-md-3">
              <Link to="/cultivos/nuevo" className="btn btn-agro-outline w-100 d-flex flex-column align-items-center py-3">
                <i className="bi bi-flower1 fs-4 mb-1"></i>
                <small>Nuevo Cultivo</small>
              </Link>
            </div>
          </div>
        </div>
      </div>

      <div className="row g-3">
        {/* Mis fincas supervisadas */}
        <div className="col-12 col-lg-6">
          <div className="card card-agro h-100">
            <div className="card-header d-flex justify-content-between align-items-center">
              <span><i className="bi bi-briefcase me-2"></i>Mis fincas supervisadas</span>
              <Link to="/tecnico/mis-fincas" className="btn btn-sm btn-light">Ver todas</Link>
            </div>
            <div className="card-body p-0">
              {misFincas.length === 0 ? (
                <div className="text-center text-muted py-4">
                  <i className="bi bi-geo-alt fs-2 d-block mb-2"></i>
                  No supervisas ninguna finca aún.
                  <div className="mt-2">
                    <Link to="/tecnico/fincas-disponibles" className="btn btn-sm btn-agro">
                      Ver fincas disponibles
                    </Link>
                  </div>
                </div>
              ) : (
                misFincas.map(f => (
                  <div key={f.supervisionId} className="d-flex align-items-center gap-3 p-3 border-bottom">
                    <div className="bg-success bg-opacity-10 rounded p-2">
                      <i className="bi bi-geo-alt-fill text-success"></i>
                    </div>
                    <div className="flex-grow-1">
                      <div className="fw-semibold small">{f.fincaNombre}</div>
                      <div className="text-muted small">
                        {f.municipio && <span><i className="bi bi-geo-alt me-1"></i>{f.municipio} · </span>}
                        {f.productorNombre && <span><i className="bi bi-person me-1"></i>{f.productorNombre}</span>}
                      </div>
                    </div>
                    <Link
                      to={`/actividades/registrar?fincaId=${f.fincaId}`}
                      className="btn btn-sm btn-agro-outline"
                    >
                      <i className="bi bi-journal-plus"></i>
                    </Link>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>

        {/* Alertas recientes en zona supervisada */}
        <div className="col-12 col-lg-6">
          <div className="card card-agro h-100">
            <div className="card-header d-flex justify-content-between align-items-center">
              <span><i className="bi bi-exclamation-triangle me-2"></i>Alertas en tu zona</span>
              <Link to="/alertas" className="btn btn-sm btn-light">Ver todas</Link>
            </div>
            <div className="card-body p-0">
              {alertasRecientes.length === 0 ? (
                <div className="text-center text-muted py-4">
                  <i className="bi bi-sun fs-2 d-block mb-2"></i>
                  No hay alertas activas en tus municipios
                </div>
              ) : (
                alertasRecientes.map(a => (
                  <div key={a.id} className="d-flex align-items-start gap-3 p-3 border-bottom">
                    <span className="badge bg-danger bg-opacity-10 text-danger p-2 rounded fs-5">
                      {a.tipo === 'TORMENTA' ? '⛈️' :
                        a.tipo === 'SEQUIA' ? '☀️' :
                          a.tipo === 'INUNDACION' ? '🌊' :
                            a.tipo === 'VIENTO_FUERTE' ? '💨' : '⚠️'}
                    </span>
                    <div className="flex-grow-1">
                      <div className="fw-semibold small">{a.titulo}</div>
                      <div className="text-muted small">{a.descripcion?.substring(0, 80)}...</div>
                      <div className="text-muted small mt-1">
                        <i className="bi bi-geo-alt me-1"></i>
                        {a.municipiosAfectados?.join(', ')}
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}