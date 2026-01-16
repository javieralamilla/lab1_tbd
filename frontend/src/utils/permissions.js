// Utilidades para el manejo de permisos basado en roles

/**
 * Matriz de permisos por módulo y rol
 * Permisos: 'read', 'create', 'update', 'delete'
 */
export const PERMISSIONS_MATRIX = {
  // Dashboard - Todos tienen acceso completo
  dashboard: {
    administrador: ['read'],
    planificador: ['read'],
    autoridad_municipal: ['read']
  },

  // Zonas - Admin (CRUD), Planificador (editar), Autoridad (solo lectura)
  zonas: {
    administrador: ['read', 'create', 'update', 'delete'],
    planificador: ['read', 'update'],
    autoridad_municipal: ['read']
  },

  // Proyectos - Admin y Planificador (CRUD), Autoridad (solo lectura)
  proyectos: {
    administrador: ['read', 'create', 'update', 'delete'],
    planificador: ['read', 'create', 'update', 'delete'],
    autoridad_municipal: ['read']
  },

  // Puntos de Interés - Admin y Planificador (CRUD), Autoridad (solo lectura)
  puntosInteres: {
    administrador: ['read', 'create', 'update', 'delete'],
    planificador: ['read', 'create', 'update', 'delete'],
    autoridad_municipal: ['read']
  },

  // Datos Demográficos - Admin (CRUD), Planificador (editar), Autoridad (solo lectura)
  datosDemograficos: {
    administrador: ['read', 'create', 'update', 'delete'],
    planificador: ['read', 'update'],
    autoridad_municipal: ['read']
  },

  // Mapa Integrado - Todos tienen acceso completo
  mapaIntegrado: {
    administrador: ['read'],
    planificador: ['read'],
    autoridad_municipal: ['read']
  },

  // Análisis Espacial - Admin y Planificador completo, Autoridad (solo lectura)
  analisisEspacial: {
    administrador: ['read'],
    planificador: ['read'],
    autoridad_municipal: ['read']
  },

  // Simulación de Proyectos - Admin y Planificador completo, Autoridad (solo lectura)
  simulacionProyectos: {
    administrador: ['read'],
    planificador: ['read'],
    autoridad_municipal: ['read']
  },

  // Cobertura de Infraestructura - Admin y Planificador completo, Autoridad (solo lectura)
  coberturaInfraestructura: {
    administrador: ['read'],
    planificador: ['read'],
    autoridad_municipal: ['read']
  },

  // Reportes - Admin (gestión), Planificador (generar), Autoridad (ver/descargar)
  reportes: {
    administrador: ['read', 'create', 'update', 'delete'], // gestión completa
    planificador: ['read', 'create'], // generar reportes
    autoridad_municipal: ['read'] // ver y descargar
  }
};

/**
 * Verifica si un usuario tiene un permiso específico en un módulo
 * @param {string} module - Nombre del módulo
 * @param {string} permission - Tipo de permiso ('read', 'create', 'update', 'delete')
 * @param {string} userRole - Rol del usuario
 * @returns {boolean} - true si tiene permiso, false en caso contrario
 */
export function hasPermission(module, permission, userRole) {
  if (!userRole || !module || !permission) return false;
  
  const modulePermissions = PERMISSIONS_MATRIX[module];
  if (!modulePermissions) return false;
  
  const rolePermissions = modulePermissions[userRole];
  if (!rolePermissions) return false;
  
  return rolePermissions.includes(permission);
}

/**
 * Verifica si un usuario puede crear en un módulo
 */
export function canCreate(module, userRole) {
  return hasPermission(module, 'create', userRole);
}

/**
 * Verifica si un usuario puede leer/ver en un módulo
 */
export function canRead(module, userRole) {
  return hasPermission(module, 'read', userRole);
}

/**
 * Verifica si un usuario puede actualizar en un módulo
 */
export function canUpdate(module, userRole) {
  return hasPermission(module, 'update', userRole);
}

/**
 * Verifica si un usuario puede eliminar en un módulo
 */
export function canDelete(module, userRole) {
  return hasPermission(module, 'delete', userRole);
}

/**
 * Obtiene todos los permisos de un usuario para un módulo
 */
export function getModulePermissions(module, userRole) {
  if (!userRole || !module) return [];
  
  const modulePermissions = PERMISSIONS_MATRIX[module];
  if (!modulePermissions) return [];
  
  return modulePermissions[userRole] || [];
}

/**
 * Verifica si el usuario es de solo lectura en un módulo
 */
export function isReadOnly(module, userRole) {
  const permissions = getModulePermissions(module, userRole);
  return permissions.length === 1 && permissions[0] === 'read';
}

/**
 * Verifica si el usuario tiene acceso completo (CRUD) en un módulo
 */
export function hasFullAccess(module, userRole) {
  const permissions = getModulePermissions(module, userRole);
  return permissions.length === 4 && 
         permissions.includes('read') &&
         permissions.includes('create') &&
         permissions.includes('update') &&
         permissions.includes('delete');
}
