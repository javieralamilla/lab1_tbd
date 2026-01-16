// Composable para gestión de permisos en componentes Vue
import { computed } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { 
  hasPermission, 
  canCreate, 
  canRead, 
  canUpdate, 
  canDelete,
  getModulePermissions,
  isReadOnly,
  hasFullAccess
} from '@/utils/permissions';

/**
 * Composable para verificar permisos en componentes Vue
 * @param {string} module - Nombre del módulo
 */
export function usePermissions(module) {
  const authStore = useAuthStore();
  
  // Obtener el rol del usuario
  const userRole = computed(() => authStore.userRole);
  
  // Permisos específicos
  const permissions = computed(() => ({
    canCreate: canCreate(module, userRole.value),
    canRead: canRead(module, userRole.value),
    canUpdate: canUpdate(module, userRole.value),
    canDelete: canDelete(module, userRole.value),
    isReadOnly: isReadOnly(module, userRole.value),
    hasFullAccess: hasFullAccess(module, userRole.value),
    all: getModulePermissions(module, userRole.value)
  }));
  
  // Función para verificar un permiso específico
  const checkPermission = (permission) => {
    return hasPermission(module, permission, userRole.value);
  };
  
  // Información del usuario
  const user = computed(() => ({
    role: userRole.value,
    isAdmin: authStore.isAdmin,
    isPlanificador: authStore.isPlanificador,
    isAutoridadMunicipal: authStore.isAutoridadMunicipal,
    name: authStore.userName
  }));
  
  return {
    permissions,
    checkPermission,
    user,
    userRole
  };
}

/**
 * Directiva v-permission para mostrar/ocultar elementos basado en permisos
 * Uso: v-permission="{ module: 'zonas', action: 'create' }"
 */
export const vPermission = {
  mounted(el, binding) {
    const { module, action } = binding.value;
    const authStore = useAuthStore();
    
    if (!hasPermission(module, action, authStore.userRole)) {
      el.style.display = 'none';
    }
  },
  updated(el, binding) {
    const { module, action } = binding.value;
    const authStore = useAuthStore();
    
    if (!hasPermission(module, action, authStore.userRole)) {
      el.style.display = 'none';
    } else {
      el.style.display = '';
    }
  }
};
