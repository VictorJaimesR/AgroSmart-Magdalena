import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { adminFincaService } from '../services/apiServices';
import { LoadingSpinner } from '../components/UIComponents';
import { useToast } from '../context/ToastContext';

export default function AdminFincaDetallePage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { addToast } = useToast();
  const [finca, setFinca] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    cargarDetalle();
  }, [id]);

  const cargarDetalle = async () => {
    setLoading(true);
    try {
      const res = await adminFincaService.obtenerDetalle(id);
      setFinca(res.data?.datos || null);
    } catch (error) {
      addToast(error.response?.data?.mensaje || 'No se pudo cargar el detalle de la finca', 'error');
      navigate('/admin/fincas');
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <LoadingSpinner text="Cargando detalle de la finca..." />;
  if (!finca) return <div className="alert alert-danger">Finca no encontrada</div>;

  return (
    <div className="content-wrapper">
      {/* Header */}
      <div className="mb-4 d-flex justify-content-between align-items-center">
        <div>
          <button onClick={() => navigate('/admin/fincas')} className="btn btn-sm btn-outline-secondary mb-2">
            <i className="bi bi-arrow-left me-1"></i>Volver
          </button>
          <h2 className="fw-bold mb-1">
            <i className="bi bi-geo-alt me-2"></i>{finca.nombre}
          </h2>
          <p className="text-muted mb-0">Detalle de finca - Vista solo lectura</p>
        </div>
        <span className={`badge bg-success`} style={{ height: 'fit-content' }}>
          {finca.estado}
        </span>
      </div>

      {/* Grid principal */}
      <div className="row g-3">
        {/* Información General */}
        <div className="col-12 col-lg-6">
          <div className="card card-agro h-100">
            <div className="card-header bg-agro-light">
              <h5 className="mb-0">
                <i className="bi bi-info-circle me-2"></i>Información General
              </h5>
            </div>
            <div className="card-body">
              <div className="row g-3">
                <div className="col-6">
                  <small className="text-muted">Nombre de Finca</small>
                  <p className="fw-bold">{finca.nombre}</p>
                </div>
                <div className="col-6">
                  <small className="text-muted">Área Total</small>
                  <p className="fw-bold">{finca.areaTotal} {finca.unidadArea}</p>
                </div>
                <div className="col-12">
                  <small className="text-muted">Descripción</small>
                  <p>{finca.descripcion || 'Sin descripción'}</p>
                </div>
                <div className="col-12">
                  <small className="text-muted">Ubicación</small>
                  <p className="fw-bold">
                    {finca.vereda}, {finca.municipio}, {finca.departamento}
                  </p>
                  {finca.latitud && finca.longitud && (
                    <a 
                      href={`https://www.google.com/maps/search/?api=1&query=${finca.latitud},${finca.longitud}`}
                      target="_blank"
                      rel="noreferrer"
                      className="btn btn-sm btn-outline-primary"
                    >
                      <i className="bi bi-map me-1"></i>Ver en mapa
                    </a>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Agricultor Dueño */}
        <div className="col-12 col-lg-6">
          <div className="card card-agro h-100">
            <div className="card-header bg-agro-light">
              <h5 className="mb-0">
                <i className="bi bi-person me-2"></i>Agricultor Dueño
              </h5>
            </div>
            <div className="card-body">
              <div className="row g-3">
                <div className="col-12">
                  <small className="text-muted">Nombre Completo</small>
                  <p className="fw-bold">{finca.productorNombre}</p>
                </div>
                <div className="col-6">
                  <small className="text-muted">Cédula</small>
                  <p><code>{finca.productorCedula}</code></p>
                </div>
                <div className="col-6">
                  <small className="text-muted">Teléfono</small>
                  <p className="fw-bold">{finca.productorTelefono || '-'}</p>
                </div>
                <div className="col-12">
                  <small className="text-muted">Email</small>
                  <p>
                    <a href={`mailto:${finca.productorEmail}`}>{finca.productorEmail}</a>
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Técnico Asociado */}
        <div className="col-12 col-lg-6">
          <div className="card card-agro h-100">
            <div className="card-header bg-agro-light">
              <h5 className="mb-0">
                <i className="bi bi-clipboard-check me-2"></i>Técnico Asociado
              </h5>
            </div>
            <div className="card-body">
              <div className="row g-3">
                <div className="col-12">
                  <small className="text-muted">Nombre</small>
                  <p className="fw-bold">{finca.tecnicoNombre}</p>
                </div>
                <div className="col-12">
                  <small className="text-muted">Especialidad</small>
                  <p className="fw-bold">{finca.tecnicoEspecialidad || '-'}</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Métricas */}
        <div className="col-12 col-lg-6">
          <div className="card card-agro h-100">
            <div className="card-header bg-agro-light">
              <h5 className="mb-0">
                <i className="bi bi-graph-up me-2"></i>Métricas
              </h5>
            </div>
            <div className="card-body">
              <div className="row text-center">
                <div className="col-4">
                  <div className="stat-box">
                    <h3 className="text-primary">{finca.cantidadParcelas}</h3>
                    <small className="text-muted">Parcelas</small>
                  </div>
                </div>
                <div className="col-4">
                  <div className="stat-box">
                    <h3 className="text-success">{finca.cantidadCultivos}</h3>
                    <small className="text-muted">Cultivos</small>
                  </div>
                </div>
                <div className="col-4">
                  <div className="stat-box">
                    <h3 className="text-info">{finca.areaCultivada?.toFixed(2) || 0}</h3>
                    <small className="text-muted">Ha. Cultivadas</small>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Parcelas y Cultivos */}
        <div className="col-12">
          <div className="card card-agro">
            <div className="card-header bg-agro-light">
              <h5 className="mb-0">
                <i className="bi bi-grid-3x2-gap me-2"></i>Parcelas y Cultivos
              </h5>
            </div>
            <div className="card-body">
              {finca.parcelas && finca.parcelas.length > 0 ? (
                <div className="accordion" id="parcelasAccordion">
                  {finca.parcelas.map((parcela, idx) => (
                    <div key={parcela.id} className="accordion-item border">
                      <h2 className="accordion-header">
                        <button
                          className="accordion-button collapsed"
                          type="button"
                          data-bs-toggle="collapse"
                          data-bs-target={`#parcela${parcela.id}`}
                          aria-expanded="false"
                          aria-controls={`parcela${parcela.id}`}
                        >
                          <i className="bi bi-square-fill text-info me-2" style={{ fontSize: '0.5rem' }}></i>
                          <strong>{parcela.nombre}</strong>
                          <span className="badge bg-secondary ms-2">{parcela.cultivos?.length || 0} cultivos</span>
                        </button>
                      </h2>
                      <div id={`parcela${parcela.id}`} className="accordion-collapse collapse" 
                           data-bs-parent="#parcelasAccordion">
                        <div className="accordion-body bg-light">
                          {/* Info Parcela */}
                          <div className="row g-2 mb-3">
                            <div className="col-12 col-md-3">
                              <small className="text-muted">Área</small>
                              <p className="fw-bold">{parcela.areaParcela} {parcela.unidadArea}</p>
                            </div>
                            <div className="col-12 col-md-3">
                              <small className="text-muted">Tipo de Suelo</small>
                              <p className="fw-bold">{parcela.tipoSuelo || '-'}</p>
                            </div>
                            <div className="col-12 col-md-3">
                              <small className="text-muted">Estado</small>
                              <p className="fw-bold">{parcela.estado}</p>
                            </div>
                            <div className="col-12 col-md-3">
                              <small className="text-muted">Descripción</small>
                              <p className="small">{parcela.descripcion || '-'}</p>
                            </div>
                          </div>

                          {/* Cultivos */}
                          {parcela.cultivos && parcela.cultivos.length > 0 ? (
                            <div className="table-responsive">
                              <table className="table table-sm table-hover">
                                <thead className="table-light">
                                  <tr>
                                    <th>Cultivo</th>
                                    <th>Variedad</th>
                                    <th>Fecha Siembra</th>
                                    <th>Área</th>
                                    <th>Estado</th>
                                    <th>Rendimiento</th>
                                  </tr>
                                </thead>
                                <tbody>
                                  {parcela.cultivos.map((cultivo) => (
                                    <tr key={cultivo.id}>
                                      <td className="fw-bold">{cultivo.nombre}</td>
                                      <td><small>{cultivo.variedad || '-'}</small></td>
                                      <td><small>{cultivo.fechaSiembra}</small></td>
                                      <td><small>{cultivo.areaUtilizada || '-'} ha</small></td>
                                      <td>
                                        <span className="badge bg-primary">{cultivo.estado}</span>
                                      </td>
                                      <td>
                                        <small>
                                          {cultivo.rendimientoReal 
                                            ? `${cultivo.rendimientoReal} ${cultivo.unidadRendimiento}`
                                            : cultivo.rendimientoEsperado 
                                            ? `Est: ${cultivo.rendimientoEsperado} ${cultivo.unidadRendimiento}`
                                            : '-'
                                          }
                                        </small>
                                      </td>
                                    </tr>
                                  ))}
                                </tbody>
                              </table>
                            </div>
                          ) : (
                            <div className="alert alert-info mb-0">
                              <i className="bi bi-info-circle me-2"></i>
                              No hay cultivos registrados en esta parcela
                            </div>
                          )}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="alert alert-info mb-0">
                  <i className="bi bi-info-circle me-2"></i>
                  No hay parcelas registradas en esta finca
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Alert informativo */}
      <div className="alert alert-info alert-dismissible fade show mt-4" role="alert">
        <i className="bi bi-info-circle me-2"></i>
        <strong>Información:</strong> Esta es una vista de solo lectura. No puedes realizar modificaciones.
        <button type="button" className="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
      </div>
    </div>
  );
}
