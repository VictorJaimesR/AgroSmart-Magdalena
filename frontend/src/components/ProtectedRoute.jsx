import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/** Guard de ruta: requiere autenticación y opcionalmente un rol */
export default function ProtectedRoute({ children, roles }) {
  const { isAuthenticated, hasRole } = useAuth();

  if (!isAuthenticated) return <Navigate to="/login" replace />;

  if (roles && roles.length > 0) {
    const authorized = roles.some((r) => hasRole(r));
    if (!authorized) return <Navigate to="/dashboard" replace />;
  }

  return children;
}
