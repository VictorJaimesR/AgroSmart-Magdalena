import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { cultivoService, supervisionService, recomendacionService } from '../services/apiServices';
import { useToast } from '../context/ToastContext';
import { useAuth } from '../context/AuthContext';

const PRIORIDADES = [
  { value: 'BAJA', label: 'Baja', icon: '🟢', cls: 'btn-outline-success' },
  { value: 'MEDIA', label: 'Media', icon: '🔵', cls: 'btn-outline-primary' },
  { value: 'ALTA', label: 'Alta', icon: '🟡', cls: 'btn-outline-warning' },
  { value: 'CRITICA', label: 'Crítica', icon: '🔴', cls: 'btn-outline-danger' },
];

const TITULOS_SUGERIDOS = [
  'Aplicar riego de emergencia',
  'Control preventivo de plagas',
  'Fertilización nitrogenada requerida',
  'Revisión fitosanitaria urgente',
  'Poda de mantenimiento',
  'Análisis de suelo recomendado',
  'Preparación para cosecha',
  'Control de malezas',
];

export default function RecomendacionFormPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { addToast } = useToast();
  const { user } = useAuth();

  const preCultivoId = searchParams.get('cultivoId');

  const [fincas, setFincas] = useState([]);
  const [cultivos, setCultivos] = useState([]);
  const [loadingFincas, setLoadingFincas] = useState(true);
  const [loadingCultivos, setLoadingCultivos] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const [fincaId, setFincaId] = useState('');
  const [cultivoId, setCultivoId] = useState(preCultivoId || '');
  const [titulo, setTitulo] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [prioridad, setPrioridad] = useState('MEDIA');

  // Cargar fincas supervisadas por el técnico
  useEffect(() => {
    supervisionService.listarMisFincas()
      .then(res => {
        const lista = res.data?.datos || res.data || [];
        setFincas(lista.map(s => ({ id: s.fincaId, nombre: s.fincaNombre })));
      })
      .catch(() => addToast('Error cargando fincas', 'error'))
      .finally(() => setLoadingFincas(false));
  }, []);

  // Cargar cultivos al cambiar finca
  useEffect(() => {
    if (!fincaId) { setCultivos([]); setCultivoId(''); return; }
    setLoadingCultivos(true);
    cultivoService.listarPorFinca(fincaId, 0)
      .then(res => {
        const page = res.data?.datos || res.data;
        setCultivos(page?.content || page || []);
      })
      .catch(() => addToast('Error cargando cultivos', 'error'))
      .finally(() => setLoadingCultivos(false));
  }, [fincaId]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!cultivoId || !titulo || !descripcion) {
      addToast('Cultivo, título y descripción son obligatorios', 'warning');
      return;
    }
    setSubmitting(true);
    try {
      await recomendacionService.crear({
        cultivoId: Number(cultivoId),
        tecnicoId: user?.tecnicoId || null,
        titulo,
        descripcion,
        prioridad,
      });
      addToast('✅ Recomendación creada exitosamente', 'success');
      navigate('/recomendaciones');
    } catch (err) {
      addToast(err.response?.data?.mensaje || 'Error al crear recomendación', 'error');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="content-wrapper">
      <div className="page-header d-flex justify-content-between align-items-start flex-wrap gap-2">
        <div>
          <h2><i className="bi bi-lightbulb me-2"></i>Nueva recomendación</h2>
          <p className="text-muted mb-0">Crea una recomendación técnica para un cultivo</p>
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
          ) : fincas.length === 0 ? (
            <div className="alert alert-warning">
              <i className="bi bi-exclamation-triangle me-2"></i>
              No supervisas ninguna finca actualmente. Toma una finca desde{' '}
              <a href="/tecnico/fincas-disponibles" className="alert-link">Fincas disponibles</a>.
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
                    className="form-select"
                    value={fincaId}
                    onChange={e => { setFincaId(e.target.value); setCultivoId(''); }}
                    required
                  >
                    <option value="">— Selecciona una finca —</option>
                    {fincas.map(f => (
                      <option key={f.id} value={f.id}>{f.nombre}</option>
                    ))}
                  </select>
                </div>

                {/* Cultivo */}
                <div className="col-12 col-md-6">
                  <label className="form-label fw-semibold">
                    <i className="bi bi-flower1 me-1 text-success"></i>Cultivo *
                  </label>
                  {loadingCultivos ? (
                    <div className="form-control text-muted d-flex align-items-center gap-2">
                      <span className="spinner-border spinner-border-sm"></span> Cargando...
                    </div>
                  ) : (
                    <select
                      className="form-select"
                      value={cultivoId}
                      onChange={e => setCultivoId(e.target.value)}
                      disabled={!fincaId}
                      required
                    >
                      <option value="">— Selecciona un cultivo —</option>
                      {cultivos.map(c => (
                        <option key={c.id} value={c.id}>
                          {c.nombre}{c.variedad ? ` (${c.variedad})` : ''}
                        </option>
                      ))}
                    </select>
                  )}
                  {fincaId && !loadingCultivos && cultivos.length === 0 && (
                    <div className="text-muted small mt-1">Esta finca no tiene cultivos activos.</div>
                  )}
                </div>

                {/* Prioridad */}
                <div className="col-12">
                  <label className="form-label fw-semibold">
                    <i className="bi bi-flag me-1 text-success"></i>Urgencia *
                  </label>
                  <div className="d-flex gap-2 flex-wrap">
                    {PRIORIDADES.map(p => (
                      <div key={p.value}>
                        <input
                          type="radio"
                          className="btn-check"
                          name="prioridad"
                          id={`prio-${p.value}`}
                          value={p.value}
                          checked={prioridad === p.value}
                          onChange={() => setPrioridad(p.value)}
                        />
                        <label
                          className={`btn ${prioridad === p.value ? p.cls.replace('outline-', '') : p.cls}`}
                          htmlFor={`prio-${p.value}`}
                        >
                          {p.icon} {p.label}
                        </label>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Título */}
                <div className="col-12">
                  <label className="form-label fw-semibold">
                    <i className="bi bi-card-heading me-1 text-success"></i>Título *
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    placeholder="Ej: Aplicar riego de emergencia"
                    maxLength={200}
                    value={titulo}
                    onChange={e => setTitulo(e.target.value)}
                    required
                  />
                  {/* Sugerencias rápidas */}
                  <div className="mt-2 d-flex flex-wrap gap-1">
                    {TITULOS_SUGERIDOS.map(s => (
                      <button
                        key={s}
                        type="button"
                        className="btn btn-sm btn-outline-secondary"
                        style={{ fontSize: '0.72rem' }}
                        onClick={() => setTitulo(s)}
                      >
                        {s}
                      </button>
                    ))}
                  </div>
                </div>

                {/* Descripción */}
                <div className="col-12">
                  <label className="form-label fw-semibold">
                    <i className="bi bi-chat-text me-1 text-success"></i>Descripción / Instrucciones *
                  </label>
                  <textarea
                    className="form-control"
                    rows={5}
                    placeholder="Describe detalladamente qué debe hacer el agricultor, con qué producto, en qué cantidad y cuándo..."
                    value={descripcion}
                    onChange={e => setDescripcion(e.target.value)}
                    required
                  />
                  <div className="text-muted small text-end mt-1">{descripcion.length} caracteres</div>
                </div>

                {/* Botones */}
                <div className="col-12 d-flex gap-2 justify-content-end pt-2">
                  <button type="button" className="btn btn-agro-outline" onClick={() => navigate(-1)}>
                    Cancelar
                  </button>
                  <button type="submit" className="btn btn-agro" disabled={submitting}>
                    {submitting
                      ? <><span className="spinner-border spinner-border-sm me-2"></span>Guardando...</>
                      : <><i className="bi bi-send me-1"></i>Enviar recomendación</>
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
