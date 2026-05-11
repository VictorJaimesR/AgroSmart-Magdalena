import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { dashboardService } from '../services/apiServices';
import { LoadingSpinner } from '../components/UIComponents';

export default function DashboardAdminPage() {
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    totalUsuarios: 0,
    productores: 0,
    tecnicos: 0,
    fincas: 0,
    cultivos: 0,
    alertas: 0,
  });

  useEffect(() => {
    (async () => {
      try {
        const res = await dashboardService.getAdminStats().catch(() => null);
        if (res && res.data && res.data.datos) {
          setStats((s) => ({ ...s, ...res.data.datos }));
        }
      } catch (err) {
        console.error('Error cargando admin stats', err);
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  if (loading) return <LoadingSpinner text="Cargando panel de administrador..." />;

  return (
    <div className="content-wrapper">
      <div className="mb-4">
        <h2 className="fw-bold mb-1">Panel de Administración</h2>
        <p className="text-muted mb-0">Bienvenido, {user?.nombreCompleto?.split(' ')[0]}</p>
      </div>

      <div className="row g-3 mb-4">
        <div className="col-6 col-md-4">
          <div className="stat-card">
            <div className="stat-icon bg-blue"><i className="bi bi-people-fill"></i></div>
            <div>
              <div className="stat-value">{stats.totalUsuarios || 0}</div>
              <div className="stat-label">Total usuarios</div>
            </div>
          </div>
        </div>

        <div className="col-6 col-md-4">
          <div className="stat-card">
            <div className="stat-icon bg-green"><i className="bi bi-people"></i></div>
            <div>
              <div className="stat-value">{stats.productores || 0}</div>
              <div className="stat-label">Productores</div>
            </div>
          </div>
        </div>

        <div className="col-6 col-md-4">
          <div className="stat-card">
            <div className="stat-icon bg-amber"><i className="bi bi-person-badge"></i></div>
            <div>
              <div className="stat-value">{stats.tecnicos || 0}</div>
              <div className="stat-label">Técnicos</div>
            </div>
          </div>
        </div>

        <div className="col-6 col-md-4">
          <div className="stat-card">
            <div className="stat-icon bg-green"><i className="bi bi-geo-alt-fill"></i></div>
            <div>
              <div className="stat-value">{stats.fincas || 0}</div>
              <div className="stat-label">Fincas registradas</div>
            </div>
          </div>
        </div>

        <div className="col-6 col-md-4">
          <div className="stat-card">
            <div className="stat-icon bg-amber"><i className="bi bi-flower1"></i></div>
            <div>
              <div className="stat-value">{stats.cultivos || 0}</div>
              <div className="stat-label">Cultivos registrados</div>
            </div>
          </div>
        </div>

        <div className="col-6 col-md-4">
          <div className="stat-card">
            <div className="stat-icon bg-red"><i className="bi bi-exclamation-triangle-fill"></i></div>
            <div>
              <div className="stat-value">{stats.alertas || 0}</div>
              <div className="stat-label">Alertas activas</div>
            </div>
          </div>
        </div>
      </div>

      <div className="card card-agro">
        <div className="card-body text-center text-muted">Módulos administrativos principales disponibles aquí.</div>
      </div>
    </div>
  );
}
