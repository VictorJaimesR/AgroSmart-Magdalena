import { useState, useEffect } from 'react';
import { reporteService } from '../services/apiServices';
import { useAuth } from '../context/AuthContext';
import { LoadingSpinner, EmptyState } from '../components/UIComponents';

export default function ReportesPage() {
  const { user } = useAuth();
  const [reportes, setReportes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);

  useEffect(() => { loadReportes(); }, []);

  const loadReportes = async () => {
    setLoading(true);
    try {
      const res = await reporteService.listarPorProductor(user?.usuarioId || 1, 0);
      setReportes(res.data?.datos?.content || []);
    } catch { setReportes([]); }
    finally { setLoading(false); }
  };

  const generarReporte = async (tipo) => {
    setGenerating(true);
    try {
      await reporteService.generar(user?.usuarioId || 1, { tipo, titulo: `Reporte de ${tipo.toLowerCase()}` });
      loadReportes();
    } catch {}
    finally { setGenerating(false); }
  };

  return (
    <div className="content-wrapper">
      <div className="page-header">
        <h2><i className="bi bi-file-earmark-bar-graph me-2"></i>Reportes</h2>
      </div>

      {/* Exportar reportes en CSV */}
      <div className="card card-agro mb-4">
        <div className="card-header"><i className="bi bi-filetype-csv me-2"></i>Exportación CSV Rápida</div>
        <div className="card-body">
          <div className="row g-2">
            <div className="col-12 col-md-4">
              <button className="btn btn-outline-success w-100 py-3 d-flex flex-column align-items-center"
               onClick={async () => {
                   const res = await reporteService.exportarProduccionCsv(user?.usuarioId || 1);
                   const url = window.URL.createObjectURL(new Blob([res.data]));
                   const link = document.createElement('a');
                   link.href = url;
                   link.setAttribute('download', 'produccion.csv');
                   document.body.appendChild(link);
                   link.click();
                }} disabled={generating}>
                <i className="bi bi-graph-up fs-4 mb-1"></i>
                <small>Producción (CSV)</small>
              </button>
            </div>
            
            <div className="col-12 col-md-4">
              <button className="btn btn-outline-success w-100 py-3 d-flex flex-column align-items-center"
                onClick={async () => {
                   const res = await reporteService.exportarInventarioCsv();
                   const url = window.URL.createObjectURL(new Blob([res.data]));
                   const link = document.createElement('a');
                   link.href = url;
                   link.setAttribute('download', 'inventario_cultivos.csv');
                   document.body.appendChild(link);
                   link.click();
                }} disabled={generating}>
                <i className="bi bi-list-nested fs-4 mb-1"></i>
                <small>Inventario Cultivos (CSV)</small>
              </button>
            </div>

            <div className="col-12 col-md-4">
              <button className="btn btn-outline-success w-100 py-3 d-flex flex-column align-items-center"
                onClick={async () => {
                   const res = await reporteService.exportarAlertasCsv();
                   const url = window.URL.createObjectURL(new Blob([res.data]));
                   const link = document.createElement('a');
                   link.href = url;
                   link.setAttribute('download', 'historial_alertas.csv');
                   document.body.appendChild(link);
                   link.click();
                }} disabled={generating}>
                <i className="bi bi-exclamation-triangle fs-4 mb-1"></i>
                <small>Historial Alertas (CSV)</small>
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Lista de reportes */}
      {loading ? <LoadingSpinner /> : reportes.length === 0 ? (
        <EmptyState icon="📊" text="No hay reportes generados" />
      ) : (
        <div className="d-flex flex-column gap-3">
          {reportes.map((r) => (
            <div key={r.id} className="card card-agro">
              <div className="card-body">
                <div className="d-flex justify-content-between align-items-start mb-2">
                  <h6 className="fw-bold mb-0">{r.titulo}</h6>
                  <span className="badge bg-primary bg-opacity-10 text-primary">{r.tipo}</span>
                </div>
                <div className="text-muted small">
                  <i className="bi bi-calendar me-1"></i>
                  {new Date(r.fechaGeneracion).toLocaleDateString('es-CO')}
                  {r.periodoInicio && <> · Periodo: {r.periodoInicio} a {r.periodoFin}</>}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
