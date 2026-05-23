import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { supervisionService } from '../services/apiServices';
import { useToast } from '../context/ToastContext';

export default function TecnicoMisFincasPage() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedItem, setSelectedItem] = useState(null);
  const { addToast } = useToast();

  const load = async () => {
    try {
      setLoading(true);
      const res = await supervisionService.listarMisFincas();
      setItems(res.data?.datos || res.data || []);
    } catch (err) {
      console.error(err);
      addToast('No se pudieron cargar tus fincas supervisadas', 'error');
      setItems([]);
    } finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const handleFinalizar = async (id) => {
    if (!window.confirm('¿Finalizar esta supervisión? Ya no podrás registrar actividades en esta finca.')) return;
    try {
      await supervisionService.finalizar(id);
      addToast('Supervisión finalizada', 'success');
      load();
    } catch (err) {
      console.error(err);
      addToast(err.response?.data?.mensaje || 'Error al finalizar supervisión', 'error');
    }
  };

  return (
    <div className="content-wrapper">
      <div className="page-header d-flex justify-content-between align-items-center flex-wrap gap-2">
        <div>
          <h2><i className="bi bi-briefcase me-2"></i>Mis fincas supervisadas</h2>
          <p className="text-muted mb-0">Supervisiones activas — puedes registrar actividades y ver el historial</p>
        </div>
        <Link to="/actividades/registrar" className="btn btn-agro">
          <i className="bi bi-journal-plus me-1"></i>Registrar actividad
        </Link>
      </div>

      <div className="card card-agro">
        <div className="card-body p-3 p-md-4">
          {loading ? (
            <div className="text-center py-4">
              <span className="spinner-border spinner-border-sm me-2"></span>Cargando...
            </div>
          ) : items.length === 0 ? (
            <div className="alert alert-info">
              <i className="bi bi-info-circle me-2"></i>
              No supervisas ninguna finca actualmente.{' '}
              <Link to="/tecnico/fincas-disponibles" className="alert-link">
                Ver fincas disponibles
              </Link>
            </div>
          ) : (
            <>
              {/* Cards en mobile, tabla en desktop */}
              <div className="d-md-none">
                {items.map(i => (
                  <div key={i.supervisionId} className="card mb-3 border shadow-sm">
                    <div className="card-body">
                      <h6 className="fw-bold mb-1">{i.fincaNombre}</h6>
                      <div className="text-muted small mb-2">
                        {i.municipio && <span><i className="bi bi-geo-alt me-1"></i>{i.municipio} · </span>}
                        <span>{(parseFloat(i.areaTotal) || 0).toFixed(2)} ha</span>
                      </div>
                      {i.productorNombre && (
                        <div className="text-muted small mb-2">
                          <i className="bi bi-person me-1"></i>{i.productorNombre}
                        </div>
                      )}
                      <div className="d-flex gap-2 flex-wrap mt-2">
                        <Link
                          to={`/actividades/registrar?fincaId=${i.fincaId}`}
                          className="btn btn-sm btn-agro"
                        >
                          <i className="bi bi-journal-plus me-1"></i>Registrar actividad
                        </Link>
                        <Link
                          to={`/actividades/finca/${i.fincaId}`}
                          className="btn btn-sm btn-agro-outline"
                        >
                          <i className="bi bi-clock-history me-1"></i>Historial
                        </Link>
                        <button
                          className="btn btn-sm btn-outline-danger"
                          onClick={() => handleFinalizar(i.supervisionId)}
                        >
                          Finalizar
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>

              <div className="d-none d-md-block table-responsive">
                <table className="table table-sm table-hover mb-0 align-middle">
                  <thead className="table-light">
                    <tr>
                      <th>Finca</th>
                      <th>Agricultor</th>
                      <th>Municipio</th>
                      <th>Área (ha)</th>
                      <th>Inicio</th>
                      <th className="text-end">Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    {items.map(i => (
                      <tr key={i.supervisionId}>
                        <td><strong>{i.fincaNombre}</strong></td>
                        <td>{i.productorNombre || '-'}</td>
                        <td>{i.municipio || '-'}</td>
                        <td>{(parseFloat(i.areaTotal) || 0).toFixed(2)}</td>
                        <td className="small text-muted">
                          {i.fechaInicio ? new Date(i.fechaInicio).toLocaleDateString('es-CO') : '-'}
                        </td>
                        <td className="text-end">
                          <Link
                            to={`/actividades/registrar?fincaId=${i.fincaId}`}
                            className="btn btn-sm btn-agro me-1"
                          >
                            <i className="bi bi-journal-plus me-1"></i>Actividad
                          </Link>
                          <Link
                            to={`/actividades/finca/${i.fincaId}`}
                            className="btn btn-sm btn-agro-outline me-1"
                          >
                            <i className="bi bi-clock-history me-1"></i>Historial
                          </Link>
                          <button
                            className="btn btn-sm btn-outline-secondary me-1"
                            onClick={() => setSelectedItem(i)}
                          >
                            Detalle
                          </button>
                          <button
                            className="btn btn-sm btn-outline-danger"
                            onClick={() => handleFinalizar(i.supervisionId)}
                          >
                            Finalizar
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </>
          )}
        </div>
      </div>

      {/* Modal detalle */}
      {selectedItem && (
        <div className="modal d-block" tabIndex="-1" style={{ background: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-dialog-centered modal-lg">
            <div className="modal-content border-0 shadow">
              <div className="modal-header border-0 pb-0">
                <div>
                  <h5 className="modal-title fw-bold mb-1">Detalle de supervisión</h5>
                  <p className="text-muted small mb-0">Información de la finca asignada</p>
                </div>
                <button type="button" className="btn-close" onClick={() => setSelectedItem(null)}></button>
              </div>
              <div className="modal-body pt-3">
                <div className="row g-3">
                  <div className="col-12 col-md-6">
                    <small className="text-muted d-block">Finca</small>
                    <strong>{selectedItem.fincaNombre}</strong>
                  </div>
                  <div className="col-12 col-md-6">
                    <small className="text-muted d-block">Agricultor</small>
                    <strong>{selectedItem.productorNombre || '-'}</strong>
                  </div>
                  <div className="col-12 col-md-6">
                    <small className="text-muted d-block">Municipio</small>
                    <strong>{selectedItem.municipio || '-'}</strong>
                  </div>
                  <div className="col-12 col-md-6">
                    <small className="text-muted d-block">Área total</small>
                    <strong>{(parseFloat(selectedItem.areaTotal) || 0).toFixed(2)} ha</strong>
                  </div>
                  <div className="col-12 col-md-6">
                    <small className="text-muted d-block">Inicio supervisión</small>
                    <strong>{selectedItem.fechaInicio ? new Date(selectedItem.fechaInicio).toLocaleString('es-CO') : '-'}</strong>
                  </div>
                  <div className="col-12 col-md-6">
                    <small className="text-muted d-block">Estado</small>
                    <span className="badge bg-success">ACTIVA</span>
                  </div>
                </div>
              </div>
              <div className="modal-footer border-0 pt-0 gap-2">
                <Link
                  to={`/actividades/registrar?fincaId=${selectedItem.fincaId}`}
                  className="btn btn-agro"
                  onClick={() => setSelectedItem(null)}
                >
                  <i className="bi bi-journal-plus me-1"></i>Registrar actividad
                </Link>
                <Link
                  to={`/actividades/finca/${selectedItem.fincaId}`}
                  className="btn btn-agro-outline"
                  onClick={() => setSelectedItem(null)}
                >
                  <i className="bi bi-clock-history me-1"></i>Ver historial
                </Link>
                <button type="button" className="btn btn-secondary" onClick={() => setSelectedItem(null)}>
                  Cerrar
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
