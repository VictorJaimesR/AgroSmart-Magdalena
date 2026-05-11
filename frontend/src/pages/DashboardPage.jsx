import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { dashboardService, alertaService, fincaService, cultivoService } from '../services/apiServices';
import { LoadingSpinner } from '../components/UIComponents';

export default function DashboardPage() {
  const { user, isAdmin, isProductor, isTecnico } = useAuth();

  const [stats, setStats] = useState({
    totalFincas: 0,
    cultivosActivos: 0,
    alertasActivas: 0,
    recomendacionesPendientes: 0,
    syncPendientes: 0,
    productoresAsociados: 0,
    totalUsuarios: 0,
  });

  const [alertas, setAlertas] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboard();
  }, []);

  const loadDashboard = async () => {
    try {
      const [alertasRes] = await Promise.all([
        alertaService
          .listarActivas(0)
          .catch(() => ({ data: { datos: { content: [], totalElements: 0 } } })),
      ]);

      setAlertas(alertasRes.data?.datos?.content?.slice(0, 3) || []);

      let totalFincas = 0;
      let cultivosActivos = 0;

      if (user?.productorId) {
        try {
          const fincasRes = await fincaService.listarPorProductor(user.productorId, 0);
          totalFincas = fincasRes.data?.datos?.totalElements || 0;
        } catch (err) {
          console.error('Error loading fincas:', err);
        }

        try {
          const cultivosRes = await cultivoService.listarPorProductor(user.productorId, 0);
          cultivosActivos = cultivosRes.data?.datos?.totalElements || 0;
        } catch (err) {
          console.error('Error loading cultivos:', err);
        }
      }

      try {
        let res;

        if (isAdmin()) {
          res = await dashboardService.getAdminStats();
        } else {
          res = await dashboardService.getProductorStats();
        }

        let pending = 0;

        if (!isAdmin()) {
          try {
            const localQueue = JSON.parse(
              localStorage.getItem('agrosmart_pending_ops') || '[]'
            );
            pending = localQueue.length;
          } catch (e) {
            pending = 0;
          }
        }

        setStats((s) => ({
          ...s,
          ...res.data?.datos,
          totalFincas,
          cultivosActivos,
          syncPendientes: (res.data?.datos?.syncPendientes || 0) + pending,
        }));
      } catch (err) {
        console.error('Error loading dashboard stats:', err);

        setStats((s) => ({
          ...s,
          totalFincas,
          cultivosActivos,
        }));
      }
    } catch (err) {
      console.error('Error in loadDashboard:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <LoadingSpinner text="Cargando tu panel..." />;

  return (
    <div className="content-wrapper">
      <div className="mb-4">
        <h2 className="fw-bold mb-1">
          ¡Hola, {user?.nombreCompleto?.split(' ')[0]}! 👋
        </h2>
        <p className="text-muted mb-0">
          {isAdmin() && 'Panel de administración del sistema'}
          {isProductor() && 'Resumen de tus unidades productivas'}
          {isTecnico() && 'Tus recomendaciones y zonas asignadas'}
        </p>
      </div>

      <div className="row g-3 mb-4">
        <div className="col-6 col-md-3">
          <div className="stat-card">
            <div className="stat-icon bg-green">
              <i className="bi bi-geo-alt-fill"></i>
            </div>
            <div>
              <div className="stat-value">
                {stats.totalFincas || stats.fincasTotales || 0}
              </div>
              <div className="stat-label">Fincas</div>
            </div>
          </div>
        </div>

        <div className="col-6 col-md-3">
          <div className="stat-card" style={{ borderLeftColor: 'var(--color-accent)' }}>
            <div className="stat-icon bg-amber">
              <i className="bi bi-flower1"></i>
            </div>
            <div>
              <div className="stat-value">
                {stats.cultivosActivos ||
                  stats.cultivosTotales ||
                  stats.cultivosPorZonaActivos ||
                  0}
              </div>
              <div className="stat-label">Cultivos</div>
            </div>
          </div>
        </div>

        <div className="col-6 col-md-3">
          <div className="stat-card" style={{ borderLeftColor: 'var(--color-danger)' }}>
            <div className="stat-icon bg-red">
              <i className="bi bi-exclamation-triangle-fill"></i>
            </div>
            <div>
              <div className="stat-value">
                {stats.alertasActivas || stats.alertasRelevantes || stats.alertasEmitidas || 0}
              </div>
              <div className="stat-label">Alertas activas</div>
            </div>
          </div>
        </div>

        {isAdmin() || isProductor() ? (
          <>
            <div className="col-6 col-md-3">
              <div className="stat-card" style={{ borderLeftColor: 'var(--color-info)' }}>
                <div className="stat-icon bg-blue">
                  <i className="bi bi-lightbulb-fill"></i>
                </div>
                <div>
                  <div className="stat-value">{stats.recomendacionesPendientes || 0}</div>
                  <div className="stat-label">Recomendaciones</div>
                </div>
              </div>
            </div>

            {isProductor() && (
              <div className="col-6 col-md-3 mt-3">
                <div className="stat-card" style={{ borderLeftColor: '#6c757d' }}>
                  <div className="stat-icon bg-secondary text-white">
                    <i className="bi bi-cloud-arrow-up-fill"></i>
                  </div>
                  <div>
                    <div className="stat-value">{stats.syncPendientes || 0}</div>
                    <div className="stat-label">Pendientes de Sync</div>
                  </div>
                </div>
              </div>
            )}
          </>
        ) : (
          <div className="col-6 col-md-3">
            <div className="stat-card" style={{ borderLeftColor: 'var(--color-info)' }}>
              <div className="stat-icon bg-blue">
                <i className="bi bi-people-fill"></i>
              </div>
              <div>
                <div className="stat-value">
                  {stats.productoresAsociados || stats.totalUsuarios || 0}
                </div>
                <div className="stat-label">Productores / Usuarios</div>
              </div>
            </div>
          </div>
        )}
      </div>

      <div className="card card-agro mb-4">
        <div className="card-header">
          <i className="bi bi-lightning-charge me-2"></i>
          Acciones Rápidas
        </div>
        <div className="card-body">
          <div className="row g-2">
            {(isProductor() || isAdmin()) && (
              <>
                <div className="col-6 col-md-3">
                  <Link
                    to="/fincas/nueva"
                    className="btn btn-agro-outline w-100 d-flex flex-column align-items-center py-3"
                  >
                    <i className="bi bi-plus-circle fs-4 mb-1"></i>
                    <small>Nueva Finca</small>
                  </Link>
                </div>

                <div className="col-6 col-md-3">
                  <Link
                    to="/cultivos/nuevo"
                    className="btn btn-agro-outline w-100 d-flex flex-column align-items-center py-3"
                  >
                    <i className="bi bi-flower1 fs-4 mb-1"></i>
                    <small>Nuevo Cultivo</small>
                  </Link>
                </div>
              </>
            )}

            <div className="col-6 col-md-3">
              <Link
                to="/alertas"
                className="btn btn-agro-outline w-100 d-flex flex-column align-items-center py-3"
              >
                <i className="bi bi-cloud-sun fs-4 mb-1"></i>
                <small>Ver Alertas</small>
              </Link>
            </div>

            <div className="col-6 col-md-3">
              <Link
                to="/recomendaciones"
                className="btn btn-agro-outline w-100 d-flex flex-column align-items-center py-3"
              >
                <i className="bi bi-journal-check fs-4 mb-1"></i>
                <small>Recomendaciones</small>
              </Link>
            </div>
          </div>
        </div>
      </div>

      {alertas.length > 0 && (
        <div className="card card-agro">
          <div className="card-header d-flex justify-content-between align-items-center">
            <span>
              <i className="bi bi-exclamation-triangle me-2"></i>
              Alertas Recientes
            </span>
            <Link to="/alertas" className="btn btn-sm btn-light">
              Ver todas
            </Link>
          </div>

          <div className="card-body p-0">
            {alertas.map((a) => (
              <div key={a.id} className="d-flex align-items-start gap-3 p-3 border-bottom">
                <span className="badge bg-danger bg-opacity-10 text-danger p-2 rounded">
                  <i className="bi bi-exclamation-triangle"></i>
                </span>
                <div className="flex-grow-1">
                  <div className="fw-semibold small">{a.titulo}</div>
                  <div className="text-muted small">
                    {a.descripcion?.substring(0, 100)}...
                  </div>
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