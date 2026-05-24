import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { cultivoService, parcelaService, fincaService, supervisionService } from '../services/apiServices';
import { LoadingSpinner } from '../components/UIComponents';
import { useAuth } from '../context/AuthContext';
import { useOnlineStatus, usePendingOps } from '../hooks/useAppHooks';
import { useToast } from '../context/ToastContext';

export default function CultivoFormPage() {
  const { id } = useParams();
  const isEdit = !!id;
  const navigate = useNavigate();
  const { user, isTecnico } = useAuth();
  const { isOnline } = useOnlineStatus();
  const { addOp } = usePendingOps();
  const { addToast } = useToast();

  const [form, setForm] = useState({
    nombre: '',
    variedad: '',
    fechaSiembra: '',
    fechaCosechaEstimada: '',
    estado: 'PLANIFICADO',
    areaUtilizada: '',
    observaciones: '',
    rendimientoEsperado: '',
    parcelaId: '',
    imagenUrl: '',
  });

  const [fincas, setFincas] = useState([]);
  const [parcelas, setParcelas] = useState([]);
  const [selectedFinca, setSelectedFinca] = useState('');
  const [selectedParcela, setSelectedParcela] = useState(null);
  const [availableParcelaArea, setAvailableParcelaArea] = useState(null);
  const [loading, setLoading] = useState(true);
  const [loadingParcelas, setLoadingParcelas] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [areaError, setAreaError] = useState('');

  useEffect(() => {
    const init = async () => {
      const lista = await loadFincas();
      if (isEdit) await loadCultivo(lista);
      else setLoading(false);
    };

    init();
  }, [id]);

  const getParcelaArea = (parcela) => {
    return parcela?.areaHectareas ?? parcela?.areaParcela ?? parcela?.area ?? parcela?.superficie ?? 0;
  };

  const loadFincas = async () => {
  try {
    if (isTecnico()) {
      const res = await supervisionService.listarMisFincas();
      const lista = (res.data?.datos || []).map(s => ({
        id: s.fincaId,
        nombre: s.fincaNombre,
        municipio: s.municipio,
      }));
      setFincas(lista);
      return lista;
    }

    if (!user?.productorId) return [];
    const res = await fincaService.listarPorProductor(user.productorId, 0);
    const lista = res.data?.datos?.content || [];
    setFincas(lista);
    return lista;
  } catch (err) {
    console.error('Error loading fincas:', err);
    return [];
  }
};

  const loadParcelas = async (fincaId, currentParcelaId = '') => {
    if (!fincaId) {
      setParcelas([]);
      return [];
    }

    try {
      setLoadingParcelas(true);
      setError('');

      const parcelasRes = await parcelaService.listarPorFinca(fincaId, 0);
      const parcelasList = parcelasRes.data?.datos?.content || [];

      const parcelasFiltradas = parcelasList.filter((p) => {
        if (String(p.id) === String(currentParcelaId)) return true;
        return p.estado === 'DISPONIBLE';
      });

      setParcelas(parcelasFiltradas);

      return parcelasFiltradas;
    } catch (err) {
      console.error('Error loading parcelas:', err);
      setParcelas([]);
      setError('No se pudieron cargar las parcelas de esta finca');
      return [];
    } finally {
      setLoadingParcelas(false);
    }
  };

  const loadCultivo = async () => {
    try {
      const res = await cultivoService.obtener(id);
      const c = res.data?.datos;

      if (c) {
        const fincaId = c.fincaId || '';
        const parcelaId = c.parcelaId || '';

        setForm({
          nombre: c.nombre || '',
          variedad: c.variedad || '',
          parcelaId,
          fechaSiembra: c.fechaSiembra || '',
          fechaCosechaEstimada: c.fechaCosechaEstimada || '',
          estado: c.estado || 'PLANIFICADO',
          areaUtilizada: c.areaUtilizada || '',
          observaciones: c.observaciones || '',
          rendimientoEsperado: c.rendimientoEsperado || '',
          imagenUrl: c.imagenUrl || '',
        });

        setSelectedFinca(fincaId);

        if (fincaId) {
          const parcelasList = await loadParcelas(fincaId, parcelaId);
          const actual = parcelasList.find((p) => String(p.id) === String(parcelaId));
          setSelectedParcela(actual || null);
          setAvailableParcelaArea(actual ? getParcelaArea(actual) : null);
        }
      }
    } catch (err) {
      console.error('Error loading cultivo:', err);
      setError('No se pudo cargar el cultivo');
    } finally {
      setLoading(false);
    }
  };

  const handleFincaChange = async (e) => {
    const fId = e.target.value;

    setSelectedFinca(fId);
    setForm((prev) => ({
      ...prev,
      parcelaId: '',
    }));
    setSelectedParcela(null);
    setAvailableParcelaArea(null);
    setAreaError('');

    if (fId) {
      await loadParcelas(fId);
    } else {
      setParcelas([]);
    }
  };

  const handleParcelaChange = (e) => {
    const parcelaId = e.target.value;
    const parcela = parcelas.find((p) => String(p.id) === String(parcelaId));

    setForm((prev) => ({
      ...prev,
      parcelaId,
    }));

    setSelectedParcela(parcela || null);
    setAvailableParcelaArea(parcela ? getParcelaArea(parcela) : null);
    setAreaError('');
  };

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    setError('');
    setAreaError('');
    setSaving(true);

    const parsedArea = form.areaUtilizada ? parseFloat(form.areaUtilizada) : null;
    const parsedRendimiento = form.rendimientoEsperado ? parseFloat(form.rendimientoEsperado) : null;

    if (!selectedFinca) {
      setError('La finca es obligatoria');
      setSaving(false);
      return;
    }

    if (!form.parcelaId) {
      setError('La parcela/lote es obligatoria para crear un cultivo');
      setSaving(false);
      return;
    }

    if (!parsedArea || parsedArea <= 0) {
      setError('El área utilizada debe ser un número mayor a 0');
      setSaving(false);
      return;
    }

    if (selectedParcela && Math.abs(parsedArea - getParcelaArea(selectedParcela)) > 0.0001) {
      setAreaError(`El área del cultivo debe ser igual al área de la parcela (${getParcelaArea(selectedParcela)} ha). Ingresa exactamente ${getParcelaArea(selectedParcela)} ha`);
      setError(`El área del cultivo debe ser igual al área de la parcela (${getParcelaArea(selectedParcela)} ha). Ingresa exactamente ${getParcelaArea(selectedParcela)} ha`);
      setSaving(false);
      return;
    }

    if (parsedRendimiento && parsedRendimiento < 0) {
      setError('El rendimiento esperado no puede ser negativo');
      setSaving(false);
      return;
    }

    if (
      form.fechaSiembra &&
      form.fechaCosechaEstimada &&
      new Date(form.fechaSiembra) > new Date(form.fechaCosechaEstimada)
    ) {
      setError('La fecha de siembra no puede ser posterior a la fecha de cosecha estimada');
      setSaving(false);
      return;
    }

    try {
      const data = {
        ...form,
        fincaId: selectedFinca,
        fincaNombre: fincas.find((f) => String(f.id) === String(selectedFinca))?.nombre,
        parcelaId: form.parcelaId,
        parcelaNombre: selectedParcela?.nombre,
        areaUtilizada: parsedArea,
        rendimientoEsperado: parsedRendimiento,
      };

      if (!isOnline) {
        addOp({ entidad: 'CULTIVO', accion: isEdit ? 'UPDATE' : 'CREATE', data: isEdit ? { ...data, id } : data });
        addToast(`Cultivo ${isEdit ? 'actualizado' : 'creado'} (pendiente de sincronizar)`, 'warning');
        navigate('/cultivos');
        return;
      }

      if (isEdit) await cultivoService.actualizar(id, data);
      else await cultivoService.crear(data);

      navigate('/cultivos');
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.mensaje || 'Error al guardar el cultivo. Verifique los datos.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <LoadingSpinner />;

  return (
    <div className="content-wrapper">
      <div className="page-header">
        <h2>
          <i className="bi bi-flower1 me-2"></i>
          {isEdit ? 'Editar Cultivo' : 'Nuevo Cultivo'}
        </h2>
      </div>

      <div className="card card-agro">
        <div className="card-body p-3 p-md-4">
          {error && <div className="alert alert-danger py-2 small">{error}</div>}
          {!isOnline && <div className="alert alert-warning py-2 small"><i className="bi bi-wifi-off me-1"></i>Sin conexión — Se guardará localmente</div>}

          <form onSubmit={handleSubmit}>
            <div className="row mb-3">
              <div className="col-6">
                <label className="form-label">Nombre del cultivo *</label>
                <input
                  type="text"
                  className="form-control"
                  name="nombre"
                  placeholder="Ej: Banano, Cacao"
                  value={form.nombre}
                  onChange={handleChange}
                  required
                />
              </div>

              <div className="col-6">
                <label className="form-label">Variedad</label>
                <input
                  type="text"
                  className="form-control"
                  name="variedad"
                  placeholder="Ej: Gran Enano"
                  value={form.variedad}
                  onChange={handleChange}
                />
              </div>
            </div>

            <div className="row mb-3">
              <div className="col-4">
                <label className="form-label">Finca *</label>
                <select
                  className="form-select"
                  value={selectedFinca}
                  onChange={handleFincaChange}
                  required
                  disabled={isEdit}
                >
                  <option value="">Seleccione finca...</option>
                  {fincas.map((f) => (
                    <option key={f.id} value={f.id}>
                      {f.nombre}
                    </option>
                  ))}
                </select>
              </div>

              <div className="col-4">
                <label className="form-label">Parcela/Lote *</label>
                <select
                  className="form-select"
                  name="parcelaId"
                  value={form.parcelaId}
                  onChange={handleParcelaChange}
                  required
                  disabled={!selectedFinca || loadingParcelas || isEdit}
                >
                  <option value="">
                    {loadingParcelas ? 'Cargando parcelas...' : 'Seleccione parcela...'}
                  </option>

                  {parcelas.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.nombre} - {getParcelaArea(p)} ha - {p.estado}
                    </option>
                  ))}
                </select>

                {selectedFinca && !loadingParcelas && parcelas.length === 0 && (
                  <small className="text-danger">
                    No hay parcelas disponibles para esta finca. Cree una parcela o libere una existente.
                  </small>
                )}
              </div>

              <div className="col-4">
                <label className="form-label">
                  Hectáreas de cultivo *
                  {availableParcelaArea > 0 && !isEdit && (
                    <small className="text-info d-block">
                      El área debe ser exactamente: {Number(availableParcelaArea).toFixed(2)} ha
                    </small>
                  )}
                </label>
                <input
                  type="number"
                  step="0.1"
                  className="form-control"
                  name="areaUtilizada"
                  value={form.areaUtilizada}
                  onChange={handleChange}
                  required
                  disabled={isEdit}
                  min="0.1"
                />
                {areaError && <small className="text-danger">{areaError}</small>}
              </div>
            </div>

            <div className="row mb-3">
              <div className="col-6">
                <label className="form-label">Fecha de siembra *</label>
                <input
                  type="date"
                  className="form-control"
                  name="fechaSiembra"
                  value={form.fechaSiembra}
                  onChange={handleChange}
                  required
                />
              </div>

              <div className="col-6">
                <label className="form-label">Cosecha estimada</label>
                <input
                  type="date"
                  className="form-control"
                  name="fechaCosechaEstimada"
                  value={form.fechaCosechaEstimada}
                  onChange={handleChange}
                />
              </div>
            </div>

            <div className="row mb-3">
              <div className="col-6">
                <label className="form-label">Estado</label>
                <select className="form-select" name="estado" value={form.estado} onChange={handleChange}>
                  <option value="PLANIFICADO">Planificado</option>
                  <option value="SEMBRADO">Sembrado</option>
                  <option value="EN_CRECIMIENTO">En crecimiento</option>
                  <option value="EN_COSECHA">En cosecha</option>
                  <option value="COSECHADO">Cosechado</option>
                </select>
              </div>

              <div className="col-6">
                <label className="form-label">Rend. esperado</label>
                <input
                  type="number"
                  step="0.1"
                  className="form-control"
                  name="rendimientoEsperado"
                  placeholder="ton/ha"
                  value={form.rendimientoEsperado}
                  onChange={handleChange}
                />
              </div>
            </div>

            <div className="mb-4">
              <label className="form-label">URL de la Imagen del Cultivo (Opcional)</label>
              <div className="input-group">
                <span className="input-group-text">
                  <i className="bi bi-camera"></i>
                </span>
                <input
                  type="text"
                  className="form-control"
                  name="imagenUrl"
                  placeholder="https://ejemplo.com/foto.jpg"
                  value={form.imagenUrl}
                  onChange={handleChange}
                />
              </div>
              <small className="text-muted">
                Puedes pegar el enlace de una foto de tu cultivo para tener un registro visual.
              </small>
            </div>

            <div className="mb-4">
              <label className="form-label">Observaciones</label>
              <textarea
                className="form-control"
                name="observaciones"
                rows="2"
                value={form.observaciones}
                onChange={handleChange}
              />
            </div>

            <div className="d-flex gap-2">
              <button type="submit" className="btn btn-agro flex-grow-1" disabled={saving}>
                {saving ? (
                  'Guardando...'
                ) : (
                  <>
                    <i className="bi bi-check-circle"></i> Guardar
                  </>
                )}
              </button>

              <button type="button" className="btn btn-secondary" onClick={() => navigate('/cultivos')}>
                Cancelar
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
