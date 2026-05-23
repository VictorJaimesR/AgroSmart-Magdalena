import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { supervisionService } from '../services/apiServices';
import { useToast } from '../context/ToastContext';

export default function TecnicoProductoresPage() {
  const [productores, setProductores] = useState([]);
  const [loading, setLoading] = useState(true);
  const [busqueda, setBusqueda] = useState('');
  const { addToast } = useToast();

  useEffect(() => { cargarProductores(); }, []);

  const cargarProductores = async () => {
    try {
      setLoading(true);
      const res = await supervisionService.listarMisFincas();
      const fincas = res.data?.datos || [];

      // Agrupar fincas por productor
      const mapaProductores = {};
      fincas.forEach(f => {
        if (!f.productorNombre) return;
        const key = f.productorNombre;
        if (!mapaProductores[key]) {
          mapaProductores[key] = {
            nombre: f.productorNombre,
            fincas: [],
          };
        }
        mapaProductores[key].fincas.push({
          id: f.fincaId,
          nombre: f.fincaNombre,
          municipio: f.municipio,
          area: f.areaTotal,
          supervisionId: f.supervisionId,
        });
      });

      setProductores(Object.values(mapaProductores));
    } catch (err) {
      console.error(err);
      addToast('Error cargando productores', 'error');
    } finally {
      setLoading(false);
    }
  };

  const producoresFiltrados = productores.filter(p =>
    p.nombre.toLowerCase().includes(busqueda.toLowerCase())
  );

  return (
    <div className="content-wrapper">
      <div className="page-header d-flex justify-content-between align-items-start flex-wrap gap-2">
        <div>
          <h2><i className="bi bi-people me-2"></i>Productores supervisados</h2>
          <p className="text-muted mb-0">
            Productores con fincas que estás monitoreando actualmente
          </p>
        </div>
        <Link to="/tecnico/fincas-disponibles" className="btn btn-agro-outline">
          <i className="bi bi-geo-alt me-1"></i>Fincas disponibles
        </Link>
      </div>

      {/* Buscador */}
      <div className="mb-3">
        <div className="input-group">
          <span className="input-group-text bg-white">
            <i className="bi bi-search text-muted"></i>
          </span>
          <input
            type="text"
            className="form-control border-start-0"
            placeholder="Buscar productor..."
            value={busqueda}
            onChange={e => setBusqueda(e.target.value)}
          />
        </div>
      </div>

      {loading ? (
        <div className="text-center py-5">
          <span className="spinner-border spinner-border-sm me-2"></span>Cargando...
        </div>
      ) : producoresFiltrados.length === 0 ? (
        <div className="card card-agro">
          <div className="card-body text-center py-5">
            <i className="bi bi-people fs-1 text-muted d-block mb-3"></i>
            {busqueda
              ? `No se encontraron productores con "${busqueda}"`
              : 'No supervisas ninguna finca actualmente.'}
            {!busqueda && (
              <div className="mt-3">
                <Link to="/tecnico/fincas-disponibles" className="btn btn-agro">
                  Ver fincas disponibles
                </Link>
              </div>
            )}
          </div>
        </div>
      ) : (
        <div className="row g-3">
          {producoresFiltrados.map((p, idx) => (
            <div key={idx} className="col-12 col-md-6">
              <div className="card card-agro h-100">
                <div className="card-body">
                  {/* Header productor */}
                  <div className="d-flex align-items-center gap-3 mb-3">
                    <div className="rounded-circle bg-success bg-opacity-10 d-flex align-items-center justify-content-center"
                      style={{ width: 48, height: 48, flexShrink: 0 }}>
                      <i className="bi bi-person-fill text-success fs-5"></i>
                    </div>
                    <div>
                      <h6 className="fw-bold mb-0">{p.nombre}</h6>
                      <small className="text-muted">
                        {p.fincas.length} finca{p.fincas.length !== 1 ? 's' : ''} supervisada{p.fincas.length !== 1 ? 's' : ''}
                      </small>
                    </div>
                  </div>

                  {/* Fincas del productor */}
                  <div className="d-flex flex-column gap-2">
                    {p.fincas.map(f => (
                      <div key={f.id} className="bg-light rounded p-2 d-flex justify-content-between align-items-center">
                        <div>
                          <div className="fw-semibold small">
                            <i className="bi bi-geo-alt me-1 text-success"></i>{f.nombre}
                          </div>
                          <div className="text-muted small">
                            {f.municipio && <span>{f.municipio} · </span>}
                            {f.area && <span>{parseFloat(f.area).toFixed(1)} ha</span>}
                          </div>
                        </div>
                        <div className="d-flex gap-1">
                          <Link
                            to={`/actividades/registrar?fincaId=${f.id}`}
                            className="btn btn-sm btn-agro"
                            title="Registrar actividad"
                          >
                            <i className="bi bi-journal-plus"></i>
                          </Link>
                          <Link
                            to={`/actividades/finca/${f.id}`}
                            className="btn btn-sm btn-agro-outline"
                            title="Ver historial"
                          >
                            <i className="bi bi-clock-history"></i>
                          </Link>
                        </div>
                      </div>
                    ))}
                  </div>

                  {/* Acciones del productor */}
                  <div className="mt-3 pt-2 border-top d-flex gap-2">
                    <Link
                      to={`/recomendaciones/nueva?fincaId=${p.fincas[0]?.id}`}
                      className="btn btn-sm btn-agro-outline flex-grow-1"
                    >
                      <i className="bi bi-lightbulb me-1"></i>Nueva recomendación
                    </Link>
                    <Link
                      to={`/cultivos/nuevo`}
                      className="btn btn-sm btn-outline-secondary flex-grow-1"
                    >
                      <i className="bi bi-flower1 me-1"></i>Nuevo cultivo
                    </Link>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}