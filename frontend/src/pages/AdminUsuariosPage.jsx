import { useEffect, useMemo, useState } from 'react';
import { adminUserService } from '../services/apiServices';
import { ConfirmModal, EmptyState, LoadingSpinner } from '../components/UIComponents';
import { useToast } from '../context/ToastContext';

const ROLE_OPTIONS = ['TODOS', 'ADMIN', 'TECNICO', 'AGRICULTOR'];
const STATE_OPTIONS = ['TODOS', 'ACTIVO', 'BLOQUEADO', 'INACTIVO'];

function normalizarRol(rol) {
  const value = (rol || '').toUpperCase();
  if (!value) return 'SIN_ROL';
  if (value.includes('ADMIN')) return 'ADMIN';
  if (value.includes('TECNICO')) return 'TECNICO';
  if (value.includes('AGRICULTOR') || value.includes('PRODUCTOR')) return 'AGRICULTOR';
  return value.replace('ROLE_', '');
}

function badgeRolClass(rol) {
  if (rol === 'ADMIN') return { background: '#6f42c1', color: '#fff' };
  if (rol === 'TECNICO') return { background: '#0d6efd', color: '#fff' };
  return { background: '#198754', color: '#fff' };
}

function badgeEstadoClass(estado) {
  if (estado === 'ACTIVO') return 'bg-success';
  if (estado === 'BLOQUEADO') return 'bg-warning text-dark';
  return 'bg-secondary';
}

export default function AdminUsuariosPage() {
  const { addToast } = useToast();
  const [usuarios, setUsuarios] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [roleFilter, setRoleFilter] = useState('TODOS');
  const [stateFilter, setStateFilter] = useState('TODOS');
  const [confirmAction, setConfirmAction] = useState(null);
  const [detailUser, setDetailUser] = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);

  const cargarUsuarios = async () => {
    setLoading(true);
    try {
      const res = await adminUserService.listarUsuarios();
      setUsuarios(res.data?.datos || []);
    } catch (error) {
      addToast(error.response?.data?.mensaje || 'No se pudo cargar la lista de usuarios', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarUsuarios();
  }, []);

  const usuariosFiltrados = useMemo(() => {
    const term = search.trim().toLowerCase();
    return usuarios.filter((usuario) => {
      const rolNormalizado = normalizarRol(usuario.rol);
      const estadoNormalizado = (usuario.estado || (usuario.activo ? 'ACTIVO' : 'INACTIVO')).toUpperCase();
      const coincideBusqueda = !term || [usuario.nombreCompleto, usuario.email].some((v) => (v || '').toLowerCase().includes(term));
      const coincideRol = roleFilter === 'TODOS' || rolNormalizado === roleFilter;
      const coincideEstado = stateFilter === 'TODOS' || estadoNormalizado === stateFilter;
      return coincideBusqueda && coincideRol && coincideEstado;
    });
  }, [usuarios, search, roleFilter, stateFilter]);

  const openDetail = async (id) => {
    setDetailLoading(true);
    setDetailUser({ loading: true });
    try {
      const res = await adminUserService.obtenerUsuario(id);
      setDetailUser(res.data?.datos || null);
    } catch (error) {
      addToast(error.response?.data?.mensaje || 'No se pudo obtener el detalle del usuario', 'error');
      setDetailUser(null);
    } finally {
      setDetailLoading(false);
    }
  };

  const executeAction = async () => {
    if (!confirmAction) return;
    const { type, usuario } = confirmAction;
    try {
      if (type === 'bloquear') {
        await adminUserService.bloquearUsuario(usuario.id);
        addToast('Usuario bloqueado', 'success');
      }
      if (type === 'desbloquear') {
        await adminUserService.desbloquearUsuario(usuario.id);
        addToast('Usuario desbloqueado', 'success');
      }
      if (type === 'eliminar') {
        await adminUserService.eliminarUsuario(usuario.id);
        addToast('Usuario desactivado', 'success');
      }
      setConfirmAction(null);
      await cargarUsuarios();
      if (detailUser?.id === usuario.id) {
        setDetailUser(null);
      }
    } catch (error) {
      addToast(error.response?.data?.mensaje || 'No se pudo completar la acción', 'error');
    }
  };

  const estadoUsuario = (usuario) => (usuario.estado || (usuario.activo ? 'ACTIVO' : 'INACTIVO')).toUpperCase();
  const rolUsuario = (usuario) => normalizarRol(usuario.rol);

  if (loading) return <LoadingSpinner text="Cargando gestión de usuarios..." />;

  return (
    <div className="content-wrapper">
      <div className="mb-4 d-flex flex-wrap align-items-center justify-content-between gap-2">
        <div>
          <h2 className="fw-bold mb-1">Gestión de Usuarios</h2>
          <p className="text-muted mb-0">Administra usuarios, estados y accesos del sistema</p>
        </div>
        <button className="btn btn-outline-secondary" onClick={cargarUsuarios}>
          <i className="bi bi-arrow-clockwise me-1"></i>Actualizar
        </button>
      </div>

      <div className="card card-agro mb-3">
        <div className="card-body">
          <div className="row g-2">
            <div className="col-12 col-lg-5">
              <div className="input-group">
                <span className="input-group-text bg-white"><i className="bi bi-search"></i></span>
                <input
                  type="search"
                  className="form-control"
                  placeholder="Buscar por nombre o email"
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                />
              </div>
            </div>
            <div className="col-6 col-lg-3">
              <select className="form-select" value={roleFilter} onChange={(e) => setRoleFilter(e.target.value)}>
                {ROLE_OPTIONS.map((option) => <option key={option} value={option}>{option}</option>)}
              </select>
            </div>
            <div className="col-6 col-lg-3">
              <select className="form-select" value={stateFilter} onChange={(e) => setStateFilter(e.target.value)}>
                {STATE_OPTIONS.map((option) => <option key={option} value={option}>{option}</option>)}
              </select>
            </div>
          </div>
        </div>
      </div>

      <div className="card card-agro">
        <div className="card-body p-0">
          {usuariosFiltrados.length === 0 ? (
            <div className="p-4">
              <EmptyState icon="👤" text="No hay usuarios que coincidan con los filtros" />
            </div>
          ) : (
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light">
                  <tr>
                    <th>Nombre</th>
                    <th>Email</th>
                    <th>Rol</th>
                    <th>Teléfono</th>
                    <th>Estado</th>
                    <th className="text-end">Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {usuariosFiltrados.map((usuario) => {
                    const rol = rolUsuario(usuario);
                    const estado = estadoUsuario(usuario);
                    return (
                      <tr key={usuario.id}>
                        <td className="fw-semibold">{usuario.nombreCompleto}</td>
                        <td>{usuario.email}</td>
                        <td>
                          <span className="badge rounded-pill" style={badgeRolClass(rol)}>
                            {rol === 'AGRICULTOR' ? 'AGRICULTOR' : rol}
                          </span>
                        </td>
                        <td>{usuario.telefono || '-'}</td>
                        <td>
                          <span className={`badge rounded-pill ${badgeEstadoClass(estado)}`}>
                            {estado}
                          </span>
                        </td>
                        <td className="text-end">
                          <div className="btn-group btn-group-sm flex-wrap" role="group">
                            <button className="btn btn-outline-primary" onClick={() => openDetail(usuario.id)}>
                              Ver detalle
                            </button>
                            {estado === 'ACTIVO' && (
                              <button
                                className="btn btn-outline-warning"
                                onClick={() => setConfirmAction({ type: 'bloquear', usuario })}
                              >
                                Bloquear
                              </button>
                            )}
                            {estado === 'BLOQUEADO' && (
                              <button
                                className="btn btn-outline-success"
                                onClick={() => setConfirmAction({ type: 'desbloquear', usuario })}
                              >
                                Desbloquear
                              </button>
                            )}
                            {estado !== 'INACTIVO' && (
                              <button
                                className="btn btn-outline-danger"
                                onClick={() => setConfirmAction({ type: 'eliminar', usuario })}
                              >
                                Eliminar
                              </button>
                            )}
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      <ConfirmModal
        show={!!confirmAction}
        title={confirmAction?.type === 'bloquear' ? 'Bloquear usuario' : confirmAction?.type === 'desbloquear' ? 'Desbloquear usuario' : 'Eliminar usuario'}
        message={
          confirmAction?.type === 'bloquear'
            ? '¿Seguro que deseas bloquear este usuario?'
            : confirmAction?.type === 'desbloquear'
              ? '¿Seguro que deseas desbloquear este usuario?'
              : '¿Seguro que deseas eliminar/desactivar este usuario?'
        }
        onConfirm={executeAction}
        onCancel={() => setConfirmAction(null)}
      />

      {detailUser && (
        <div className="modal d-block" style={{ background: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content border-0 shadow">
              <div className="modal-header border-0">
                <h5 className="modal-title fw-bold">Detalle de usuario</h5>
                <button type="button" className="btn-close" onClick={() => setDetailUser(null)}></button>
              </div>
              <div className="modal-body">
                {detailLoading || detailUser.loading ? (
                  <div className="text-center py-4">
                    <LoadingSpinner text="Cargando detalle..." />
                  </div>
                ) : (
                  <div className="row g-3 small">
                    <div className="col-12"><strong>Nombre:</strong> {detailUser.nombreCompleto}</div>
                    <div className="col-12"><strong>Email:</strong> {detailUser.email}</div>
                    <div className="col-6"><strong>Rol:</strong> {normalizarRol(detailUser.rol)}</div>
                    <div className="col-6"><strong>Estado:</strong> {estadoUsuario(detailUser)}</div>
                    <div className="col-6"><strong>Teléfono:</strong> {detailUser.telefono || '-'}</div>
                    <div className="col-6"><strong>Activo:</strong> {detailUser.activo ? 'Sí' : 'No'}</div>
                    <div className="col-12"><strong>Creado:</strong> {detailUser.fechaCreacion ? new Date(detailUser.fechaCreacion).toLocaleString() : '-'}</div>
                  </div>
                )}
              </div>
              <div className="modal-footer border-0">
                <button className="btn btn-secondary" onClick={() => setDetailUser(null)}>Cerrar</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
