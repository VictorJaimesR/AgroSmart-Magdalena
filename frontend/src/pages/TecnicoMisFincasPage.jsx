import { useEffect, useState } from 'react';
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
    if (!window.confirm('Finalizar supervisión?')) return;
    try {
      await supervisionService.finalizar(id);
      addToast('Supervisión finalizada', 'success');
      load();
    } catch (err) {
      console.error(err);
      addToast(err.response?.data?.mensaje || 'Error al finalizar supervisión', 'error');
    }
  };

  const handleVerDetalles = (item) => {
    setSelectedItem(item);
  };

  return (
    <div className="content-wrapper">
      <div className="page-header d-flex justify-content-between align-items-center">
        <div>
          <h2><i className="bi bi-briefcase me-2"></i>Mis fincas supervisadas</h2>
          <p className="text-muted mb-0">Supervisiones activas asignadas a tu usuario</p>
        </div>
      </div>

      <div className="card card-agro">
        <div className="card-body p-3 p-md-4">
          {loading ? (
            <div className="text-center py-4"><span className="spinner-border spinner-border-sm me-2"></span>Cargando...</div>
          ) : items.length === 0 ? (
            <div className="alert alert-info">No supervisas ninguna finca actualmente.</div>
          ) : (
            <div className="table-responsive">
              <table className="table table-sm table-hover mb-0">
                <thead className="table-light">
                  <tr>
                    <th>Finca</th>
                    <th>Agricultor</th>
                    <th>Municipio</th>
                    <th>Área (ha)</th>
                    <th>Fecha inicio</th>
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
                      <td>{i.fechaInicio ? new Date(i.fechaInicio).toLocaleString() : '-'}</td>
                      <td className="text-end">
                        <button className="btn btn-sm btn-outline-secondary me-1" onClick={() => handleVerDetalles(i)}>
                          Ver detalles
                        </button>
                        <button className="btn btn-sm btn-danger" onClick={() => handleFinalizar(i.supervisionId)}>Finalizar</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {selectedItem && (
        <div className="modal d-block" tabIndex="-1" style={{ background: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-dialog-centered modal-lg">
            <div className="modal-content border-0 shadow">
              <div className="modal-header border-0 pb-0">
                <div>
                  <h5 className="modal-title fw-bold mb-1">Detalle de supervisión</h5>
                  <p className="text-muted small mb-0">Información de la finca y la supervisión activa</p>
                </div>
                <button type="button" className="btn-close" aria-label="Cerrar" onClick={() => setSelectedItem(null)}></button>
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
                    <small className="text-muted d-block">Área</small>
                    <strong>{(parseFloat(selectedItem.areaTotal) || 0).toFixed(2)} ha</strong>
                  </div>
                  <div className="col-12 col-md-6">
                    <small className="text-muted d-block">Fecha inicio</small>
                    <strong>{selectedItem.fechaInicio ? new Date(selectedItem.fechaInicio).toLocaleString() : '-'}</strong>
                  </div>
                  <div className="col-12 col-md-6">
                    <small className="text-muted d-block">Estado</small>
                    <span className="badge bg-success">ACTIVA</span>
                  </div>
                </div>
              </div>
              <div className="modal-footer border-0 pt-0">
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
