import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(email, password);
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.mensaje || 'Error al iniciar sesión. Verifique sus credenciales.');
    } finally { setLoading(false); }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <div className="login-logo">🌱</div>
        <h1 className="login-title">AgroSmart Magdalena</h1>
        <p className="login-subtitle">Plataforma de Agricultura Inteligente</p>

        {error && <div className="alert alert-danger py-2 small">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label">Correo electrónico</label>
            <div className="input-group">
              <span className="input-group-text"><i className="bi bi-envelope"></i></span>
              <input type="email" className="form-control" placeholder="ejemplo@correo.com"
                value={email} onChange={(e) => setEmail(e.target.value)} required autoComplete="email" />
            </div>
          </div>

          <div className="mb-3">
            <label className="form-label">Contraseña</label>
            <div className="input-group">
              <span className="input-group-text"><i className="bi bi-lock"></i></span>
              <input type="password" className="form-control" placeholder="••••••"
                value={password} onChange={(e) => setPassword(e.target.value)} required minLength={6} />
            </div>
          </div>

          <button type="submit" className="btn btn-agro w-100 mb-3" disabled={loading}>
            {loading ? <><span className="spinner-border spinner-border-sm me-2"></span>Ingresando...</> : <><i className="bi bi-box-arrow-in-right"></i>Ingresar</>}
          </button>
        </form>

        <div className="text-center">
          <Link to="/register" className="text-decoration-none small" style={{ color: 'var(--color-primary)' }}>
            ¿No tiene cuenta? <strong>Regístrese aquí</strong>
          </Link>
        </div>

        <hr className="my-3" />
        <p className="text-center text-muted small mb-0">Productores del Magdalena 🌿</p>
      </div>
    </div>
  );
}
