import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function RegisterPage() {
  const [form, setForm] = useState({ nombreCompleto: '', email: '', password: '', telefono: '', rol: 'AGRICULTOR', cedula: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { register, logout, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (isAuthenticated) {
      logout();
    }
  }, [isAuthenticated, logout]);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await register(form);
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.mensaje || 'Error al registrarse');
    } finally { setLoading(false); }
  };

  return (
    <div className="login-container">
      <div className="login-card" style={{ maxWidth: 480 }}>
        <div className="login-logo">🌱</div>
        <h1 className="login-title">Crear Cuenta</h1>
        <p className="login-subtitle">Únase a AgroSmart Magdalena</p>

        {error && <div className="alert alert-danger py-2 small">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label">Nombre completo</label>
            <input type="text" className="form-control" name="nombreCompleto" placeholder="Ej: Carlos Pérez"
              value={form.nombreCompleto} onChange={handleChange} required />
          </div>

          <div className="mb-3">
            <label className="form-label">Correo electrónico</label>
            <input type="email" className="form-control" name="email" placeholder="ejemplo@correo.com"
              value={form.email} onChange={handleChange} required />
          </div>

          <div className="row mb-3">
            <div className="col-6">
              <label className="form-label">Contraseña</label>
              <input type="password" className="form-control" name="password" placeholder="Mín. 6 caracteres"
                value={form.password} onChange={handleChange} required minLength={6} />
            </div>
            <div className="col-6">
              <label className="form-label">Teléfono</label>
              <input type="tel" className="form-control" name="telefono" placeholder="3001234567"
                value={form.telefono} onChange={handleChange} />
            </div>
          </div>

          <div className="row mb-3">
            <div className="col-6">
              <label className="form-label">Tipo de usuario</label>
              <select className="form-select" name="rol" value={form.rol} onChange={handleChange}>
                <option value="AGRICULTOR">Agricultor</option>
                <option value="TECNICO">Técnico</option>
                <option value="ASOCIACION">Asociación</option>
              </select>
            </div>
            <div className="col-6">
              <label className="form-label">Cédula *</label>
              <input type="text" className="form-control" name="cedula" placeholder="Ej: 1234567890"
                value={form.cedula} onChange={handleChange} required />
            </div>
          </div>

          <button type="submit" className="btn btn-agro w-100 mb-3" disabled={loading}>
            {loading ? <><span className="spinner-border spinner-border-sm me-2"></span>Registrando...</> : <><i className="bi bi-person-plus"></i>Registrarse</>}
          </button>
        </form>

        <div className="text-center">
          <Link to="/login" className="text-decoration-none small" style={{ color: 'var(--color-primary)' }}>
            ¿Ya tiene cuenta? <strong>Ingrese aquí</strong>
          </Link>
        </div>
      </div>
    </div>
  );
}
