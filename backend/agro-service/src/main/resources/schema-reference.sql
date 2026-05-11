-- ============================================================================
-- AgroSmart Magdalena — Schema Reference (documentación)
-- Este archivo es solo documentación de referencia del esquema.
-- NO debe usarse como script de inicialización automático de la base de datos.
-- Utilice migraciones gestionadas (Flyway/Liquibase) para cambios en producción.
-- ============================================================================

-- Nota: Con ddl-auto=update, Hibernate puede generar tablas automáticamente.
-- Este archivo documenta el esquema para referencia y migraciones futuras.

-- Roles del sistema
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL UNIQUE
);

-- Usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    nombre_completo VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(120) NOT NULL,
    telefono VARCHAR(20),
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Relación usuarios-roles (many-to-many)
CREATE TABLE IF NOT EXISTS usuario_roles (
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    rol_id BIGINT NOT NULL REFERENCES roles(id),
    PRIMARY KEY (usuario_id, rol_id)
);

-- Productores
CREATE TABLE IF NOT EXISTS productores (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL UNIQUE REFERENCES usuarios(id),
    cedula VARCHAR(20) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    direccion VARCHAR(200),
    asociacion_id BIGINT,
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Administradores
CREATE TABLE IF NOT EXISTS administradores (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL UNIQUE REFERENCES usuarios(id),
    cargo VARCHAR(100) NOT NULL,
    departamento VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Técnicos agropecuarios
CREATE TABLE IF NOT EXISTS tecnicos_agropecuarios (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL UNIQUE REFERENCES usuarios(id),
    especialidad VARCHAR(100) NOT NULL,
    numero_registro VARCHAR(20),
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Zonas asignadas a técnicos
CREATE TABLE IF NOT EXISTS tecnico_zonas (
    tecnico_id BIGINT NOT NULL REFERENCES tecnicos_agropecuarios(id),
    zona VARCHAR(255)
);

-- Asociaciones agrícolas
CREATE TABLE IF NOT EXISTS asociaciones_agricolas (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    nit VARCHAR(20) UNIQUE,
    municipio VARCHAR(100),
    representante_legal VARCHAR(100),
    telefono VARCHAR(20),
    direccion VARCHAR(200),
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Ubicaciones geográficas
CREATE TABLE IF NOT EXISTS ubicaciones_geograficas (
    id BIGSERIAL PRIMARY KEY,
    latitud DOUBLE PRECISION,
    longitud DOUBLE PRECISION,
    vereda VARCHAR(100),
    municipio VARCHAR(100) NOT NULL,
    departamento VARCHAR(100) NOT NULL DEFAULT 'Magdalena',
    referencia_adicional VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Fincas
CREATE TABLE IF NOT EXISTS fincas (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    productor_id BIGINT NOT NULL REFERENCES productores(id),
    ubicacion_id BIGINT NOT NULL REFERENCES ubicaciones_geograficas(id),
    area_total DOUBLE PRECISION NOT NULL,
    unidad_area VARCHAR(20) DEFAULT 'hectáreas',
    descripcion VARCHAR(500),
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Parcelas/Lotes
CREATE TABLE IF NOT EXISTS parcelas (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    finca_id BIGINT NOT NULL REFERENCES fincas(id),
    area_parcela DOUBLE PRECISION NOT NULL,
    unidad_area VARCHAR(20) DEFAULT 'hectáreas',
    tipo_suelo VARCHAR(50),
    descripcion VARCHAR(500),
    estado VARCHAR(20) NOT NULL DEFAULT 'DISPONIBLE',
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Cultivos
CREATE TABLE IF NOT EXISTS cultivos (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    variedad VARCHAR(100),
    parcela_id BIGINT NOT NULL REFERENCES parcelas(id),
    fecha_siembra DATE NOT NULL,
    fecha_cosecha_estimada DATE,
    fecha_cosecha_real DATE,
    estado VARCHAR(20) NOT NULL DEFAULT 'PLANIFICADO',
    area_utilizada DOUBLE PRECISION,
    observaciones VARCHAR(500),
    rendimiento_esperado DOUBLE PRECISION,
    rendimiento_real DOUBLE PRECISION,
    unidad_rendimiento VARCHAR(30) DEFAULT 'toneladas/hectárea',
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Recomendaciones técnicas
CREATE TABLE IF NOT EXISTS recomendaciones (
    id BIGSERIAL PRIMARY KEY,
    cultivo_id BIGINT NOT NULL REFERENCES cultivos(id),
    tecnico_id BIGINT REFERENCES tecnicos_agropecuarios(id),
    titulo VARCHAR(200) NOT NULL,
    descripcion TEXT NOT NULL,
    fecha_emision TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    prioridad VARCHAR(10) NOT NULL DEFAULT 'MEDIA',
    aplicada BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Historial de recomendaciones
CREATE TABLE IF NOT EXISTS historial_recomendaciones (
    id BIGSERIAL PRIMARY KEY,
    recomendacion_id BIGINT NOT NULL REFERENCES recomendaciones(id),
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observaciones TEXT,
    aplicada BOOLEAN NOT NULL DEFAULT false,
    registrado_por VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Alertas climáticas
CREATE TABLE IF NOT EXISTS alertas_climaticas (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    descripcion TEXT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    fecha_emision TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_expiracion TIMESTAMP,
    emitida_por BIGINT REFERENCES usuarios(id),
    activa BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Municipios afectados por alertas
CREATE TABLE IF NOT EXISTS alerta_municipios (
    alerta_id BIGINT NOT NULL REFERENCES alertas_climaticas(id),
    municipio VARCHAR(255)
);

-- Reportes
CREATE TABLE IF NOT EXISTS reportes (
    id BIGSERIAL PRIMARY KEY,
    productor_id BIGINT NOT NULL REFERENCES productores(id),
    tipo VARCHAR(20) NOT NULL,
    titulo VARCHAR(200),
    fecha_generacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    contenido TEXT,
    periodo_inicio DATE,
    periodo_fin DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Parámetros técnicos
CREATE TABLE IF NOT EXISTS parametros_tecnicos (
    id BIGSERIAL PRIMARY KEY,
    clave VARCHAR(100) NOT NULL UNIQUE,
    valor TEXT NOT NULL,
    descripcion VARCHAR(300),
    categoria VARCHAR(50),
    modificado_por BIGINT REFERENCES usuarios(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Sincronización offline
CREATE TABLE IF NOT EXISTS sincronizaciones_offline (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    entidad VARCHAR(50) NOT NULL,
    accion VARCHAR(10) NOT NULL,
    datos_json TEXT NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha_sincronizacion TIMESTAMP,
    mensaje_error TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Índices de Integridad y Rendimiento (Refuerzo académico)
CREATE INDEX IF NOT EXISTS idx_finca_productor ON fincas(productor_id);
CREATE INDEX IF NOT EXISTS idx_finca_activo ON fincas(activo);
CREATE INDEX IF NOT EXISTS idx_parcela_finca ON parcelas(finca_id);
CREATE INDEX IF NOT EXISTS idx_parcela_activo ON parcelas(activo);
CREATE INDEX IF NOT EXISTS idx_cultivo_parcela ON cultivos(parcela_id);
CREATE INDEX IF NOT EXISTS idx_cultivo_estado ON cultivos(estado);
CREATE INDEX IF NOT EXISTS idx_cultivo_activo ON cultivos(activo);
CREATE INDEX IF NOT EXISTS idx_alerta_municipio ON alerta_municipios(municipio);
CREATE INDEX IF NOT EXISTS idx_sync_estado ON sincronizaciones_offline(estado);
