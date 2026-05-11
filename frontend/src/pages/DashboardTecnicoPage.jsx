import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { LoadingSpinner } from '../components/UIComponents';

export default function DashboardTecnicoPage() {
  const { user } = useAuth();
  const [loading, setLoading] = useState(false);
  const [stats, setStats] = useState({
    productoresAsignados: 0,
    cultivosMonitoreados: 0,
    recomendacionesEmitidas: 0,
    alertasActivas: 0,
  });

  useEffect(() => {
    // No hay endpoints específicos por ahora -> mostrar 0 o valores vacíos
    setLoading(false);
  }, []);

  if (loading) return <LoadingSpinner text="Cargando panel técnico..." />;

  return (
    <div className="content-wrapper">
      <div className="mb-4">
        <h2 className="fw-bold mb-1">Panel Técnico</h2>
        <p className="text-muted mb-0">Bienvenido, {user?.nombreCompleto?.split(' ')[0]}</p>
      </div>

      <div className="row g-3 mb-4">
        <div className="col-6 col-md-3">
          <div className="stat-card">
            <div className="stat-icon bg-blue"><i className="bi bi-people-fill"></i></div>
            <div>
              <div className="stat-value">{stats.productoresAsignados || 0}</div>
              <div className="stat-label">Productores asignados</div>
            </div>
          </div>
        </div>

        <div className="col-6 col-md-3">
          <div className="stat-card">
            <div className="stat-icon bg-amber"><i className="bi bi-flower1"></i></div>
            <div>
              <div className="stat-value">{stats.cultivosMonitoreados || 0}</div>
              <div className="stat-label">Cultivos monitoreados</div>
            </div>
          </div>
        </div>

        <div className="col-6 col-md-3">
          <div className="stat-card">
            <div className="stat-icon bg-green"><i className="bi bi-journal-check"></i></div>
            <div>
              <div className="stat-value">{stats.recomendacionesEmitidas || 0}</div>
              <div className="stat-label">Recomendaciones</div>
            </div>
          </div>
        </div>

        <div className="col-6 col-md-3">
          <div className="stat-card">
            <div className="stat-icon bg-red"><i className="bi bi-exclamation-triangle-fill"></i></div>
            <div>
              <div className="stat-value">{stats.alertasActivas || 0}</div>
              <div className="stat-label">Alertas activas</div>
            </div>
          </div>
        </div>
      </div>

      <div className="card card-agro">
        <div className="card-body text-center text-muted">Módulo técnico en construcción.</div>
      </div>
    </div>
  );
}
