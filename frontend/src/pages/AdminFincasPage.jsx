import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { adminFincaService } from '../services/apiServices';
import { LoadingSpinner, EmptyState } from '../components/UIComponents';
import { useToast } from '../context/ToastContext';

const STATE_OPTIONS = ['TODOS', 'ACTIVO'];

function badgeEstadoClass(estado) {
  if (estado === 'ACTIVO') return 'bg-success';
  return 'bg-secondary';
}

export default function AdminFincasPage() {
  const { addToast } = useToast();
  const navigate = useNavigate();
  const [fincas, setFincas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [stateFilter, setStateFilter] = useState('TODOS');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const cargarFincas = async () => {
    setLoading(true);
    try {
      const res = await adminFincaService.listarFincas(page);
      setFincas(res.data?.datos?.content || []);
      setTotalPages(res.data?.datos?.totalPages || 0);
    } catch (error) {
      addToast(error.response?.data?.mensaje || 'No se pudo cargar el listado de fincas', 'error');
      setFincas([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarFincas();
  }, [page]);

  const fincasFiltradas = useMemo(() => {
    const term = search.trim().toLowerCase();
    return fincas.filter((finca) => {
      const coincideBusqueda = !term || [finca.nombre, finca.productorNombre, finca.municipio].some(
        (v) => (v || '').toLowerCase().includes(term)
      );
      const coincideEstado = stateFilter === 'TODOS' || finca.estado === stateFilter;
      return coincideBusqueda && coincideEstado;
    });
  }, [fincas, search, stateFilter]);

  const handleVerDetalles = (id) => {
    navigate(`/admin/fincas/${id}`);
  };

  if (loading) return <LoadingSpinner text="Cargando fincas del sistema..." />;

  return (
    <div className="content-wrapper">
      <div className="mb-4 d-flex flex-wrap align-items-center justify-content-between gap-2">
        <div>
          <h2 className="fw-bold mb-1">Gestión Global de Fincas</h2>
          <p className="text-muted mb-0">Visualiza todas las fincas activas del sistema (solo lectura)</p>
        </div>
        <button className="btn btn-outline-secondary" onClick={cargarFincas}>
          <i className="bi bi-arrow-clockwise me-1"></i>Actualizar
        </button>
      </div>

      <div className="card card-agro mb-3">
        <div className="card-body">
          <div className="row g-2">
            <div className="col-12 col-lg-7">
              <div className="input-group">
                <span className="input-group-text bg-white"><i className="bi bi-search"></i></span>
                <input
                  type="search"
                  className="form-control"
                  placeholder="Buscar por nombre de finca, agricultor o municipio"
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                />
              </div>
            </div>
            <div className="col-12 col-lg-5">
              <select 
                className="form-select" 
                value={stateFilter} 
                onChange={(e) => setStateFilter(e.target.value)}
              >
                {STATE_OPTIONS.map((option) => <option key={option} value={option}>{option}</option>)}
              </select>
            </div>
          </div>
        </div>
      </div>

      {fincasFiltradas.length === 0 ? (
        <EmptyState 
          icon="🌾" 
          text={search ? "No se encontraron fincas que coincidan con tu búsqueda" : "No hay fincas registradas en el sistema"}
        />
      ) : (
        <>
          {/* Tabla responsiva */}
          <div className="table-responsive">
            <table className="table table-hover align-middle">
              <thead className="table-light">
                <tr>
                  <th style={{ maxWidth: '200px' }}>
                    <i className="bi bi-geo-alt me-2"></i>Finca
                  </th>
                  <th style={{ maxWidth: '150px' }}>Agricultor</th>
                  <th style={{ maxWidth: '120px' }}>
                    <i className="bi bi-rulers me-1"></i>Hectáreas
                  </th>
                  <th style={{ maxWidth: '100px' }}>Municipio</th>
                  <th style={{ maxWidth: '100px' }}>Técnico</th>
                  <th style={{ maxWidth: '80px' }}>Parcelas</th>
                  <th style={{ maxWidth: '80px' }}>Estado</th>
                  <th style={{ maxWidth: '100px' }}>Acción</th>
                </tr>
              </thead>
              <tbody>
                {fincasFiltradas.map((finca) => (
                  <tr key={finca.id}>
                    <td className="fw-bold text-primary">
                      <i className="bi bi-geo-alt me-2"></i>
                      {finca.nombre}
                    </td>
                    <td>
                      <small>{finca.productorNombre}</small>
                      <br />
                      <code className="text-muted small">{finca.productorCedula}</code>
                    </td>
                    <td>
                      <strong>{finca.areaTotal}</strong> {finca.unidadArea}
                    </td>
                    <td>
                      <small>{finca.municipio}</small>
                      <br />
                      <code className="text-muted small">{finca.departamento}</code>
                    </td>
                    <td>
                      <small>{finca.tecnicoNombre}</small>
                    </td>
                    <td>
                      <span className="badge bg-info">
                        {finca.cantidadParcelas} parcelas
                      </span>
                    </td>
                    <td>
                      <span className={`badge ${badgeEstadoClass(finca.estado)}`}>
                        {finca.estado}
                      </span>
                    </td>
                    <td>
                      <button 
                        className="btn btn-sm btn-agro-outline"
                        onClick={() => handleVerDetalles(finca.id)}
                        title="Ver detalles"
                      >
                        <i className="bi bi-eye"></i> Detalles
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Paginación */}
          {totalPages > 1 && (
            <div className="d-flex justify-content-center mt-4">
              <nav aria-label="Paginación">
                <ul className="pagination">
                  <li className={`page-item ${page === 0 ? 'disabled' : ''}`}>
                    <button 
                      className="page-link" 
                      onClick={() => setPage(page - 1)}
                      disabled={page === 0}
                    >
                      Anterior
                    </button>
                  </li>
                  {Array.from({ length: totalPages }).map((_, i) => (
                    <li key={i} className={`page-item ${page === i ? 'active' : ''}`}>
                      <button 
                        className="page-link" 
                        onClick={() => setPage(i)}
                      >
                        {i + 1}
                      </button>
                    </li>
                  ))}
                  <li className={`page-item ${page === totalPages - 1 ? 'disabled' : ''}`}>
                    <button 
                      className="page-link" 
                      onClick={() => setPage(page + 1)}
                      disabled={page === totalPages - 1}
                    >
                      Siguiente
                    </button>
                  </li>
                </ul>
              </nav>
            </div>
          )}
        </>
      )}

      <div className="alert alert-info alert-dismissible fade show mt-4" role="alert">
        <i className="bi bi-info-circle me-2"></i>
        <strong>Información:</strong> Esta es una vista global de lectura. No puedes crear, editar ni eliminar fincas desde aquí.
        <button type="button" className="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
      </div>
    </div>
  );
}
