import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { fincaService } from '../services/apiServices';
import { LoadingSpinner } from '../components/UIComponents';
import { useOnlineStatus, usePendingOps } from '../hooks/useAppHooks';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

export default function FincaFormPage() {
  const { id } = useParams();
  const isEdit = !!id;
  const navigate = useNavigate();
  const { isOnline } = useOnlineStatus();
  const { addOp } = usePendingOps();
  const { user } = useAuth();
  const { addToast } = useToast();

  const [form, setForm] = useState({
    nombre: '', areaTotal: '', descripcion: '',
    latitud: '', longitud: '', vereda: '', municipio: '', referenciaAdicional: '',
  });
  const [loading, setLoading] = useState(isEdit);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!isEdit) {
      setLoading(false);
      return;
    }

    fincaService
      .obtener(id)
      .then((res) => {
        const f = res.data?.datos;
        if (f) {
          setForm({
            nombre: f.nombre || '',
            areaTotal: f.areaTotal || '',
            descripcion: f.descripcion || '',
            latitud: f.latitud || '',
            longitud: f.longitud || '',
            vereda: f.vereda || '',
            municipio: f.municipio || '',
            referenciaAdicional: '',
          });
        }
      })
      .catch(() => setError('No se pudo cargar la finca'))
      .finally(() => setLoading(false));
  }, [id, isEdit]);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const obtenerUbicacionActual = () => {
    if (!navigator.geolocation) {
      setError('Tu navegador no soporta geolocalización');
      return;
    }

    setSaving(true);
    navigator.geolocation.getCurrentPosition(
      (position) => {
        setForm((prev) => ({
          ...prev,
          latitud: position.coords.latitude.toFixed(6),
          longitud: position.coords.longitude.toFixed(6),
        }));
        setSaving(false);
      },
      (err) => {
        console.error(err);
        setError('No se pudo obtener la ubicación. Asegúrate de dar permisos de GPS.');
        setSaving(false);
      },
      { enableHighAccuracy: true }
    );
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSaving(true);

    const area = parseFloat(form.areaTotal);
    const lat = form.latitud ? parseFloat(form.latitud) : null;
    const lng = form.longitud ? parseFloat(form.longitud) : null;

    if (area <= 0) {
      setError('El área total debe ser mayor a 0');
      setSaving(false);
      return;
    }
    if (lat !== null && (lat < -90 || lat > 90 || Number.isNaN(lat))) {
      setError('La latitud debe estar entre -90 y 90 grados');
      setSaving(false);
      return;
    }
    if (lng !== null && (lng < -180 || lng > 180 || Number.isNaN(lng))) {
      setError('La longitud debe estar entre -180 y 180 grados');
      setSaving(false);
      return;
    }

    const data = { ...form, areaTotal: area, latitud: lat, longitud: lng };

    if (!isOnline) {
      addOp({ entidad: 'FINCA', accion: isEdit ? 'UPDATE' : 'CREATE', datosJson: JSON.stringify(data) });
      navigate('/fincas');
      return;
    }

    try {
      if (isEdit) {
        await fincaService.actualizar(id, data);
      } else {
        if (user?.productorId == null) {
          setError('No se encontró tu perfil de productor. Intenta cerrar sesión y volver a entrar.');
          return;
        }
        await fincaService.crear(user.productorId, data);
      }
      navigate('/fincas');
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.mensaje || 'Error de la API al guardar la finca');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <LoadingSpinner />;

  return (
    <div className="content-wrapper">
      <div className="page-header">
        <h2><i className="bi bi-geo-alt me-2"></i>{isEdit ? 'Editar Finca' : 'Nueva Finca'}</h2>
      </div>

      <div className="card card-agro">
        <div className="card-body p-3 p-md-4">
          {error && <div className="alert alert-danger py-2 small">{error}</div>}
          {!isOnline && <div className="alert alert-warning py-2 small"><i className="bi bi-wifi-off me-1"></i>Sin conexión — Se guardará localmente</div>}

          <form onSubmit={handleSubmit}>
            <div className="mb-3">
              <label className="form-label">Nombre de la finca *</label>
              <input
                type="text"
                className="form-control"
                name="nombre"
                placeholder="Ej: Finca La Esperanza"
                value={form.nombre}
                onChange={handleChange}
                required
              />
            </div>

            <div className="row mb-3">
              <div className="col-6">
                <label className="form-label">Área total *</label>
                <div className="input-group">
                  <input
                    type="number"
                    step="0.1"
                    className="form-control"
                    name="areaTotal"
                    placeholder="Ej: 10"
                    value={form.areaTotal}
                    onChange={handleChange}
                    required
                    min="0.1"
                  />
                  <span className="input-group-text">ha</span>
                </div>
              </div>
              <div className="col-6">
                <label className="form-label">Municipio *</label>
                <select className="form-select" name="municipio" value={form.municipio} onChange={handleChange} required>
                  <option value="">Seleccione...</option>
                  <option value="Santa Marta">Santa Marta</option>
                  <option value="Ciénaga">Ciénaga</option>
                  <option value="Aracataca">Aracataca</option>
                  <option value="Zona Bananera">Zona Bananera</option>
                  <option value="Fundación">Fundación</option>
                  <option value="El Retén">El Retén</option>
                  <option value="Pivijay">Pivijay</option>
                  <option value="Plato">Plato</option>
                  <option value="El Banco">El Banco</option>
                  <option value="Otro">Otro</option>
                </select>
              </div>
            </div>

            <div className="d-flex justify-content-between align-items-center mb-3">
              <h5 className="mb-0 text-success"><i className="bi bi-geo-fill me-2"></i>Ubicación de la Finca</h5>
              <button type="button" className="btn btn-sm btn-outline-primary" onClick={obtenerUbicacionActual}>
                <i className="bi bi-crosshair me-1"></i>Capturar GPS
              </button>
            </div>
            <p className="text-muted small mb-3">Puedes capturar tu ubicación actual con el botón superior o ingresar las coordenadas manualmente.</p>
            <div className="row mb-3">
              <div className="col-6">
                <label className="form-label">Latitud</label>
                <input
                  type="number"
                  step="0.000001"
                  className="form-control"
                  name="latitud"
                  placeholder="Ej: 10.4195"
                  value={form.latitud}
                  onChange={handleChange}
                />
              </div>
              <div className="col-6">
                <label className="form-label">Longitud</label>
                <input
                  type="number"
                  step="0.000001"
                  className="form-control"
                  name="longitud"
                  placeholder="Ej: -74.1502"
                  value={form.longitud}
                  onChange={handleChange}
                />
              </div>
            </div>

            <div className="mb-3">
              <label className="form-label">Vereda</label>
              <input
                type="text"
                className="form-control"
                name="vereda"
                placeholder="Nombre de la vereda"
                value={form.vereda}
                onChange={handleChange}
              />
            </div>

            <div className="mb-3">
              <label className="form-label">Descripción</label>
              <textarea
                className="form-control"
                name="descripcion"
                rows="2"
                placeholder="Descripción breve de la finca"
                value={form.descripcion}
                onChange={handleChange}
              />
            </div>

            {!isEdit && (
              <p className="text-muted small mb-4">
                Después de guardar podrás dividir la finca en parcelas/lotes.
              </p>
            )}

            <div className="d-flex gap-2">
              <button type="submit" className="btn btn-agro flex-grow-1" disabled={saving}>
                {saving ? (
                  <>
                    <span className="spinner-border spinner-border-sm me-2"></span>
                    Guardando...
                  </>
                ) : (
                  <>
                    <i className="bi bi-check-circle"></i>
                    Guardar
                  </>
                )}
              </button>
              <button type="button" className="btn btn-secondary" onClick={() => navigate('/fincas')}>
                Cancelar
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
