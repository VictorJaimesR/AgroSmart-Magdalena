import DashboardPage from './DashboardPage';
import DashboardAdminPage from './DashboardAdminPage';
import DashboardTecnicoPage from './DashboardTecnicoPage';
import { useAuth } from '../context/AuthContext';

export default function DashboardRouter() {
  const { isAdmin, isTecnico, isProductor } = useAuth();

  if (isAdmin()) return <DashboardAdminPage />;
  if (isTecnico()) return <DashboardTecnicoPage />;
  // por defecto o productor
  if (isProductor()) return <DashboardPage />;

  // fallback: si está autenticado pero sin rol claro, mostrar productor
  return <DashboardPage />;
}
