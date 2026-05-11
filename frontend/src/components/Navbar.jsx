import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { usePendingOps } from '../hooks/useAppHooks';

export default function Navbar() {
  const { user, logout, isAdmin, isTecnico, isProductor } = useAuth();
  const { count } = usePendingOps();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => { logout(); navigate('/login'); };
  const isActive = (path) => location.pathname.startsWith(path) ? 'active' : '';

  return (
    <nav className="navbar navbar-expand-lg navbar-agro sticky-top">
      <div className="container-fluid">
        <Link className="navbar-brand" to="/dashboard">
          <span>🌱</span> AgroSmart
        </Link>

        <div className="d-flex align-items-center gap-2 d-lg-none">
          {count > 0 && (
            <Link to="/sync" className="btn btn-sm btn-warning position-relative">
              <i className="bi bi-cloud-arrow-up"></i>
              <span className="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger">{count}</span>
            </Link>
          )}
          <button className="navbar-toggler border-0" type="button" data-bs-toggle="collapse" data-bs-target="#navMain">
            <i className="bi bi-list text-white fs-4"></i>
          </button>
        </div>

        <div className="collapse navbar-collapse" id="navMain">
          <ul className="navbar-nav me-auto mb-2 mb-lg-0">
            <li className="nav-item">
              <Link className={`nav-link ${isActive('/dashboard')}`} to="/dashboard">
                <i className="bi bi-house-door me-1"></i>Inicio
              </Link>
            </li>

            {(isProductor() || isAdmin()) && (
              <>
                <li className="nav-item">
                  <Link className={`nav-link ${isActive('/fincas')}`} to="/fincas">
                    <i className="bi bi-geo-alt me-1"></i>Fincas
                  </Link>
                </li>
                <li className="nav-item">
                  <Link className={`nav-link ${isActive('/cultivos')}`} to="/cultivos">
                    <i className="bi bi-flower1 me-1"></i>Cultivos
                  </Link>
                </li>
              </>
            )}

            <li className="nav-item">
              <Link className={`nav-link ${isActive('/recomendaciones')}`} to="/recomendaciones">
                <i className="bi bi-lightbulb me-1"></i>Recomendaciones
              </Link>
            </li>

            <li className="nav-item">
              <Link className={`nav-link ${isActive('/alertas')}`} to="/alertas">
                <i className="bi bi-exclamation-triangle me-1"></i>Alertas
              </Link>
            </li>

            {(isProductor() || isAdmin()) && (
              <li className="nav-item">
                <Link className={`nav-link ${isActive('/reportes')}`} to="/reportes">
                  <i className="bi bi-file-earmark-bar-graph me-1"></i>Reportes
                </Link>
              </li>
            )}

            {isAdmin() && (
              <li className="nav-item">
                <Link className={`nav-link ${isActive('/parametros')}`} to="/parametros">
                  <i className="bi bi-gear me-1"></i>Parámetros
                </Link>
              </li>
            )}
          </ul>

          <div className="d-flex align-items-center gap-2">
            {count > 0 && (
              <Link to="/sync" className="btn btn-sm btn-warning d-none d-lg-flex align-items-center gap-1">
                <i className="bi bi-cloud-arrow-up"></i>
                <span>{count} pendientes</span>
              </Link>
            )}

            <div className="dropdown">
              <button className="btn btn-sm text-white dropdown-toggle d-flex align-items-center gap-1"
                type="button" data-bs-toggle="dropdown" style={{ background: 'rgba(255,255,255,0.15)' }}>
                <i className="bi bi-person-circle"></i>
                <span className="d-none d-sm-inline">{user?.nombreCompleto?.split(' ')[0]}</span>
              </button>
              <ul className="dropdown-menu dropdown-menu-end">
                <li><span className="dropdown-item-text small text-muted">{user?.email}</span></li>
                <li><hr className="dropdown-divider" /></li>
                <li><Link className="dropdown-item" to="/perfil"><i className="bi bi-person me-2"></i>Mi perfil</Link></li>
                <li><Link className="dropdown-item" to="/sync"><i className="bi bi-arrow-repeat me-2"></i>Sincronización</Link></li>
                <li><hr className="dropdown-divider" /></li>
                <li><button className="dropdown-item text-danger" onClick={handleLogout}><i className="bi bi-box-arrow-right me-2"></i>Cerrar sesión</button></li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </nav>
  );
}
