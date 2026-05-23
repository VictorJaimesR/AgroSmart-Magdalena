import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { dashboardService, alertaService } from '../services/apiServices';
import { LoadingSpinner } from '../components/UIComponents';

export default function DashboardAdminPage() {
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    totalUsuarios: 0,
    productores: 0,
    tecnicos: 0,
    asociaciones: 0,
    fincasRegistradas: 0,
    cultivosRegistrados: 0,
    alertasActivas: 0,
  });
  const [alertasRecientes, setAlertasRecientes] = useState([]);

  useEffect(() => {
    (async () => {
      try {
        const [statsRes, alertasRes] = await Promise.all([
          dashboardService.getAdminStats(),
          alertaService.listarActivas(0).catch(() => ({ data: { datos: { content: [] } } })),
        ]);

        if (statsRes?.data?.datos) {
          const d = statsRes.data.datos;
          setStats({
            totalUsuarios: d.totalUsuarios || 0,
            productores: d.productores || 0,
            tecnicos: d.tecnicos || 0,
            asociaciones: d.asociaciones || 0,
            fincasRegistradas: d.fincasRegistradas || 0,
            cultivosRegistrados: d.cultivosRegistrados || 0,
            alertasActivas: d.alertasActivas || 0,
          });
        }

        const alertas = alertasRes.data?.datos?.content || [];
        setAlertasRecientes(alertas.slice(0, 4));

      } catch (err) {
        console.error('Error loading admin stats:', err);
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  if (loading) return <LoadingSpinner text="Cargando panel de administrador..." />;

  return (
    <div className="content-wrapper">
      <div className="mb-4">
        <h2 className="fw-bold mb-1">Panel de Administración 🛠️</h2>
        <p className="text-muted mb-0">Bienvenido, {user?.nombreCompleto?.split(' ')[0]}</p>
      </div>

      {/* Stats clicables */}
      <div className="row g-3 mb-4">
        <div className="col-6 col-md-4">
          <Link to="/usuarios" style={{ textDecoration: 'none', color: 'inherit' }}>
            <div className="stat-card h-100" style={{ cursor: 'pointer' }}>
              <div className="stat-icon bg-blue"><i className="bi bi-people-fill"></i></div>
              <div>
                <div className="stat-value">{stats.totalUsuarios}</div>
                <div className="stat-label">Total usuarios</div>
              </div>
            </div>
          </Link>
        </div>

        <div className="col-6 col-md-4">
          <Link to="/usuarios" style={{ textDecoration: 'none', color: 'inherit' }}>
            <div className="stat-card h-100" style={{ cursor: 'pointer', borderLeftColor: 'var(--color-success)' }}>
              <div className="stat-icon bg-green"><i className="bi bi-people"></i></div>
              <div>
                <div className="stat-value">{stats.productores}</div>
                <div className="stat-label">Productores</div>
              </div>
            </div>
          </Link>
        </div>

        <div className="col-6 col-md-4">
          <Link to="/usuarios" style={{ textDecoration: 'none', color: 'inherit' }}>
            <div className="stat-card h-100" style={{ cursor: 'pointer', borderLeftColor: 'var(--color-accent)' }}>
              <div className="stat-icon bg-amber"><i className="bi bi-person-badge"></i></div>
              <div>
                <div className="stat-value">{stats.tecnicos}</div>
                <div className="stat-label">Técnicos</div>
              </div>
            </div>
          </Link>
        </div>

        <div className="col-6 col-md-4">
          <Link to="/admin/fincas" style={{ textDecoration: 'none', color: 'inherit' }}>
            <div className="stat-card h-100" style={{ cursor: 'pointer', borderLeftColor: 'var(--color-success)' }}>
              <div className="stat-icon bg-green"><i className="bi bi-geo-alt-fill"></i></div>
              <div>
                <div className="stat-value">{stats.fincasRegistradas}</div>
                <div className="stat-label">Fincas registradas</div>
              </div>
            </div>
          </Link>
        </div>

        <div className="col-6 col-md-4">
          <Link to="/admin/fincas" style={{ textDecoration: 'none', color: 'inherit' }}>
            <div className="stat-card h-100" style={{ cursor: 'pointer', borderLeftColor: 'var(--color-accent)' }}>
              <div className="stat-icon bg-amber"><i className="bi bi-flower1"></i></div>
              <div>
                <div className="stat-value">{stats.cultivosRegistrados}</div>
                <div className="stat-label">Cultivos registrados</div>
              </div>
            </div>
          </Link>
        </div>

        <div className="col-6 col-md-4">
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
              <Link to="/usuarios" className="btn btn-agro-outline w-100 d-flex flex-column align-items-center py-3">
                <i className="bi bi-person-plus fs-4 mb-1"></i>
                <small>Gestionar Usuarios</small>
              </Link>
            </div>
            <div className="col-6 col-md-3">
              <Link to="/admin/fincas" className="btn btn-agro-outline w-100 d-flex flex-column align-items-center py-3">
                <i className="bi bi-geo-alt fs-4 mb-1"></i>
                <small>Ver Fincas</small>
              </Link>
            </div>
            <div className="col-6 col-md-3">
              <Link to="/alertas" className="btn btn-agro-outline w-100 d-flex flex-column align-items-center py-3">
                <i className="bi bi-exclamation-triangle fs-4 mb-1"></i>
                <small>Ver Alertas</small>
              </Link>
            </div>
            <div className="col-6 col-md-3">
              <Link to="/parametros" className="btn btn-agro-outline w-100 d-flex flex-column align-items-center py-3">
                <i className="bi bi-gear fs-4 mb-1"></i>
                <small>Parámetros</small>
              </Link>
            </div>
          </div>
        </div>
      </div>

      {/* Alertas recientes */}
      {alertasRecientes.length > 0 && (
        <div className="card card-agro">
          <div className="card-header d-flex justify-content-between align-items-center">
            <span><i className="bi bi-exclamation-triangle me-2"></i>Alertas Climáticas Recientes</span>
            <Link to="/alertas" className="btn btn-sm btn-light">Ver todas</Link>
          </div>
          <div className="card-body p-0">
            {alertasRecientes.map(a => (
              <div key={a.id} className="d-flex align-items-start gap-3 p-3 border-bottom">
                <span className="badge bg-danger bg-opacity-10 text-danger p-2 rounded fs-5">
                  {a.tipo === 'TORMENTA' ? '⛈️' :
                    a.tipo === 'SEQUIA' ? '☀️' :
                      a.tipo === 'INUNDACION' ? '🌊' :
                        a.tipo === 'VIENTO_FUERTE' ? '💨' : '⚠️'}
                </span>
                <div className="flex-grow-1">
                  <div className="fw-semibold small">{a.titulo}</div>
                  <div className="text-muted small">{a.descripcion?.substring(0, 100)}...</div>
                  <div className="text-muted small mt-1">
                    <i className="bi bi-geo-alt me-1"></i>
                    {a.municipiosAfectados?.join(', ')}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}