import api from '../api/axiosConfig';

export const authService = {
  login: (email, password) => api.post('/auth/login', { email, password }),
  register: (data) => api.post('/auth/register', data),
};

export const fincaService = {
  listar: (page = 0, size = 10) => api.get(`/fincas?page=${page}&size=${size}`),
  listarPorProductor: (id, page = 0) => api.get(`/fincas/productor/${id}?page=${page}`),
  obtener: (id) => api.get(`/fincas/${id}`),
  crear: (productorId, data) => api.post(`/fincas/productor/${productorId}`, data),
  actualizar: (id, data) => api.put(`/fincas/${id}`, data),
  eliminar: (id) => api.delete(`/fincas/${id}`),
  buscarPorMunicipio: (municipio) => api.get(`/fincas/municipio/${municipio}`),
};

export const parcelaService = {
  listarPorFinca: (fincaId, page = 0) => api.get(`/parcelas/finca/${fincaId}?page=${page}`),
  obtener: (id) => api.get(`/parcelas/${id}`),
  crear: (data) => api.post('/parcelas', data),
  actualizar: (id, data) => api.put(`/parcelas/${id}`, data),
  eliminar: (id) => api.delete(`/parcelas/${id}`),
};

export const cultivoService = {
  listar: (page = 0) => api.get(`/cultivos?page=${page}`),
  listarPorProductor: (id, page = 0) => api.get(`/cultivos/productor/${id}?page=${page}`),
  listarPorFinca: (id, page = 0) => api.get(`/cultivos/finca/${id}?page=${page}`),
  listarPorParcela: (id, page = 0) => api.get(`/cultivos/parcela/${id}?page=${page}`),
  obtener: (id) => api.get(`/cultivos/${id}`),
  crear: (data) => api.post('/cultivos', data),
  actualizar: (id, data) => api.put(`/cultivos/${id}`, data),
  eliminar: (id) => api.delete(`/cultivos/${id}`),
};

export const recomendacionService = {
  listarPorCultivo: (id, page = 0) => api.get(`/recomendaciones/cultivo/${id}?page=${page}`),
  listarPorProductor: (id, page = 0) => api.get(`/recomendaciones/productor/${id}?page=${page}`),
  listarPendientes: (page = 0) => api.get(`/recomendaciones/pendientes?page=${page}`),
  obtener: (id) => api.get(`/recomendaciones/${id}`),
  crear: (data) => api.post('/recomendaciones', data),
  aplicar: (id, obs) => api.patch(`/recomendaciones/${id}/aplicar?observaciones=${obs || ''}`),
};

export const alertaService = {
  listarActivas: (page = 0) => api.get(`/alertas?page=${page}`),
  listarHistorial: (page = 0) => api.get(`/alertas/historial?page=${page}`),
  listarPorMunicipio: (municipio) => api.get(`/alertas/municipio/${municipio}`),
  obtener: (id) => api.get(`/alertas/${id}`),
  crear: (data) => api.post('/alertas', data),
  desactivar: (id) => api.patch(`/alertas/${id}/desactivar`),
};

export const reporteService = {
  listarPorProductor: (id, page = 0) => api.get(`/reportes/productor/${id}?page=${page}`),
  obtener: (id) => api.get(`/reportes/${id}`),
  generar: (productorId, data) => api.post(`/reportes/productor/${productorId}`, data),
  /* Exports (CSV) - Retornan el contenido en texto */
  exportarProduccionCsv: (productorId) => api.get(`/reportes/produccion-csv${productorId ? '?productorId='+productorId : ''}`, { responseType: 'blob' }),
  exportarInventarioCsv: (fincaId) => api.get(`/reportes/inventario-cultivos-csv${fincaId ? '?fincaId='+fincaId : ''}`, { responseType: 'blob' }),
  exportarAlertasCsv: () => api.get(`/reportes/alertas-historial-csv`, { responseType: 'blob' }),
};

export const dashboardService = {
  getProductorStats: () => api.get('/dashboard/productor'),
  getAsociacionStats: () => api.get('/dashboard/asociacion'),
  getAdminStats: () => api.get('/dashboard/admin'),
};

export const parametroService = {
  listar: () => api.get('/parametros-tecnicos'),
  obtener: (clave) => api.get(`/parametros-tecnicos/${clave}`),
  crear: (data) => api.post('/parametros-tecnicos', data),
  actualizar: (clave, data) => api.put(`/parametros-tecnicos/${clave}`, data),
  eliminar: (clave) => api.delete(`/parametros-tecnicos/${clave}`),
};

export const syncService = {
  push: (data) => api.post('/sync/push', data),
  pushBatch: (items) => api.post('/sync/push-batch', items),
  listarPendientes: (userId) => api.get(`/sync/pending/${userId}`),
  procesar: (userId) => api.post(`/sync/process/${userId}`),
};
