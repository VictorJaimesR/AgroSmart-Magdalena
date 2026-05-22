import { useEffect, useState } from 'react';
import { supervisionService } from '../services/apiServices';
import { useToast } from '../context/ToastContext';

export default function TecnicoFincasDisponiblesPage() {
  const [fincas, setFincas] = useState([]);
  const [loading, setLoading] = useState(false);
  const { addToast } = useToast();

  const load = async () => {
    try {
      setLoading(true);
      const res = await supervisionService.listarDisponibles();
      setFincas(res.data?.datos || res.data || []);
    } catch (err) {
      console.error(err);
      addToast('No se pudieron cargar las fincas disponibles', 'error');
      setFincas([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const handleTomar = async (id) => {
    try {
      await supervisionService.tomarFinca(id);
      addToast('Finca tomada para supervisión', 'success');
      load();
    } catch (err) {
      console.error(err);
      addToast(err.response?.data?.mensaje || 'Error al tomar la finca', 'error');
    }
  };

  return (
    <div className="content-wrapper">
      <div className="page-header d-flex justify-content-between align-items-center">
        <div>
          <h2><i className="bi bi-geo-alt me-2"></i>Fincas disponibles</h2>
          <p className="text-muted mb-0">Fincas activas sin supervisión</p>
        </div>
      </div>

      <div className="card card-agro">
        <div className="card-body p-3 p-md-4">
          {loading ? (
            <div className="text-center py-4"><span className="spinner-border spinner-border-sm me-2"></span>Cargando...</div>
          ) : fincas.length === 0 ? (
            <div className="alert alert-info">No hay fincas disponibles para supervisar.</div>
          ) : (
            <div className="table-responsive">
              <table className="table table-sm table-hover mb-0">
                <thead className="table-light">
                  <tr>
                    <th>Nombre</th>
                    <th>Área (ha)</th>
                    <th>Municipio</th>
                    <th>Agricultor</th>
                    <th className="text-end">Acción</th>
                  </tr>
                </thead>
                <tbody>
                  {fincas.map(f => (
                    <tr key={f.id}>
                      <td><strong>{f.nombre}</strong></td>
                      <td>{(parseFloat(f.areaTotal) || 0).toFixed(2)}</td>
                      <td>{f.municipio || '-'}</td>
                      <td>{f.productorNombre || '-'}</td>
                      <td className="text-end">
                        <button className="btn btn-sm btn-agro" onClick={() => handleTomar(f.id)}>Supervisar</button>
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
