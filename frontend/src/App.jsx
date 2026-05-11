import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import { useOnlineStatus } from './hooks/useAppHooks';
import { useToast } from './context/ToastContext';
import { ConnectivityBanner, ToastContainer } from './components/UIComponents';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';

// Pages
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import FincasPage from './pages/FincasPage';
import FincaFormPage from './pages/FincaFormPage';
import ParcelasFincaPage from './pages/ParcelasFincaPage';
import CultivosPage from './pages/CultivosPage';
import CultivoFormPage from './pages/CultivoFormPage';
import RecomendacionesPage from './pages/RecomendacionesPage';
import AlertasPage from './pages/AlertasPage';
import ReportesPage from './pages/ReportesPage';
import SyncPage from './pages/SyncPage';
import ParametrosPage from './pages/ParametrosPage';
import PerfilPage from './pages/PerfilPage';
import WelcomePage from './pages/WelcomePage';

function Layout({ children }) {
  return (
    <>
      <Navbar />
      {children}
    </>
  );
}

export default function App() {
  const { isAuthenticated, initializing } = useAuth();
  const { isOnline, showBanner } = useOnlineStatus();
  const { toasts } = useToast();

  if (initializing) return null;

  return (
    <>
      <Routes>
        {/* Rutas públicas */}
        <Route path="/login" element={isAuthenticated ? <Navigate to="/dashboard" /> : <LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        {/* Rutas protegidas */}
        <Route path="/dashboard" element={<ProtectedRoute><Layout><DashboardPage /></Layout></ProtectedRoute>} />
        <Route path="/perfil" element={<ProtectedRoute><Layout><PerfilPage /></Layout></ProtectedRoute>} />
        <Route path="/sync" element={<ProtectedRoute><Layout><SyncPage /></Layout></ProtectedRoute>} />

        {/* Fincas */}
        <Route path="/fincas" element={<ProtectedRoute roles={['AGRICULTOR', 'ADMIN']}><Layout><FincasPage /></Layout></ProtectedRoute>} />
        <Route path="/fincas/nueva" element={<ProtectedRoute roles={['AGRICULTOR', 'ADMIN']}><Layout><FincaFormPage /></Layout></ProtectedRoute>} />
        <Route path="/fincas/:id" element={<ProtectedRoute><Layout><FincaFormPage /></Layout></ProtectedRoute>} />
        <Route path="/fincas/:id/editar" element={<ProtectedRoute roles={['AGRICULTOR', 'ADMIN']}><Layout><FincaFormPage /></Layout></ProtectedRoute>} />
        <Route path="/fincas/:id/parcelas" element={<ProtectedRoute roles={['AGRICULTOR', 'ADMIN']}><Layout><ParcelasFincaPage /></Layout></ProtectedRoute>} />

        {/* Cultivos */}
        <Route path="/cultivos" element={<ProtectedRoute roles={['AGRICULTOR', 'ADMIN']}><Layout><CultivosPage /></Layout></ProtectedRoute>} />
        <Route path="/cultivos/nuevo" element={<ProtectedRoute roles={['AGRICULTOR', 'ADMIN']}><Layout><CultivoFormPage /></Layout></ProtectedRoute>} />
        <Route path="/cultivos/:id" element={<ProtectedRoute><Layout><CultivoFormPage /></Layout></ProtectedRoute>} />
        <Route path="/cultivos/:id/editar" element={<ProtectedRoute roles={['AGRICULTOR', 'ADMIN']}><Layout><CultivoFormPage /></Layout></ProtectedRoute>} />

        {/* Otros módulos */}
        <Route path="/recomendaciones" element={<ProtectedRoute><Layout><RecomendacionesPage /></Layout></ProtectedRoute>} />
        <Route path="/alertas" element={<ProtectedRoute><Layout><AlertasPage /></Layout></ProtectedRoute>} />
        <Route path="/reportes" element={<ProtectedRoute roles={['AGRICULTOR', 'ADMIN']}><Layout><ReportesPage /></Layout></ProtectedRoute>} />
        <Route path="/parametros" element={<ProtectedRoute roles={['ADMIN']}><Layout><ParametrosPage /></Layout></ProtectedRoute>} />

        {/* Default / Welcome */}
        <Route path="/" element={<WelcomePage />} />
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>

      {/* Global overlays */}
      <ConnectivityBanner isOnline={isOnline} show={showBanner} />
      <ToastContainer toasts={toasts} />
    </>
  );
}
