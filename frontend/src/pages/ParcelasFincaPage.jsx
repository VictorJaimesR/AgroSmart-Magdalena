import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { fincaService, parcelaService } from '../services/apiServices';
import { LoadingSpinner } from '../components/UIComponents';
import { useOnlineStatus, usePendingOps } from '../hooks/useAppHooks';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

const emptyParcelaForm = {
  nombre: '',
  areaParcela: '',
  tipoSuelo: '',
  descripcion: '',
};

export default function ParcelasFincaPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isOnline } = useOnlineStatus();
  const { addOp } = usePendingOps();
  const { user } = useAuth();
  const { addToast } = useToast();

  const [finca, setFinca] = useState(null);
  const [loadingFinca, setLoadingFinca] = useState(true);

  const [parcelas, setParcelas] = useState([]);
  const [loadingParcelas, setLoadingParcelas] = useState(false);
  const [showParcelaForm, setShowParcelaForm] = useState(false);
  const [newParcela, setNewParcela] = useState(emptyParcelaForm);
  const [editingParcela, setEditingParcela] = useState(null);
  const [parcelaError, setParcelaError] = useState('');

  // Cargar datos de finca
  useEffect(() => {
    if (!id) {
      setLoadingFinca(false);
      return;
    }

    fincaService
      .obtener(id)
      .then((res) => {
        const f = res.data?.datos;
        if (f) {
          setFinca(f);
          loadParcelas(id);
        }
      })
      .catch((err) => {
        console.error('Error cargando finca:', err);
        addToast('No se pudo cargar la finca', 'error');
        navigate('/fincas');
      })
      .finally(() => setLoadingFinca(false));
  }, [id]);

  const loadParcelas = async (fincaId) => {
    try {
      setLoadingParcelas(true);
      const res = await parcelaService.listarPorFinca(fincaId, 0);
      const lista = res.data?.datos?.content || [];
      setParcelas(lista);
      setParcelaError('');
    } catch (err) {
      console.error('Error cargando parcelas:', err);
      setParcelaError('No se pudieron cargar las parcelas');
      setParcelas([]);
    } finally {
      setLoadingParcelas(false);
    }
  };

  const resetParcelaForm = () => {
    setNewParcela(emptyParcelaForm);
    setEditingParcela(null);
    setParcelaError('');
  };

  const calculateAreaUtilizada = () => {
    return parcelas.reduce((sum, p) => sum + (parseFloat(p.areaParcela) || 0), 0);
  };

  const getAreaDisponible = () => {
    const total = parseFloat(finca?.areaTotal) || 0;
    const utilizada = calculateAreaUtilizada();
    return Math.max(0, total - utilizada);
  };

  const handleParcelaChange = (e) => {
    setNewParcela((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const validateParcela = (parcela) => {
    setParcelaError('');

    if (!parcela.nombre || parcela.nombre.trim().length === 0) {
      setParcelaError('El nombre de la parcela es requerido');
      return false;
    }

    const area = parseFloat(parcela.areaParcela);
    if (!area || area <= 0) {
      setParcelaError('El área debe ser mayor a 0');
      return false;
    }

    if (editingParcela) {
      const areaAnterior = parseFloat(editingParcela.areaParcela) || 0;
      const diferenciaArea = area - areaAnterior;
      if (diferenciaArea > 0 && diferenciaArea > getAreaDisponible()) {
        setParcelaError(`El área disponible es insuficiente. Disponible: ${getAreaDisponible().toFixed(2)} ha`);
        return false;
      }
    } else if (area > getAreaDisponible()) {
      setParcelaError(`El área no puede superar el área disponible (${getAreaDisponible().toFixed(2)} ha)`);
      return false;
    }

    return true;
  };

  const handleSubmitParcela = async (e) => {
    e.preventDefault();
    if (!validateParcela(newParcela) || !id) return;

    const payload = {
      nombre: newParcela.nombre.trim(),
      areaParcela: parseFloat(newParcela.areaParcela),
      tipoSuelo: newParcela.tipoSuelo || null,
      descripcion: newParcela.descripcion || null,
      fincaId: parseInt(id, 10),
      unidadArea: 'hectáreas',
    };

    try {
      if (!isOnline) {
        addOp({
          entidad: 'PARCELA',
          accion: editingParcela ? 'UPDATE' : 'CREATE',
          datosJson: JSON.stringify(editingParcela ? { ...payload, id: editingParcela.id } : payload),
        });

        if (editingParcela) {
          setParcelas((prev) => prev.map((p) => (p.id === editingParcela.id ? { ...p, ...payload } : p)));
          addToast('Parcela actualizada (pendiente de sincronizar)', 'warning');
        } else {
          setParcelas((prev) => [
            ...prev,
            {
              ...payload,
              id: `pending-${Date.now()}`,
              estado: 'DISPONIBLE',
            },
          ]);
          addToast('Parcela creada (pendiente de sincronizar)', 'warning');
        }
      } else if (editingParcela) {
        await parcelaService.actualizar(editingParcela.id, payload);
        await loadParcelas(id);
        addToast('Parcela actualizada', 'success');
      } else {
        await parcelaService.crear(payload);
        await loadParcelas(id);
        addToast('Parcela creada', 'success');
      }

      resetParcelaForm();
    } catch (err) {
      console.error('Error al guardar parcela:', err);
      setParcelaError(err.response?.data?.mensaje || 'Error al guardar la parcela');
    }
  };

  const handleDeleteParcela = async (parcela) => {
    if (parcela.estado === 'OCUPADA') return;
    if (!window.confirm('¿Eliminar esta parcela?')) return;

    try {
      if (!isOnline) {
        addOp({ entidad: 'PARCELA', accion: 'DELETE', datosJson: JSON.stringify({ id: parcela.id, fincaId: id }) });
        setParcelas((prev) => prev.filter((p) => p.id !== parcela.id));
        addToast('Parcela eliminada (pendiente de sincronizar)', 'warning');
      } else {
        await parcelaService.eliminar(parcela.id);
        await loadParcelas(id);
        addToast('Parcela eliminada', 'success');
      }

      if (editingParcela && editingParcela.id === parcela.id) {
        resetParcelaForm();
      }
    } catch (err) {
      console.error('Error al eliminar parcela:', err);
      addToast(err.response?.data?.mensaje || 'Error al eliminar parcela', 'error');
    }
  };

  const handleStartEditParcela = (parcela) => {
    if (parcela.estado !== 'DISPONIBLE') return;
    setEditingParcela(parcela);
    setParcelaError('');
    setNewParcela({
      nombre: parcela.nombre || '',
      areaParcela: parcela.areaParcela || '',
      tipoSuelo: parcela.tipoSuelo || '',
      descripcion: parcela.descripcion || '',
    });
  };

  const handleCancelEditParcela = () => {
    resetParcelaForm();
  };

  if (loadingFinca) return <LoadingSpinner />;

  if (!finca) {
    return (
      <div className="content-wrapper">
        <div className="alert alert-danger">Finca no encontrada</div>
      </div>
    );
  }

  return (
    <div className="content-wrapper">
      <div className="page-header d-flex justify-content-between align-items-center">
        <div>
          <h2><i className="bi bi-grid-3x3-gap me-2"></i>Parcelas / Lotes</h2>
          <p className="text-muted mb-0"><strong>{finca.nombre}</strong> • {(parseFloat(finca.areaTotal) || 0).toFixed(2)} ha</p>
        </div>
        <button type="button" className="btn btn-secondary" onClick={() => navigate('/fincas')}>
          <i className="bi bi-arrow-left me-1"></i>Volver
        </button>
      </div>

      <div className="card card-agro">
        <div className="card-body p-3 p-md-4">
          {!isOnline && <div className="alert alert-warning py-2 small mb-3"><i className="bi bi-wifi-off me-1"></i>Sin conexión — Se guardará localmente</div>}

          <div className="row mb-4 g-2">
            <div className="col-6 col-md-3">
              <div className="p-2 bg-white rounded border-start border-success border-3">
                <small className="text-muted d-block">Área Total</small>
                <strong>{finca.areaTotal ? parseFloat(finca.areaTotal).toFixed(2) : '0.00'} ha</strong>
              </div>
            </div>
            <div className="col-6 col-md-3">
              <div className="p-2 bg-white rounded border-start border-warning border-3">
                <small className="text-muted d-block">Asignada</small>
                <strong>{calculateAreaUtilizada().toFixed(2)} ha</strong>
              </div>
            </div>
            <div className="col-6 col-md-3">
              <div className="p-2 bg-white rounded border-start border-info border-3">
                <small className="text-muted d-block">Disponible</small>
                <strong className={getAreaDisponible() > 0 ? 'text-success' : 'text-danger'}>
                  {getAreaDisponible().toFixed(2)} ha
                </strong>
              </div>
            </div>
            <div className="col-6 col-md-3">
              <div className="p-2 bg-white rounded border-start border-primary border-3">
                <small className="text-muted d-block">Cantidad</small>
                <strong>{parcelas.length}</strong>
              </div>
            </div>
          </div>

          {parcelaError && <div className="alert alert-danger py-2 small mb-3">{parcelaError}</div>}

          <div className="d-flex justify-content-between align-items-center mb-3">
            <h5 className="mb-0">{editingParcela ? 'Editar Parcela' : 'Agregar Nueva Parcela'}</h5>
            {showParcelaForm && (
              <button type="button" className="btn btn-sm btn-outline-secondary" onClick={() => { setShowParcelaForm(false); resetParcelaForm(); }}>
                <i className="bi bi-chevron-up me-1"></i>Ocultar
              </button>
            )}
          </div>

          {!showParcelaForm && (
            <button type="button" className="btn btn-outline-agro mb-3" onClick={() => setShowParcelaForm(true)}>
              <i className="bi bi-plus-circle me-1"></i>Nueva Parcela
            </button>
          )}

          {showParcelaForm && (
            <form onSubmit={handleSubmitParcela} className="mb-4 p-3 bg-light rounded">
              <div className="row mb-3 g-2">
                <div className="col-12 col-md-3">
                  <label className="form-label mb-1">Nombre *</label>
                  <input
                    type="text"
                    className="form-control"
                    name="nombre"
                    placeholder="Ej: Lote A"
                    value={newParcela.nombre}
                    onChange={handleParcelaChange}
                    required
                  />
                </div>
                <div className="col-12 col-md-2">
                  <label className="form-label mb-1">Área (ha) *</label>
                  <input
                    type="number"
                    step="0.1"
                    min="0.1"
                    className="form-control"
                    name="areaParcela"
                    placeholder="10"
                    value={newParcela.areaParcela}
                    onChange={handleParcelaChange}
                    required
                    disabled={editingParcela && editingParcela.estado === 'OCUPADA'}
                  />
                </div>
                <div className="col-12 col-md-3">
                  <label className="form-label mb-1">Tipo de suelo</label>
                  <select
                    className="form-select"
                    name="tipoSuelo"
                    value={newParcela.tipoSuelo}
                    onChange={handleParcelaChange}
                  >
                    <option value="">Seleccionar...</option>
                    <option value="arcilloso">Arcilloso</option>
                    <option value="arenoso">Arenoso</option>
                    <option value="limoso">Limoso</option>
                    <option value="franco">Franco</option>
                    <option value="otro">Otro</option>
                  </select>
                </div>
                <div className="col-12 col-md-4">
                  <label className="form-label mb-1">Descripción</label>
                  <input
                    type="text"
                    className="form-control"
                    name="descripcion"
                    placeholder="Observaciones"
                    value={newParcela.descripcion}
                    onChange={handleParcelaChange}
                  />
                </div>
              </div>

              <div className="d-flex gap-2">
                <button type="submit" className="btn btn-agro">
                  <i className={`bi ${editingParcela ? 'bi-check2-circle' : 'bi-plus-circle'} me-1`}></i>
                  {editingParcela ? 'Actualizar Parcela' : 'Agregar Parcela'}
                </button>
                {editingParcela && (
                  <button type="button" className="btn btn-secondary" onClick={handleCancelEditParcela}>
                    Cancelar edición
                  </button>
                )}
              </div>
            </form>
          )}

          <h5 className="mb-3">Listado de Parcelas</h5>

          {loadingParcelas ? (
            <div className="text-center py-3">
              <span className="spinner-border spinner-border-sm me-2"></span>
              <small className="text-muted">Cargando parcelas...</small>
            </div>
          ) : parcelas.length === 0 ? (
            <div className="alert alert-info">
              <i className="bi bi-inbox me-2"></i>
              No hay parcelas registradas. Agrega la primera parcela para comenzar.
            </div>
          ) : (
            <div className="table-responsive">
              <table className="table table-sm table-hover mb-0">
                <thead className="table-light">
                  <tr>
                    <th>Nombre</th>
                    <th>Área (ha)</th>
                    <th>Tipo de Suelo</th>
                    <th>Estado</th>
                    <th className="text-end">Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {parcelas.map((p) => (
                    <tr key={p.id}>
                      <td><strong>{p.nombre}</strong></td>
                      <td>{(parseFloat(p.areaParcela) || 0).toFixed(2)}</td>
                      <td><small className="text-muted">{p.tipoSuelo || '-'}</small></td>
                      <td>
                        <span className={`badge bg-${p.estado === 'DISPONIBLE' ? 'success' : p.estado === 'OCUPADA' ? 'warning text-dark' : 'secondary'}`}>
                          {p.estado}
                        </span>
                      </td>
                      <td className="text-end">
                        {p.estado === 'DISPONIBLE' ? (
                          <>
                            <button
                              type="button"
                              className="btn btn-sm btn-outline-primary me-1"
                              onClick={() => handleStartEditParcela(p)}
                              title="Editar"
                            >
                              <i className="bi bi-pencil"></i>
                            </button>
                            <button
                              type="button"
                              className="btn btn-sm btn-outline-danger"
                              onClick={() => handleDeleteParcela(p)}
                              title="Eliminar"
                            >
                              <i className="bi bi-trash"></i>
                            </button>
                          </>
                        ) : (
                          <small className="text-muted">Sin acciones</small>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
