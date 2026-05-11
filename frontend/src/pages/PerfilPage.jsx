import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

export default function PerfilPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="content-wrapper">
      <div className="page-header d-flex justify-content-between align-items-center">
        <h2><i className="bi bi-person-circle me-2"></i>Mi Perfil</h2>
        <button className="btn btn-outline-danger btn-sm" onClick={handleLogout}>
          <i className="bi bi-box-arrow-right me-1"></i>Cerrar Sesión
        </button>
      </div>

      <div className="row g-4">
        <div className="col-12 col-lg-4">
          <div className="card card-agro h-100">
            <div className="card-body p-4 text-center">
              <div className="position-relative d-inline-block mb-3">
                <div className="bg-agro bg-opacity-10 d-flex align-items-center justify-content-center rounded-circle"
                  style={{ width: 100, height: 100 }}>
                  <i className="bi bi-person-fill text-agro fs-1"></i>
                </div>
                <span className="position-absolute bottom-0 end-0 bg-success border border-white border-2 rounded-circle p-2" title="Usuario Activo"></span>
              </div>
              <h4 className="fw-bold mb-1">{user?.nombreCompleto}</h4>
              <p className="text-muted mb-3">{user?.email}</p>
              <div className="d-flex justify-content-center gap-2">
                {user?.roles?.map((r) => (
                  <span key={r} className="badge bg-agro bg-opacity-10 text-agro text-uppercase p-2">
                    {r.replace('ROLE_', '')}
                  </span>
                ))}
              </div>
            </div>
          </div>
        </div>

        <div className="col-12 col-lg-8">
          <div className="card card-agro mb-4">
            <div className="card-header">
              <i className="bi bi-info-circle me-2"></i>Información General
            </div>
            <div className="card-body p-4">
              <div className="row g-4">
                <div className="col-6">
                  <label className="text-muted small d-block mb-1">Nombre Completo</label>
                  <div className="fw-semibold">{user?.nombreCompleto || 'No especificado'}</div>
                </div>
                <div className="col-6">
                  <label className="text-muted small d-block mb-1">Correo Electrónico</label>
                  <div className="fw-semibold">{user?.email}</div>
                </div>
                <div className="col-6">
                  <label className="text-muted small d-block mb-1">Identificador Sistema</label>
                  <div className="fw-semibold text-mono small">#{user?.usuarioId}</div>
                </div>
                <div className="col-6">
                  <label className="text-muted small d-block mb-1">Estado de Cuenta</label>
                  <div className="text-success fw-bold small"><i className="bi bi-check-circle-fill me-1"></i>Verificada</div>
                </div>
              </div>
            </div>
          </div>

          <div className="card card-agro opacity-75">
            <div className="card-header bg-light text-muted">
              <i className="bi bi-shield-lock me-2"></i>Seguridad (Próximamente)
            </div>
            <div className="card-body p-4">
              <div className="d-flex align-items-center gap-3">
                <div className="flex-grow-1">
                  <h6 className="mb-1 text-muted">Cambiar Contraseña</h6>
                  <p className="small text-muted mb-0">Podrás actualizar tu contraseña de acceso próximamente.</p>
                </div>
                <button className="btn btn-sm btn-light border" disabled>Configurar</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
