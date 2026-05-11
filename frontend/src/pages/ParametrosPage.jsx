import { useState, useEffect } from 'react';
import { parametroService } from '../services/apiServices';
import { LoadingSpinner, EmptyState, ConfirmModal } from '../components/UIComponents';

export default function ParametrosPage() {
  const [params, setParams] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editKey, setEditKey] = useState(null);
  const [form, setForm] = useState({ clave: '', valor: '', descripcion: '', categoria: '' });
  const [saving, setSaving] = useState(false);
  const [deleteKey, setDeleteKey] = useState(null);

  useEffect(() => { loadParams(); }, []);

  const loadParams = async () => {
    setLoading(true);
    try {
      const res = await parametroService.listar();
      setParams(res.data?.datos || []);
    } catch { setParams([]); }
    finally { setLoading(false); }
  };

  const handleEdit = (p) => {
    setForm({ clave: p.clave, valor: p.valor, descripcion: p.descripcion || '', categoria: p.categoria || '' });
    setEditKey(p.clave);
    setShowForm(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      if (editKey) await parametroService.actualizar(editKey, form);
      else await parametroService.crear(form);
      setShowForm(false); setEditKey(null);
      setForm({ clave: '', valor: '', descripcion: '', categoria: '' });
      loadParams();
    } catch {} finally { setSaving(false); }
  };

  const handleDelete = async () => {
    try { await parametroService.eliminar(deleteKey); loadParams(); } catch {}
    setDeleteKey(null);
  };

  if (loading) return <LoadingSpinner />;

  return (
    <div className="content-wrapper">
      <div className="page-header">
        <h2><i className="bi bi-gear me-2"></i>Parámetros del Sistema</h2>
        <button className="btn btn-agro" onClick={() => { setShowForm(true); setEditKey(null); setForm({ clave: '', valor: '', descripcion: '', categoria: '' }); }}>
          <i className="bi bi-plus-circle"></i>Nuevo
        </button>
      </div>

      {/* Formulario inline */}
      {showForm && (
        <div className="card card-agro mb-3">
          <div className="card-body">
            <form onSubmit={handleSubmit}>
              <div className="row g-2 mb-2">
                <div className="col-6 col-md-3">
                  <input type="text" className="form-control form-control-sm" placeholder="Clave" value={form.clave}
                    onChange={(e) => setForm({ ...form, clave: e.target.value })} required disabled={!!editKey} />
                </div>
                <div className="col-6 col-md-3">
                  <input type="text" className="form-control form-control-sm" placeholder="Valor" value={form.valor}
                    onChange={(e) => setForm({ ...form, valor: e.target.value })} required />
                </div>
                <div className="col-6 col-md-3">
                  <input type="text" className="form-control form-control-sm" placeholder="Categoría" value={form.categoria}
                    onChange={(e) => setForm({ ...form, categoria: e.target.value })} />
                </div>
                <div className="col-6 col-md-3 d-flex gap-1">
                  <button type="submit" className="btn btn-sm btn-agro flex-grow-1" disabled={saving}>Guardar</button>
                  <button type="button" className="btn btn-sm btn-secondary" onClick={() => setShowForm(false)}>✕</button>
                </div>
              </div>
              <input type="text" className="form-control form-control-sm" placeholder="Descripción" value={form.descripcion}
                onChange={(e) => setForm({ ...form, descripcion: e.target.value })} />
            </form>
          </div>
        </div>
      )}

      {params.length === 0 ? (
        <EmptyState icon="⚙️" text="No hay parámetros configurados" />
      ) : (
        <div className="table-responsive table-agro">
          <table className="table table-hover mb-0">
            <thead><tr><th>Clave</th><th>Valor</th><th>Categoría</th><th>Descripción</th><th></th></tr></thead>
            <tbody>
              {params.map((p) => (
                <tr key={p.id}>
                  <td><code className="small">{p.clave}</code></td>
                  <td className="fw-semibold">{p.valor}</td>
                  <td><span className="badge bg-light text-dark border small">{p.categoria}</span></td>
                  <td className="text-muted small">{p.descripcion?.substring(0, 50)}</td>
                  <td>
                    <div className="d-flex gap-1">
                      <button className="btn btn-sm btn-agro-outline py-0 px-2" onClick={() => handleEdit(p)}><i className="bi bi-pencil"></i></button>
                      <button className="btn btn-sm btn-outline-danger py-0 px-2" onClick={() => setDeleteKey(p.clave)}><i className="bi bi-trash"></i></button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <ConfirmModal show={!!deleteKey} title="Eliminar parámetro"
        message={`¿Eliminar el parámetro "${deleteKey}"?`} onConfirm={handleDelete} onCancel={() => setDeleteKey(null)} />
    </div>
  );
}
