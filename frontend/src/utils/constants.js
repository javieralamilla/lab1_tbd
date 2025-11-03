/**
 * Constantes del sistema - Deben coincidir con los Enums del backend
 */

// Roles de usuario (Backend: Usuario.Rol)
export const ROLES = {
  ADMINISTRADOR: 'administrador',
  PLANIFICADOR: 'planificador',
  AUTORIDAD_MUNICIPAL: 'autoridad_municipal'
};

// Nombres legibles de roles
export const ROLES_DISPLAY = {
  [ROLES.ADMINISTRADOR]: 'Administrador',
  [ROLES.PLANIFICADOR]: 'Planificador',
  [ROLES.AUTORIDAD_MUNICIPAL]: 'Autoridad Municipal'
};

// Tipos de zona (Backend: ZonaUrbana.TipoZona)
export const TIPOS_ZONA = {
  RESIDENCIAL: 'Residencial',
  COMERCIAL: 'Comercial',
  INDUSTRIAL: 'Industrial',
  MIXTO: 'Mixto'
};

// Estados de proyecto (Backend: ProyectoUrbano.Estado)
export const ESTADOS_PROYECTO = {
  PLANEADO: 'Planeado',
  EN_CURSO: 'En Curso',
  COMPLETADO: 'Completado',
  RETRASADO: 'Retrasado',
  CANCELADO: 'Cancelado'
};

// Tipos de punto de interés (Backend: PuntoInteres.Tipo)
export const TIPOS_PUNTO_INTERES = {
  HOSPITAL: 'Hospital',
  ESCUELA: 'Escuela',
  PARQUE: 'Parque',
  CENTRO_COMERCIAL: 'Centro Comercial',
  TRANSPORTE: 'Transporte'
};

// Colores para los estados de proyectos
export const COLORES_ESTADO = {
  [ESTADOS_PROYECTO.PLANEADO]: {
    bg: '#fef3c7',
    border: '#f59e0b',
    text: '#92400e'
  },
  [ESTADOS_PROYECTO.EN_CURSO]: {
    bg: '#dbeafe',
    border: '#3b82f6',
    text: '#1e40af'
  },
  [ESTADOS_PROYECTO.COMPLETADO]: {
    bg: '#d1fae5',
    border: '#059669',
    text: '#065f46'
  },
  [ESTADOS_PROYECTO.RETRASADO]: {
    bg: '#fee',
    border: '#dc2626',
    text: '#991b1b'
  },
  [ESTADOS_PROYECTO.CANCELADO]: {
    bg: '#f3f4f6',
    border: '#6b7280',
    text: '#4b5563'
  }
};

// Colores para los tipos de zona
export const COLORES_TIPO_ZONA = {
  [TIPOS_ZONA.RESIDENCIAL]: {
    bg: '#dbeafe',
    text: '#1e40af'
  },
  [TIPOS_ZONA.COMERCIAL]: {
    bg: '#fef3c7',
    text: '#92400e'
  },
  [TIPOS_ZONA.INDUSTRIAL]: {
    bg: '#fee',
    text: '#991b1b'
  },
  [TIPOS_ZONA.MIXTO]: {
    bg: '#e0e7ff',
    text: '#5b21b6'
  }
};

// Colores para los tipos de punto de interés
export const COLORES_PUNTO_INTERES = {
  [TIPOS_PUNTO_INTERES.HOSPITAL]: '#dc2626',
  [TIPOS_PUNTO_INTERES.ESCUELA]: '#3b82f6',
  [TIPOS_PUNTO_INTERES.PARQUE]: '#059669',
  [TIPOS_PUNTO_INTERES.CENTRO_COMERCIAL]: '#f59e0b',
  [TIPOS_PUNTO_INTERES.TRANSPORTE]: '#8b5cf6'
};

// Configuración de la API
export const API_CONFIG = {
  TIMEOUT: 10000,
  BASE_URL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
};

// Mensajes de error comunes
export const ERROR_MESSAGES = {
  NETWORK_ERROR: 'Error de conexión. Verifica tu conexión a internet.',
  UNAUTHORIZED: 'No tienes permisos para realizar esta acción.',
  NOT_FOUND: 'El recurso solicitado no fue encontrado.',
  SERVER_ERROR: 'Error en el servidor. Inténtalo más tarde.',
  VALIDATION_ERROR: 'Los datos ingresados no son válidos.'
};

// Configuración de paginación
export const PAGINATION = {
  DEFAULT_PAGE_SIZE: 10,
  PAGE_SIZE_OPTIONS: [10, 25, 50, 100]
};
