import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { fincaService, cultivoService, actividadService, supervisionService } from '../services/apiServices';
import { useToast } from '../context/ToastContext';
import { useAuth } from '../context/AuthContext';

const TIPOS_ACTIVIDAD = [
  { value: 'RIEGO', label: 'Riego', icon: '💧', unidades: ['litros', 'm³', 'horas'], usaProducto: false },
  { value: 'FERTILIZACION', label: 'Fertilización', icon: '🌿', unidades: ['kg', 'litros', 'g'], usaProducto: true },
  { value: 'CONTROL_PLAGAS', label: 'Control de plagas', icon: '🐛', unidades: ['ml', 'litros', 'g'], usaProducto: true },
  { value: 'CONTROL_ENFERMEDADES', label: 'Control de enfermedades', icon: '🍄', unidades: ['ml', 'litros', 'g'], usaProducto: true },
  { value: 'PODA', label: 'Poda', icon: '✂️', unidades: ['plantas', '%'], usaProducto: false },
  { value: 'TRASPLANTE', label: 'Trasplante', icon: '🌱', unidades: ['plantas', 'unidades'], usaProducto: false },
  { value: 'ANALISIS_SUELO', label: 'Análisis de suelo', icon: '🧪', unidades: ['muestras', 'm²', 'ha'], usaProducto: false },
  { value: 'COSECHA_PARCIAL', label: 'Cosecha parcial', icon: '🌾', unidades: ['kg', 'toneladas', 'lb'], usaProducto: false },
  { value: 'COSECHA_TOTAL', label: 'Cosecha total', icon: '🌾', unidades: ['kg', 'toneladas', 'lb'], usaProducto: false },
  { value: 'LIMPIEZA', label: 'Limpieza / Deshierbe', icon: '🧹', unidades: ['horas', 'm²', 'ha'], usaProducto: false },
];

function toLocalDateTimeInput(date) {
  const d = date || new Date();
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

export default function ActividadFormPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { addToast } = useToast();
  const { user, isProductor, isTecnico } = useAuth();

  // Pre-selección desde query params (?fincaId=X&cultivoId=Y)
  const params = new URLSearchParams(location.search);
  const preFincaId = params.get('fincaId') ? Number(params.get('fincaId')) : null;
  const preCultivoId = params.get('cultivoId') ? Number(params.get('cultivoId')) : null;

  const [fincas, setFincas] = useState([]);
  const [cultivos, setCultivos] = useState([]);
  const [loadingFincas, setLoadingFincas] = useState(true);
  const [loadingCultivos, setLoadingCultivos] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const [form, setForm] = useState({
    fincaId: preFincaId || '',
    cultivoId: preCultivoId || '',
    tipoActividad: '',
    cantidad: '',
    unidad: '',
    producto: '',
    observaciones: '',
    fechaActividad: toLocalDateTimeInput(new Date()),
  });

  const tipoSeleccionado = TIPOS_ACTIVIDAD.find(t => t.value === form.tipoActividad);

  /* ── Cargar fincas según rol ── */
  useEffect(() => {
    const load = async () => {
      setLoadingFincas(true);
      try {
        if (isProductor()) {
          const productorId = user?.productorId;
          if (!productorId) { setFincas([]); return; }
          const res = await fincaService.listarPorProductor(productorId, 0);
          setFincas(res.data?.datos?.content || []);
        } else if (isTecnico()) {
          const res = await supervisionService.listarMisFincas();
          // mis-fincas devuelve lista con fincaId y fincaNombre
          const lista = res.data?.datos || [];
          setFincas(lista.map(s => ({ id: s.fincaId, nombre: s.fincaNombre })));
        }
      } catch (err) {
        console.error(err);
        addToast('Error cargando fincas', 'error');
      } finally {
        setLoadingFincas(false);
      }
    };
    load();
  }, []);

  /* ── Cargar cultivos cuando cambia la finca ── */
  useEffect(() => {
    if (!form.fincaId) { setCultivos([]); return; }
    const load = async () => {
      setLoadingCultivos(true);
      try {
        const res = await cultivoService.listarPorFinca(form.fincaId, 0);
        setCultivos(res.data?.datos?.content || []);
      } catch (err) {
        console.error(err);
        addToast('Error cargando cultivos de la finca', 'error');
        setCultivos([]);
      } finally {
        setLoadingCultivos(false);
      }
    };
    load();
  }, [form.fincaId]);

  /* ── Cuando cambia el tipo de actividad, resetear unidad ── */
  useEffect(() => {
    if (tipoSeleccionado) {
      setForm(f => ({ ...f, unidad: tipoSeleccionado.unidades[0] }));
    }
  }, [form.tipoActividad]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm(f => {
      const next = { ...f, [name]: value };
      // Si cambia finca, resetear cultivo
      if (name === 'fincaId') next.cultivoId = '';
      return next;
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.fincaId || !form.cultivoId || !form.tipoActividad || !form.fechaActividad) {
      addToast('Finca, cultivo, tipo de actividad y fecha son obligatorios', 'warning');
      return;
    }
    setSubmitting(true);
    try {
      await actividadService.registrar({
        fincaId: Number(form.fincaId),
        cultivoId: Number(form.cultivoId),
        tipoActividad: form.tipoActividad,
        cantidad: form.cantidad ? Number(form.cantidad) : null,
        unidad: form.unidad || null,
        producto: form.producto || null,
        observaciones: form.observaciones || null,
        fechaActividad: form.fechaActividad,  // ISO string
      });
      addToast('✅ Actividad registrada exitosamente', 'success');
      navigate(`/actividades/finca/${form.fincaId}`);
    } catch (err) {
      console.error(err);
      addToast(err.response?.data?.mensaje || 'Error al registrar la actividad', 'error');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="content-wrapper">
      <div className="page-header">
        <div>
          <h2><i className="bi bi-journal-plus me-2"></i>Registrar Actividad</h2>
          <p className="text-muted mb-0">Registra una labor realizada en un cultivo</p>
        </div>
        <button className="btn btn-agro-outline" onClick={() => navigate(-1)}>
          <i className="bi bi-arrow-left me-1"></i>Volver
        </button>
      </div>

      <div className="card card-agro">
        <div className="card-body p-3 p-md-4">
          {loadingFincas ? (
            <div className="text-center py-4">
              <span className="spinner-border spinner-border-sm me-2"></span>Cargando fincas...
            </div>
          ) : (
            <form onSubmit={handleSubmit}>
              <div className="row g-3">

                {/* Finca */}
                <div className="col-12 col-md-6">
                  <label className="form-label fw-semibold">
                    <i className="bi bi-geo-alt me-1 text-success"></i>Finca *
                  </label>
                  <select
                    id="fincaId"
                    name="fincaId"
                    className="form-select"
                    value={form.fincaId}
                    onChange={handleChange}
                    required
                  >
                    <option value="">— Selecciona una finca —</option>
                    {fincas.map(f => (
                      <option key={f.id} value={f.id}>{f.nombre}</option>
                    ))}
                  </select>
                  {fincas.length === 0 && !loadingFincas && (
                    <div className="text-muted small mt-1">
                      {isTecnico() ? 'No supervisas ninguna finca actualmente.' : 'No tienes fincas registradas.'}
                    </div>
                  )}
                </div>

                {/* Cultivo */}
                <div className="col-12 col-md-6">
                  <label className="form-label fw-semibold">
                    <i className="bi bi-flower1 me-1 text-success"></i>Cultivo *
                  </label>
                  {loadingCultivos ? (
                    <div className="form-control d-flex align-items-center gap-2 text-muted">
                      <span className="spinner-border spinner-border-sm"></span> Cargando cultivos...
                    </div>
                  ) : (
                    <select
                      id="cultivoId"
                      name="cultivoId"
                      className="form-select"
                      value={form.cultivoId}
                      onChange={handleChange}
                      required
                      disabled={!form.fincaId}
                    >
                      <option value="">— Selecciona un cultivo —</option>
                      {cultivos.map(c => (
                        <option key={c.id} value={c.id}>
                          {c.nombre}{c.variedad ? ` (${c.variedad})` : ''} — {c.parcelaNombre}
                        </option>
                      ))}
                    </select>
                  )}
                  {form.fincaId && !loadingCultivos && cultivos.length === 0 && (
                    <div className="text-muted small mt-1">Esta finca no tiene cultivos activos.</div>
                  )}
                </div>

                {/* Tipo de actividad */}
                <div className="col-12">
                  <label className="form-label fw-semibold">
                    <i className="bi bi-list-check me-1 text-success"></i>Tipo de actividad *
                  </label>
                  <div className="row g-2">
                    {TIPOS_ACTIVIDAD.map(t => (
                      <div key={t.value} className="col-6 col-sm-4 col-lg-3">
                        <input
                          type="radio"
                          className="btn-check"
                          name="tipoActividad"
                          id={`tipo-${t.value}`}
                          value={t.value}
                          checked={form.tipoActividad === t.value}
                          onChange={handleChange}
                        />
                        <label
                          className="btn btn-outline-secondary w-100 text-start d-flex align-items-center gap-2 py-2"
                          htmlFor={`tipo-${t.value}`}
                          style={{ fontSize: '0.85rem' }}
                        >
                          <span style={{ fontSize: '1.2rem' }}>{t.icon}</span>
                          {t.label}
                        </label>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Cantidad + Unidad */}
                {tipoSeleccionado && (
                  <>
                    <div className="col-12 col-md-4">
                      <label className="form-label fw-semibold">
                        <i className="bi bi-123 me-1 text-success"></i>Cantidad
                      </label>
                      <input
                        id="cantidad"
                        type="number"
                        name="cantidad"
                        className="form-control"
                        placeholder="Ej: 50"
                        min="0"
                        step="0.01"
                        value={form.cantidad}
                        onChange={handleChange}
                      />
                    </div>

                    <div className="col-12 col-md-4">
                      <label className="form-label fw-semibold">
                        <i className="bi bi-rulers me-1 text-success"></i>Unidad
                      </label>
                      <select
                        id="unidad"
                        name="unidad"
                        className="form-select"
                        value={form.unidad}
                        onChange={handleChange}
                      >
                        {tipoSeleccionado.unidades.map(u => (
                          <option key={u} value={u}>{u}</option>
                        ))}
                      </select>
                    </div>

                    {/* Producto — solo si el tipo lo usa */}
                    {tipoSeleccionado.usaProducto && (
                      <div className="col-12 col-md-4">
                        <label className="form-label fw-semibold">
                          <i className="bi bi-bag me-1 text-success"></i>Producto / Insumo
                        </label>
                        <input
                          id="producto"
                          type="text"
                          name="producto"
                          className="form-control"
                          placeholder="Ej: Urea, Glifosato…"
                          maxLength={150}
                          value={form.producto}
                          onChange={handleChange}
                        />
                      </div>
                    )}
                  </>
                )}

                {/* Fecha de actividad */}
                <div className="col-12 col-md-6">
                  <label className="form-label fw-semibold">
                    <i className="bi bi-calendar-event me-1 text-success"></i>Fecha y hora de la actividad *
                  </label>
                  <input
                    id="fechaActividad"
                    type="datetime-local"
                    name="fechaActividad"
                    className="form-control"
                    value={form.fechaActividad}
                    onChange={handleChange}
                    required
                  />
                </div>

                {/* Observaciones */}
                <div className="col-12">
                  <label className="form-label fw-semibold">
                    <i className="bi bi-chat-text me-1 text-success"></i>Observaciones
                  </label>
                  <textarea
                    id="observaciones"
                    name="observaciones"
                    className="form-control"
                    rows={3}
                    placeholder="Notas adicionales sobre la actividad realizada…"
                    maxLength={1000}
                    value={form.observaciones}
                    onChange={handleChange}
                  />
                  <div className="text-muted small text-end mt-1">{form.observaciones.length}/1000</div>
                </div>

                {/* Botones */}
                <div className="col-12 d-flex gap-2 justify-content-end pt-2">
                  <button type="button" className="btn btn-agro-outline" onClick={() => navigate(-1)}>
                    Cancelar
                  </button>
                  <button type="submit" className="btn btn-agro" disabled={submitting}>
                    {submitting
                      ? <><span className="spinner-border spinner-border-sm me-2"></span>Guardando…</>
                      : <><i className="bi bi-check-circle me-1"></i>Registrar actividad</>
                    }
                  </button>
                </div>

              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}
