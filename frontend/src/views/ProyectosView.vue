<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1>Proyectos Urbanos</h1>
        <p>Gestión de proyectos de desarrollo de la ciudad</p>
      </div>
      <div class="header-actions">
        <button
          v-if="authStore.isAdmin || authStore.isPlanificador"
          @click="createProject"
          class="btn-primary"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="12" y1="5" x2="12" y2="19"></line>
            <line x1="5" y1="12" x2="19" y2="12"></line>
          </svg>
          Nuevo Proyecto
        </button>

        <button
          v-if="authStore.isAdmin || authStore.isPlanificador"
          @click="abrirActualizarRetrasadosModal"
          class="btn-actualizar-retrasados"
          title="Actualizar estado de proyectos retrasados"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"></circle>
            <polyline points="12 6 12 12 16 14"></polyline>
          </svg>
          ⏱️ Actualizar Retrasados
        </button>
      </div>
    </div>

    <!-- Modal / Panel Nuevo Proyecto -->
    <div v-if="showCreateModal" class="modal-overlay">
      <div class="modal-panel">
        <div class="modal-header">
          <h3>➕ Nuevo Proyecto</h3>
          <button class="btn-cerrar" @click="cancelCreate">✖</button>
        </div>

        <div class="modal-body">
          <div class="form-group">
            <label>Nombre del Proyecto</label>
            <input v-model="newProject.nombre" type="text" placeholder="Nombre" />
          </div>

          <div class="form-group">
            <label>Tipo de Proyecto</label>
            <select v-model="newProject.tipo_proyecto">
              <option value="RESIDENCIAL">Residencial</option>
              <option value="COMERCIAL">Comercial</option>
              <option value="INDUSTRIAL">Industrial</option>
              <option value="EDUCATIVO">Educativo</option>
              <option value="SALUD">Salud</option>
              <option value="RECREATIVO">Recreativo</option>
              <option value="TRANSPORTE">Transporte</option>
            </select>
          </div>

          <div class="form-group">
            <label>Fecha de Inicio</label>
            <input v-model="newProject.fecha_inicio" type="date" />
          </div>

          <div class="form-group">
            <label>Fecha de Término (Opcional)</label>
            <input v-model="newProject.fecha_termino" type="date" />
          </div>

          <div class="form-group">
            <label>Presupuesto (CLP)</label>
            <input v-model.number="newProject.presupuesto" type="number" min="0" />
          </div>

          <div class="form-group">
            <label>Descripción</label>
            <textarea v-model="newProject.descripcion" rows="3" placeholder="Descripción breve"></textarea>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn-guardar" @click="saveProject" :disabled="creating">Guardar Proyecto</button>
          <button class="btn-cancel" @click="cancelCreate" :disabled="creating">Cancelar</button>
        </div>
      </div>
    </div>

    <!-- Mapa de dibujo para nuevo proyecto -->
    <div v-if="showMapDraw" class="modal-overlay">
      <div class="modal-panel" style="width:90%; max-width:1100px; padding:0;">
        <MapaNuevoProyecto @area-drawn="onAreaDrawn" @cancel="cancelMapDraw" />
      </div>
    </div>

    <!-- Modal / Panel Editar Proyecto -->
    <div v-if="showEditModal" class="modal-overlay">
      <div class="modal-panel">
        <div class="modal-header">
          <h3>✏️ Editar Proyecto</h3>
          <button class="btn-cerrar" @click="cancelEdit">✖</button>
        </div>

        <div class="modal-body">
          <div class="form-group">
            <label>Nombre del Proyecto</label>
            <input v-model="editProjectData.nombre" type="text" placeholder="Nombre" />
          </div>

          <div class="form-group">
            <label>Tipo de Proyecto</label>
            <select v-model="editProjectData.tipo_proyecto">
              <option value="RESIDENCIAL">Residencial</option>
              <option value="COMERCIAL">Comercial</option>
              <option value="INDUSTRIAL">Industrial</option>
              <option value="EDUCATIVO">Educativo</option>
              <option value="SALUD">Salud</option>
              <option value="RECREATIVO">Recreativo</option>
              <option value="TRANSPORTE">Transporte</option>
            </select>
          </div>

          <div class="form-group">
            <label>Fecha de Inicio</label>
            <input v-model="editProjectData.fecha_inicio" type="date" />
          </div>

          <div class="form-group">
            <label>Fecha de Término (Opcional)</label>
            <input v-model="editProjectData.fecha_termino" type="date" />
          </div>

          <div class="form-group">
            <label>Presupuesto (CLP)</label>
            <input v-model.number="editProjectData.presupuesto" type="number" min="0" />
          </div>

          <div class="form-group">
            <label>Estado</label>
            <select v-model="editProjectData.estado">
              <option value="Planeado">Planeado</option>
              <option value="En Curso">En Curso</option>
              <option value="Completado">Completado</option>
              <option value="Retrasado">Retrasado</option>
              <option value="Cancelado">Cancelado</option>
            </select>
          </div>

          <div class="form-group">
            <label>Descripción</label>
            <textarea v-model="editProjectData.descripcion" rows="3" placeholder="Descripción breve"></textarea>
          </div>

          <div class="form-group">
            <label>Geometría (GeoJSON)</label>
            <textarea v-model="editProjectData.geometria" rows="4" placeholder='GeoJSON como texto (ej: {"type":"Polygon",...})'></textarea>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn-guardar" @click="saveEdit" :disabled="editing">Guardar Cambios</button>
          <button class="btn-cancel" @click="cancelEdit" :disabled="editing">Cancelar</button>
        </div>
      </div>
    </div>

    <!-- Modal Análisis de Superposición -->
    <div v-if="showSuperposicionModal" class="modal-overlay">
      <div class="modal-panel modal-wide">
        <div class="modal-header">
          <h3>Análisis de Superposición de Proyectos</h3>
          <button class="btn-cerrar" @click="closeSuperposicionModal">✖</button>
        </div>

        <div class="modal-body">
          <div v-if="loadingSuperposiciones" class="loading-section">
            <LoadingSpinner message="Analizando superposiciones..." />
          </div>

          <div v-else-if="superposiciones.length === 0" class="empty-state">
            <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
              <circle cx="12" cy="12" r="10"></circle>
              <path d="M12 6v6l4 2"></path>
            </svg>
            <p>No se encontraron proyectos con superposiciones geográficas.</p>
          </div>

          <div v-else class="superposiciones-list">
            <div class="superposicion-stats">
              <div class="stat-card">
                <div class="stat-value">{{ superposiciones.length }}</div>
                <div class="stat-label">Superposiciones detectadas</div>
              </div>
              <div class="stat-card">
                <div class="stat-value">{{ formatArea(totalAreaSuperposicion) }}</div>
                <div class="stat-label">Área total superpuesta</div>
              </div>
            </div>

            <div class="table-container">
              <table class="superposiciones-table">
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Proyecto 1</th>
                    <th>Estado</th>
                    <th>Proyecto 2</th>
                    <th>Estado</th>
                    <th>Área Superposición</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(sup, index) in superposiciones" :key="index" :class="{'critical-overlap': sup.area_superposicion_m2 > 1000}">
                    <td>{{ index + 1 }}</td>
                    <td class="proyecto-name">{{ sup.proyecto_1 }}</td>
                    <td>
                      <span class="status-badge-small" :class="getStatusClass(sup.estado_proyecto_1)">
                        {{ sup.estado_proyecto_1 }}
                      </span>
                    </td>
                    <td class="proyecto-name">{{ sup.proyecto_2 }}</td>
                    <td>
                      <span class="status-badge-small" :class="getStatusClass(sup.estado_proyecto_2)">
                        {{ sup.estado_proyecto_2 }}
                      </span>
                    </td>
                    <td class="area-cell">
                      <strong>{{ formatArea(sup.area_superposicion_m2) }}</strong>
                      <span v-if="sup.area_superposicion_m2 > 1000" class="warning-badge">Crítico</span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn-cancel" @click="closeSuperposicionModal">Cerrar</button>
        </div>
      </div>
    </div>

    <!-- Modal Zonas Sin Planificación Reciente -->
    <div v-if="showZonasSinPlanModal" class="modal-overlay">
      <div class="modal-panel modal-wide">
        <div class="modal-header">
          <h3>Zonas Sin Planificación Reciente</h3>
          <button class="btn-cerrar" @click="closeZonasSinPlanModal">✖</button>
        </div>

        <div class="modal-body">
          <div v-if="loadingZonasSinPlan" class="loading-section">
            <LoadingSpinner message="Analizando zonas..." />
          </div>

          <div v-else-if="zonasSinPlanificacion.length === 0" class="empty-state">
            <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
              <circle cx="12" cy="10" r="3"></circle>
            </svg>
            <p>Todas las zonas tienen proyectos recientes (últimos 2 años).</p>
          </div>

          <div v-else class="zonas-list">
            <div class="zonas-stats">
              <div class="stat-card">
                <div class="stat-value">{{ zonasSinPlanificacion.length }}</div>
                <div class="stat-label">Zonas sin planificación</div>
              </div>
              <div class="stat-card">
                <div class="stat-value">{{ zonasNuncaTuvieron }}</div>
                <div class="stat-label">Nunca tuvieron proyectos</div>
              </div>
            </div>

            <div class="table-container">
              <table class="zonas-table">
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Zona</th>
                    <th>Tipo</th>
                    <th>Último Proyecto</th>
                    <th>Años Sin Proyecto</th>
                    <th>Estado</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(zona, index) in zonasSinPlanificacion" :key="index" :class="{'nunca-proyecto': zona.ultimo_proyecto === 'Ninguno'}">
                    <td>{{ index + 1 }}</td>
                    <td class="zona-name">{{ zona.zona }}</td>
                    <td>
                      <span class="tipo-badge">{{ zona.tipo_zona }}</span>
                    </td>
                    <td>
                      <span :class="{'sin-proyecto': zona.ultimo_proyecto === 'Ninguno'}">
                        {{ zona.ultimo_proyecto }}
                      </span>
                    </td>
                    <td>
                      <span v-if="zona.anios_sin_proyecto !== null" class="anios-badge">
                        {{ zona.anios_sin_proyecto }} año{{ zona.anios_sin_proyecto !== 1 ? 's' : '' }}
                      </span>
                      <span v-else class="anios-badge nunca">-</span>
                    </td>
                    <td>
                      <span class="estado-badge" :class="zona.estado_planificacion.toLowerCase().replace(/ /g, '-')">
                        {{ zona.estado_planificacion }}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn-cancel" @click="closeZonasSinPlanModal">Cerrar</button>
        </div>
      </div>
    </div>

    <!-- Modal Actualizar Proyectos Retrasados -->
    <div v-if="showActualizarRetrasadosModal" class="modal-overlay">
      <div class="modal-panel modal-medium">
        <div class="modal-header">
          <h3>⏱️ Actualizar Proyectos Retrasados</h3>
          <button class="btn-cerrar" @click="closeActualizarRetrasadosModal">✖</button>
        </div>

        <div class="modal-body">
          <div v-if="actualizandoRetrasados" class="loading-section">
            <LoadingSpinner message="Actualizando proyectos..." />
          </div>

          <div v-else-if="!resultadoActualizacion" class="confirmacion-section">
            <div class="icono-advertencia">
              <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="8" x2="12" y2="12"></line>
                <line x1="12" y1="16" x2="12.01" y2="16"></line>
              </svg>
            </div>
            <h4>¿Actualizar proyectos retrasados?</h4>
            <p>Esta acción actualizará el estado de todos tus proyectos "En Curso" que hayan superado su fecha límite, marcándolos como "Retrasado".</p>

            <!-- Contador de proyectos a actualizar -->
            <div v-if="contarProyectosRetrasados > 0" class="alerta-proyectos">
              <div class="numero-proyectos">{{ contarProyectosRetrasados }}</div>
              <p><strong>proyecto{{ contarProyectosRetrasados !== 1 ? 's' : '' }} será{{ contarProyectosRetrasados !== 1 ? 'n' : '' }} actualizado{{ contarProyectosRetrasados !== 1 ? 's' : '' }}</strong></p>
            </div>
            <div v-else class="alerta-proyectos sin-proyectos">
              <p>No hay proyectos retrasados para actualizar</p>
            </div>

            <div class="info-box">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="16" x2="12" y2="12"></line>
                <line x1="12" y1="8" x2="12.01" y2="8"></line>
              </svg>
              <div>
                <strong>Criterios de actualización:</strong>
                <ul>
                  <li>Estado actual: "En Curso"</li>
                  <li>Fecha de término menor a hoy ({{ formatDate(new Date()) }})</li>
                  <li>Solo tus proyectos</li>
                </ul>
              </div>
            </div>
          </div>

          <div v-else class="resultado-section">
            <div class="icono-exito">
              <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                <polyline points="22 4 12 14.01 9 11.01"></polyline>
              </svg>
            </div>
            <h4>Actualización Completada</h4>
            <p>{{ resultadoActualizacion }}</p>
            <div class="success-stats">
              <div class="stat-item">
                <div class="stat-icon"></div>
                <div>
                  <div class="stat-number">{{ proyectosActualizadosCount }}</div>
                  <div class="stat-label">Proyectos actualizados</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button
            v-if="!resultadoActualizacion"
            class="btn-primary"
            @click="confirmarActualizarRetrasados"
            :disabled="actualizandoRetrasados"
          >
            Actualizar Proyectos
          </button>
          <button
            class="btn-cancel"
            @click="closeActualizarRetrasadosModal"
            :disabled="actualizandoRetrasados"
          >
            {{ resultadoActualizacion ? 'Cerrar' : 'Cancelar' }}
          </button>
        </div>
      </div>
    </div>

    <LoadingSpinner v-if="loading" message="Cargando proyectos..." />

    <ErrorAlert
      v-if="error"
      :message="error"
      type="error"
      @close="error = null"
    />

    <div v-if="!loading" class="filters">
      <div class="search-box">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8"></circle>
          <path d="m21 21-4.35-4.35"></path>
        </svg>
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Buscar proyecto..."
        />
      </div>

      <select v-model="filterEstado" class="filter-select">
        <option value="">Todos los estados</option>
        <option value="Planeado">Planeado</option>
        <option value="En Curso">En Curso</option>
        <option value="Completado">Completado</option>
        <option value="Retrasado">Retrasado</option>
        <option value="Cancelado">Cancelado</option>
      </select>

      <select v-model="filterTipoZona" class="filter-select">
        <option value="">Todos los tipos de zona</option>
        <option value="Residencial">Residencial</option>
        <option value="Comercial">Comercial</option>
        <option value="Industrial">Industrial</option>
        <option value="Mixto">Mixto</option>
      </select>

      <button @click="showSuperposicionModal = true" class="btn-analisis">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"></circle>
          <circle cx="12" cy="12" r="6"></circle>
          <circle cx="12" cy="12" r="2"></circle>
        </svg>
        Análisis de Superposición
      </button>

      <button @click="showZonasSinPlanModal = true" class="btn-analisis">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
          <circle cx="12" cy="10" r="3"></circle>
        </svg>
        Zonas Sin Planificación
      </button>
    </div>

    <!-- Mapa Interactivo de Proyectos -->
    <div v-if="!loading && !error" class="mapa-container">
      <MapaProyectos
        :proyectos="proyectos"
        :selected-estado="filterEstado"
        :selected-tipo-zona="filterTipoZona"
        @proyecto-selected="viewProject"
      />
      <div class="mapa-leyenda">
        <h4>Estados de Proyectos</h4>
        <div class="leyenda-item planeado">
          <span class="leyenda-color"></span>
          <label>Planeado</label>
        </div>
        <div class="leyenda-item en-curso">
          <span class="leyenda-color">🚧</span>
          <label>En Curso</label>
        </div>
        <div class="leyenda-item completado">
          <span class="leyenda-color"></span>
          <label>Completado</label>
        </div>
        <div class="leyenda-item retrasado">
          <span class="leyenda-color"></span>
          <label>Retrasado</label>
        </div>
        <div class="leyenda-item cancelado">
          <span class="leyenda-color"></span>
          <label>Cancelado</label>
        </div>

        <div style="margin-top: 24px; padding-top: 16px; border-top: 1px solid var(--border-color);">
          <p style="font-size: 12px; color: var(--text-secondary); margin: 0;">
            Haz clic en un proyecto en el mapa para ver más detalles
          </p>
        </div>
      </div>
    </div>

    <div v-if="!loading && !error" class="projects-grid">
      <div
        v-for="proyecto in filteredProyectos"
        :key="proyecto.proyectoUrbanoId || proyecto.proyecto_urbano_id"
        class="project-card"
      >
        <div class="project-header">
          <h3>{{ proyecto.nombre }}</h3>
          <span class="status-badge" :class="getStatusClass(proyecto.estado)">
              {{ proyecto.estado }}
            </span>
        </div>

        <p class="project-description">{{ proyecto.descripcion }}</p>

        <div class="project-details">
          <div class="detail-item">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
              <line x1="16" y1="2" x2="16" y2="6"></line>
              <line x1="8" y1="2" x2="8" y2="6"></line>
              <line x1="3" y1="10" x2="21" y2="10"></line>
            </svg>
            <span><strong>Inicio:</strong> {{ formatDate(proyecto.fechaInicio || proyecto.fecha_inicio) }}</span>
          </div>

          <div v-if="proyecto.fechaTermino || proyecto.fecha_termino"
               class="detail-item"
               :class="{ 'fecha-retrasada': isProyectoRetrasado(proyecto) }">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"></circle>
              <polyline points="12 6 12 12 16 14"></polyline>
            </svg>
            <span>
              <strong>Término:</strong> {{ formatDate(proyecto.fechaTermino || proyecto.fecha_termino) }}
              <span v-if="isProyectoRetrasado(proyecto)" class="badge-retrasado">Retrasado</span>
            </span>
          </div>

          <div class="detail-item">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="1" x2="12" y2="23"></line>
              <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path>
            </svg>
            <span>{{ formatCurrency(proyecto.presupuesto) }}</span>
          </div>

          <div v-if="proyecto.tipoProyecto || proyecto.tipo_proyecto" class="detail-item">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path>
            </svg>
            <span>{{ proyecto.tipoProyecto || proyecto.tipo_proyecto }}</span>
          </div>
        </div>

        <div class="project-actions">
          <button @click="viewProject(proyecto)" class="btn-secondary">
            Ver Detalles
          </button>
          <button
            v-if="authStore.isAdmin || authStore.isPlanificador"
            @click="editProject(proyecto)"
            class="btn-icon"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M17 3a2.828 2.828 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5L17 3z"></path>
            </svg>
          </button>
        </div>
      </div>

      <div v-if="filteredProyectos.length === 0" class="empty-state">
        <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
          <polyline points="14 2 14 8 20 8"></polyline>
        </svg>
        <h3>No se encontraron proyectos</h3>
        <p>Intenta ajustar los filtros de búsqueda</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useAuthStore } from '@/stores/auth';
import LoadingSpinner from '@/components/common/LoadingSpinner.vue';
import ErrorAlert from '@/components/common/ErrorAlert.vue';
import MapaProyectos from '@/components/common/MapaProyectos.vue';
import MapaNuevoProyecto from '@/components/common/MapaNuevoProyecto.vue';
import proyectosService from '@/services/proyectosService';

// ... (el resto de tu <script setup> es idéntico y correcto)
const authStore = useAuthStore();
const loading = ref(true);
const error = ref(null);
const proyectos = ref([]);
const searchQuery = ref('');
const filterEstado = ref('');
const filterTipoZona = ref('');

// Análisis de superposición
const showSuperposicionModal = ref(false);
const loadingSuperposiciones = ref(false);
const superposiciones = ref([]);

// Análisis de zonas sin planificación
const showZonasSinPlanModal = ref(false);
const loadingZonasSinPlan = ref(false);
const zonasSinPlanificacion = ref([]);

// Actualizar proyectos retrasados
const showActualizarRetrasadosModal = ref(false);
const actualizandoRetrasados = ref(false);
const resultadoActualizacion = ref(null);
const proyectosActualizadosCount = ref(0);

// Nuevo proyecto (modal)
const showCreateModal = ref(false);
const creating = ref(false);
const newProject = ref({
  nombre: '',
  descripcion: '',
  tipo_proyecto: 'RESIDENCIAL',
  fecha_inicio: '',
  fecha_termino: '',
  presupuesto: 0
});
// Editar proyecto
const showEditModal = ref(false);
const editing = ref(false);
const editProjectData = ref({
  id: null,
  nombre: '',
  descripcion: '',
  tipo_proyecto: 'RESIDENCIAL',
  fecha_inicio: '',
  fecha_termino: '',
  presupuesto: 0,
  estado: 'Planeado',
  geometria: ''
});
const showMapDraw = ref(false);
const filteredProyectos = computed(() => {
  return proyectos.value.filter(proyecto => {
    const matchesSearch = proyecto.nombre.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      (proyecto.descripcion && proyecto.descripcion.toLowerCase().includes(searchQuery.value.toLowerCase()));
    const matchesEstado = !filterEstado.value || proyecto.estado === filterEstado.value;
    return matchesSearch && matchesEstado;
  });
});
const getStatusClass = (estado) => {
  const classes = {
    'Planeado': 'planeado',
    'En Curso': 'en-curso',
    'Completado': 'completado',
    'Retrasado': 'retrasado',
    'Cancelado': 'cancelado'
  };
  return classes[estado] || '';
};
const formatDate = (dateString) => {
  if (!dateString) return 'Sin fecha';
  const date = new Date(dateString);
  return date.toLocaleDateString('es-CL', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  });
};
const formatCurrency = (amount) => {
  if (!amount) return 'Sin presupuesto';
  return new Intl.NumberFormat('es-CL', {
    style: 'currency',
    currency: 'CLP',
    minimumFractionDigits: 0
  }).format(amount);
};

const isProyectoRetrasado = (proyecto) => {
  if (proyecto.estado !== 'En Curso') return false;

  const fechaTermino = proyecto.fechaTermino || proyecto.fecha_termino;
  if (!fechaTermino) return false;

  const fechaTerminoDate = new Date(fechaTermino);
  const hoy = new Date();
  hoy.setHours(0, 0, 0, 0); // Resetear horas para comparar solo fechas

  return fechaTerminoDate < hoy;
};

const contarProyectosRetrasados = computed(() => {
  return proyectos.value.filter(p => isProyectoRetrasado(p)).length;
});

const formatArea = (area) => {
  const n = Number(area);
  if (!n && n !== 0) return '0 m²';
  return new Intl.NumberFormat('es-CL', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(n) + ' m²';
};

const totalAreaSuperposicion = computed(() => {
  return superposiciones.value.reduce((total, sup) => total + (sup.area_superposicion_m2 || 0), 0);
});

const zonasNuncaTuvieron = computed(() => {
  return zonasSinPlanificacion.value.filter(z => z.ultimo_proyecto === 'Ninguno').length;
});
const loadProyectos = async () => {
  loading.value = true;
  error.value = null;

  try {
    const data = await proyectosService.getAll(filterTipoZona.value || null);
    proyectos.value = data;
  } catch (err) {
    error.value = err.message || 'Error al cargar proyectos';
  } finally {
    loading.value = false;
  }
};
const viewProject = (proyecto) => {
  alert(`Ver detalles de: ${proyecto.nombre}`);
};
const editProject = (proyecto) => {
  const id = proyecto.proyectoUrbanoId || proyecto.proyecto_urbano_id || proyecto.id || proyecto.proyectoId;
  editProjectData.value = {
    id: id,
    nombre: proyecto.nombre || '',
    descripcion: proyecto.descripcion || '',
    tipo_proyecto: proyecto.tipoProyecto || proyecto.tipo_proyecto || 'RESIDENCIAL',
    fecha_inicio: (proyecto.fechaInicio || proyecto.fecha_inicio) ? (proyecto.fechaInicio || proyecto.fecha_inicio).slice(0,10) : '',
    fecha_termino: (proyecto.fechaTermino || proyecto.fecha_termino) ? (proyecto.fechaTermino || proyecto.fecha_termino).slice(0,10) : '',
    presupuesto: proyecto.presupuesto || 0,
    estado: proyecto.estado || 'Planeado',
    geometria: typeof proyecto.geometria === 'string' ? proyecto.geometria : (proyecto.geometria ? JSON.stringify(proyecto.geometria) : '')
  };
  showEditModal.value = true;
};

const cancelEdit = () => {
  showEditModal.value = false;
};

const saveEdit = async () => {
  if (!editProjectData.value || !editProjectData.value.id) {
    error.value = 'No se puede identificar el proyecto a editar.';
    return;
  }

  editing.value = true;
  error.value = null;

  try {
    // Normalizar fecha de inicio
    let fechaIso = null;
    if (editProjectData.value.fecha_inicio) {
      const d = new Date(editProjectData.value.fecha_inicio);
      if (!isNaN(d.getTime())) fechaIso = d.toISOString().slice(0,10);
      else fechaIso = editProjectData.value.fecha_inicio;
    }

    // Normalizar fecha de término
    let fechaTerminoIso = null;
    if (editProjectData.value.fecha_termino) {
      const d = new Date(editProjectData.value.fecha_termino);
      if (!isNaN(d.getTime())) fechaTerminoIso = d.toISOString().slice(0,10);
      else fechaTerminoIso = editProjectData.value.fecha_termino;
    }

    // Geometría: asegurar que sea string
    let geom = editProjectData.value.geometria || null;
    if (geom && typeof geom !== 'string') {
      try { geom = JSON.stringify(geom); } catch(e) { geom = String(geom); }
    }

    const payload = {
      nombre: editProjectData.value.nombre,
      descripcion: editProjectData.value.descripcion,
      tipoProyecto: editProjectData.value.tipo_proyecto,
      fechaInicio: fechaIso,
      fechaTermino: fechaTerminoIso,
      presupuesto: editProjectData.value.presupuesto ? Number(editProjectData.value.presupuesto) : null,
      estado: editProjectData.value.estado,
      geometria: geom
    };

    await proyectosService.update(editProjectData.value.id, payload);
    await loadProyectos();
    showEditModal.value = false;
    alert('Proyecto actualizado correctamente');
  } catch (err) {
    console.error('Error actualizando proyecto:', err);
    let msg = 'Error al actualizar proyecto';
    if (!err) msg = 'Error desconocido';
    else if (typeof err === 'string') msg = err;
    else if (err.message) msg = err.message;
    else if (err.response && err.response.data) msg = err.response.data;
    else { try { msg = JSON.stringify(err); } catch(e) { msg = String(err); } }
    error.value = msg;
  } finally {
    editing.value = false;
  }
};
const createProject = () => {
  // Abrir la interfaz de dibujo en el mapa para seleccionar el área primero
  newProject.value = {
    nombre: '',
    descripcion: '',
    tipo_proyecto: 'RESIDENCIAL',
    fecha_inicio: '',
    fecha_termino: '',
    presupuesto: 0,
    geometria: null
  };
  showMapDraw.value = true;
  showCreateModal.value = false;
};

const cancelMapDraw = () => {
  showMapDraw.value = false;
};

// Funciones para análisis de superposición
const loadSuperposiciones = async () => {
  loadingSuperposiciones.value = true;
  try {
    const data = await proyectosService.getSuperposicion();
    console.log('[ProyectosView] ============ DIAGNÓSTICO DE SUPERPOSICIONES ============');
    console.log('[ProyectosView] Total registros recibidos:', data?.length || 0);
    console.log('[ProyectosView] Datos raw completos:', JSON.stringify(data, null, 2));

    // Mostrar detalles de cada superposición
    (data || []).forEach((sup, idx) => {
      console.log(`\n[Superposición ${idx + 1}] ${sup.proyecto_1} <-> ${sup.proyecto_2}`);
      console.log('  - Tipo geom 1:', sup.tipo_geom_1);
      console.log('  - Tipo geom 2:', sup.tipo_geom_2);
      console.log('  - SRID origen 1:', sup.srid_origen_1);
      console.log('  - SRID origen 2:', sup.srid_origen_2);
      console.log('  - Área (geog buffer):', sup.area_geog_buffer_m2, 'm²');
      console.log('  - Área (proj buffer):', sup.area_proj_buffer_m2, 'm²');
      console.log('  - Área FINAL:', sup.area_superposicion_m2, 'm²');
      console.log('  - GeoJSON intersección (geog):', sup.inter_geojson_geog_buffer ? 'presente' : 'NULL');
      console.log('  - GeoJSON intersección (proj):', sup.inter_geojson_proj_buffer ? 'presente' : 'NULL');
    });

    // Normalizar campo numérico por si viene como string
    superposiciones.value = (data || []).map(s => ({
      ...s,
      area_superposicion_m2: s.area_superposicion_m2 !== null && s.area_superposicion_m2 !== undefined ? Number(s.area_superposicion_m2) : 0,
      area_geog_buffer_m2: s.area_geog_buffer_m2 !== null && s.area_geog_buffer_m2 !== undefined ? Number(s.area_geog_buffer_m2) : 0,
      area_proj_buffer_m2: s.area_proj_buffer_m2 !== null && s.area_proj_buffer_m2 !== undefined ? Number(s.area_proj_buffer_m2) : 0
    }));

    console.log('[ProyectosView] ============================================\n');
  } catch (err) {
    console.error('[ProyectosView] ERROR al cargar superposiciones:', err);
    // No establecer error.value global para no afectar el mapa
    superposiciones.value = [];
  } finally {
    loadingSuperposiciones.value = false;
  }
};

const closeSuperposicionModal = () => {
  showSuperposicionModal.value = false;
};

// Funciones para análisis de zonas sin planificación
const loadZonasSinPlanificacion = async () => {
  loadingZonasSinPlan.value = true;
  try {
    const data = await proyectosService.getZonasSinPlanificacion();
    console.log('[ProyectosView] Zonas sin planificación:', data);
    zonasSinPlanificacion.value = data || [];
  } catch (err) {
    console.error('[ProyectosView] ERROR al cargar zonas sin planificación:', err);
    // No establecer error.value global para no afectar el mapa
    zonasSinPlanificacion.value = [];
  } finally {
    loadingZonasSinPlan.value = false;
  }
};

const closeZonasSinPlanModal = () => {
  showZonasSinPlanModal.value = false;
};

// Funciones para actualizar proyectos retrasados
const abrirActualizarRetrasadosModal = () => {
  resultadoActualizacion.value = null;
  proyectosActualizadosCount.value = 0;
  showActualizarRetrasadosModal.value = true;
};

const closeActualizarRetrasadosModal = () => {
  showActualizarRetrasadosModal.value = false;
  if (resultadoActualizacion.value) {
    // Si hubo actualización, recargar proyectos
    loadProyectos();
  }
};

const confirmarActualizarRetrasados = async () => {
  actualizandoRetrasados.value = true;

  try {
    const usuarioId = authStore.user?.usuario_id || authStore.user?.usuarioId;

    if (!usuarioId) {
      error.value = 'No se pudo identificar el usuario actual';
      showActualizarRetrasadosModal.value = false;
      return;
    }

    const response = await proyectosService.actualizarProyectosRetrasados(usuarioId);
    const cantidad = response.cantidad || 0;

    // Establecer resultado
    proyectosActualizadosCount.value = cantidad;
    resultadoActualizacion.value = cantidad > 0
      ? `Se actualizaron ${cantidad} proyecto${cantidad !== 1 ? 's' : ''} a estado "Retrasado".`
      : 'No se encontraron proyectos que requieran actualización.';

  } catch (err) {
    console.error('[ProyectosView] ERROR al actualizar proyectos retrasados:', err);
    error.value = err.message || 'Error al actualizar proyectos retrasados';
    showActualizarRetrasadosModal.value = false;
  } finally {
    actualizandoRetrasados.value = false;
  }
};

// Watch para cargar superposiciones cuando se abre el modal
watch(showSuperposicionModal, (newValue) => {
  if (newValue) {
    loadSuperposiciones();
  }
});

// Watch para cargar zonas sin planificación cuando se abre el modal
watch(showZonasSinPlanModal, (newValue) => {
  if (newValue) {
    loadZonasSinPlanificacion();
  }
});

const onAreaDrawn = (geometry) => {
  // geometry es la geometría GeoJSON (objeto)
  newProject.value.geometria = geometry;
  // cerrar el mapa y abrir el formulario
  showMapDraw.value = false;
  showCreateModal.value = true;
};

const cancelCreate = () => {
  showCreateModal.value = false;
};

const saveProject = async () => {
  if (!newProject.value.nombre || newProject.value.nombre.trim() === '') {
    error.value = 'El nombre del proyecto es obligatorio.';
    return;
  }

  creating.value = true;
  error.value = null;

  try {
    // Preparar payload según backend (ajustar si es necesario)
    // Adaptar nombres al modelo backend: `tipoProyecto`, `fechaInicio`, `geometria` como string
    // Normalizar fecha al formato ISO (YYYY-MM-DD) que backend espera
    let fechaIso = null;
    if (newProject.value.fecha_inicio) {
      try {
        // newProject.value.fecha_inicio normalmente ya viene como 'YYYY-MM-DD'
        const d = new Date(newProject.value.fecha_inicio);
        if (!isNaN(d.getTime())) {
          fechaIso = d.toISOString().slice(0, 10);
        } else {
          // si no es un string parseable, usar tal cual
          fechaIso = newProject.value.fecha_inicio;
        }
      } catch (e) {
        fechaIso = newProject.value.fecha_inicio;
      }
    }

    // Normalizar fecha de término
    let fechaTerminoIso = null;
    if (newProject.value.fecha_termino) {
      try {
        const d = new Date(newProject.value.fecha_termino);
        if (!isNaN(d.getTime())) {
          fechaTerminoIso = d.toISOString().slice(0, 10);
        } else {
          fechaTerminoIso = newProject.value.fecha_termino;
        }
      } catch (e) {
        fechaTerminoIso = newProject.value.fecha_termino;
      }
    }

    const payload = {
      nombre: newProject.value.nombre,
      descripcion: newProject.value.descripcion,
      tipoProyecto: newProject.value.tipo_proyecto,
      fechaInicio: fechaIso,
      fechaTermino: fechaTerminoIso,
      presupuesto: newProject.value.presupuesto ? Number(newProject.value.presupuesto) : null,
      estado: 'Planeado',
      geometria: newProject.value.geometria ? JSON.stringify(newProject.value.geometria) : null,
      // Enviar ambos nombres por compatibilidad: usuarioId y usuario_id
      usuarioId: authStore.user?.usuario_id || authStore.user?.usuarioId || null,
      usuario_id: authStore.user?.usuario_id || authStore.user?.usuarioId || null
    };

    console.log('Payload crear proyecto:', payload);

    const created = await proyectosService.create(payload);
    // Refrescar la lista desde el servidor para asegurar formatos (fechas, geojson)
    await loadProyectos();
    showCreateModal.value = false;
    alert('Proyecto creado correctamente');
  } catch (err) {
    console.error('Error creando proyecto:', err);
    // Mostrar mensaje detallado si el backend devolvió texto o un objeto con message
    let msg = 'Error al crear proyecto';
    if (!err) {
      msg = 'Error desconocido';
    } else if (typeof err === 'string') {
      msg = err;
    } else if (err.message) {
      msg = err.message;
    } else if (err.response && err.response.data) {
      msg = err.response.data;
    } else {
      try { msg = JSON.stringify(err); } catch(e) { msg = String(err); }
    }
    error.value = msg;
  } finally {
    creating.value = false;
  }
};
onMounted(() => {
  loadProyectos();
});

// Recargar proyectos cuando cambia el filtro de tipo de zona
watch(filterTipoZona, () => {
  loadProyectos();
});
</script>

<style scoped>
/* .proyectos-page { ... } <-- Eliminado, no es necesario */

.page-container {
  /* max-width: 1400px;
  /* margin: 0 auto; <-- Esto ya lo maneja AppLayout.vue */
  /* padding: 32px 24px; <-- Esto ya lo maneja AppLayout.vue */
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.page-header h1 {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary); /* CAMBIADO */
  margin: 0 0 8px 0;
}

.page-header p {
  font-size: 16px;
  color: var(--text-secondary); /* CAMBIADO */
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.btn-primary {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: var(--accent-primary); /* CAMBIADO */
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary:hover {
  transform: translateY(-2px);
  background: var(--accent-primary-hover); /* CAMBIADO */
}

.filters {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.search-box {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: var(--bg-secondary); /* CAMBIADO */
  border: 1px solid var(--border-color); /* CAMBIADO */
  border-radius: 8px;
}

.search-box svg {
  color: var(--text-secondary); /* CAMBIADO */
}

.search-box input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 14px;
  background: transparent; /* CAMBIADO */
  color: var(--text-primary); /* CAMBIADO */
}

.search-box input::placeholder {
  color: var(--text-secondary);
}

.filter-select {
  padding: 12px 16px;
  background: var(--bg-secondary); /* CAMBIADO */
  border: 1px solid var(--border-color); /* CAMBIADO */
  color: var(--text-primary); /* CAMBIADO */
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
}

.filter-select option {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

/* Contenedor del Mapa de Proyectos */
.mapa-container {
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: 20px;
  margin-bottom: 24px;
}

.mapa-leyenda {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 20px;
  height: fit-content;
}

.mapa-leyenda h4 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 16px 0;
}

.leyenda-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.leyenda-color {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.leyenda-item label {
  font-size: 14px;
  color: var(--text-secondary);
  cursor: pointer;
}

/* Colores de estados para la leyenda */
.leyenda-item.planeado label { color: #3b82f6; }
.leyenda-item.en-curso label { color: #f59e0b; }
.leyenda-item.completado label { color: #10b981; }
.leyenda-item.retrasado label { color: #ef4444; }
.leyenda-item.cancelado label { color: #6b7280; }

.projects-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 24px;
}

.project-card {
  background: var(--bg-secondary); /* CAMBIADO */
  border-radius: 12px;
  padding: 24px;
  box-shadow: none; /* CAMBIADO */
  border: 1px solid var(--border-color); /* CAMBIADO */
  transition: transform 0.2s, box-shadow 0.2s;
}

.project-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.project-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}

.project-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary); /* CAMBIADO */
  margin: 0;
  flex: 1;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
  color: var(--text-primary); /* CAMBIADO */
}

.status-badge.planeado {
  background-color: var(--status-planeado-bg); /* CAMBIADO */
  color: var(--status-planeado-border);
}

.status-badge.en-curso {
  background-color: var(--status-en-curso-bg); /* CAMBIADO */
  color: var(--status-en-curso-border);
}

.status-badge.completado {
  background-color: var(--status-completado-bg); /* CAMBIADO */
  color: var(--status-completado-border);
}

.status-badge.retrasado {
  background-color: var(--status-retrasado-bg); /* CAMBIADO */
  color: var(--status-retrasado-border);
}

.status-badge.cancelado {
  background-color: var(--border-color); /* CAMBIADO */
  color: var(--text-secondary);
}

.project-description {
  font-size: 14px;
  color: var(--text-secondary); /* CAMBIADO */
  margin-bottom: 16px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.project-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color); /* CAMBIADO */
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-secondary); /* CAMBIADO */
}

.detail-item svg {
  flex-shrink: 0;
}

.project-actions {
  display: flex;
  gap: 8px;
}

.btn-secondary {
  flex: 1;
  padding: 10px 16px;
  background-color: var(--bg-primary); /* CAMBIADO */
  color: var(--text-secondary); /* CAMBIADO */
  border: 1px solid var(--border-color); /* CAMBIADO */
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-secondary:hover {
  background-color: var(--border-color); /* CAMBIADO */
  color: var(--text-primary);
}

.btn-icon {
  padding: 10px;
  background-color: var(--bg-primary); /* CAMBIADO */
  border: 1px solid var(--border-color); /* CAMBIADO */
  border-radius: 8px;
  cursor: pointer;
  color: var(--text-secondary); /* CAMBIADO */
  transition: all 0.2s;
}

.btn-icon:hover {
  background-color: var(--status-en-curso-bg); /* CAMBIADO */
  border-color: var(--status-en-curso-border); /* CAMBIADO */
  color: var(--status-en-curso-border); /* CAMBIADO */
}

.empty-state {
  grid-column: 1 / -1;
  padding: 64px 24px;
  text-align: center;
  color: var(--text-secondary); /* CAMBIADO */
}

/* Responsive */
@media (max-width: 1024px) {
  .mapa-container {
    grid-template-columns: 1fr;
  }

  .mapa-leyenda {
    order: -1;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .filters {
    flex-direction: column;
  }

  .projects-grid {
    grid-template-columns: 1fr;
  }

  .project-card {
    padding: 20px;
  }
}

/* ... (el resto de tus estilos es correcto) ... */
</style>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}
.modal-panel {
  width: 520px;
  background: var(--bg-primary);
  border-radius: 10px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.3);
  padding: 16px;
  border: 1px solid var(--border-color);
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.modal-header h3 { margin: 0; }
.btn-cerrar {
  background: transparent;
  border: none;
  font-size: 18px;
  cursor: pointer;
}
.modal-body { max-height: 60vh; overflow-y: auto; }
.modal-body .form-group { margin-bottom: 10px; }
.modal-body label { display:block; font-weight:600; margin-bottom:6px; }
.modal-body input, .modal-body select, .modal-body textarea {
  width: 100%; padding: 8px; border-radius: 6px; border: 1px solid var(--border-color); background: var(--bg-secondary); color: var(--text-primary);
}
.modal-footer { display:flex; gap:8px; justify-content:flex-end; margin-top:12px; }
.btn-guardar { background: var(--accent-primary); color: white; padding: 10px 14px; border-radius: 8px; border:none; cursor:pointer; }
.btn-cancel { background: transparent; border: 1px solid var(--border-color); padding: 10px 14px; border-radius: 8px; cursor:pointer; }

/* Modal ancho para superposición */
.modal-wide {
  width: 90%;
  max-width: 1000px;
}

/* Botón de análisis */
.btn-analisis {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: var(--accent-primary);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.btn-analisis:hover {
  background: var(--accent-primary-hover);
  transform: translateY(-2px);
}

/* Estadísticas de superposición */
.superposicion-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 20px;
  text-align: center;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--accent-primary);
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
}

/* Tabla de superposiciones */
.table-container {
  overflow-x: auto;
  border-radius: 8px;
  border: 1px solid var(--border-color);
}

.superposiciones-table {
  width: 100%;
  border-collapse: collapse;
  background: var(--bg-secondary);
}

.superposiciones-table thead {
  background: var(--bg-primary);
  border-bottom: 2px solid var(--border-color);
}

.superposiciones-table th {
  padding: 12px 16px;
  text-align: left;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.superposiciones-table td {
  padding: 16px;
  border-bottom: 1px solid var(--border-color);
  font-size: 14px;
  color: var(--text-primary);
}

.superposiciones-table tbody tr:hover {
  background: var(--bg-primary);
}

.superposiciones-table tbody tr:last-child td {
  border-bottom: none;
}

.proyecto-name {
  font-weight: 600;
  color: var(--text-primary);
}

.area-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: space-between;
}

.status-badge-small {
  padding: 4px 8px;
  border-radius: 8px;
  font-size: 11px;
  font-weight: 600;
  white-space: nowrap;
  display: inline-block;
}

.status-badge-small.planeado {
  background-color: var(--status-planeado-bg);
  color: var(--status-planeado-border);
}

.status-badge-small.en-curso {
  background-color: var(--status-en-curso-bg);
  color: var(--status-en-curso-border);
}

.status-badge-small.completado {
  background-color: var(--status-completado-bg);
  color: var(--status-completado-border);
}

.status-badge-small.retrasado {
  background-color: var(--status-retrasado-bg);
  color: var(--status-retrasado-border);
}

.status-badge-small.cancelado {
  background-color: var(--border-color);
  color: var(--text-secondary);
}

.warning-badge {
  padding: 4px 8px;
  background: var(--status-retrasado-bg);
  color: var(--status-retrasado-border);
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
  white-space: nowrap;
}

.critical-overlap {
  background: rgba(239, 68, 68, 0.05);
}

.critical-overlap:hover {
  background: rgba(239, 68, 68, 0.1);
}

.loading-section {
  padding: 40px;
  text-align: center;
}

/* Responsive para el modal ancho */
@media (max-width: 768px) {
  .modal-wide {
    width: 95%;
    max-width: none;
  }

  .superposicion-stats {
    grid-template-columns: 1fr;
  }

  .table-container {
    font-size: 12px;
  }

  .superposiciones-table th,
  .superposiciones-table td {
    padding: 8px;
  }

  .btn-analisis {
    padding: 10px 16px;
    font-size: 13px;
  }
}

/* Estilos para modal de zonas sin planificación */
.zonas-list {
  width: 100%;
}

.zonas-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.zonas-table {
  width: 100%;
  border-collapse: collapse;
  background: var(--bg-secondary);
}

.zonas-table thead {
  background: var(--bg-primary);
  border-bottom: 2px solid var(--border-color);
}

.zonas-table th {
  padding: 12px 16px;
  text-align: left;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.zonas-table td {
  padding: 16px;
  border-bottom: 1px solid var(--border-color);
  font-size: 14px;
  color: var(--text-primary);
}

.zonas-table tbody tr:hover {
  background: var(--bg-primary);
}

.zonas-table tbody tr:last-child td {
  border-bottom: none;
}

.zona-name {
  font-weight: 600;
  color: var(--text-primary);
}

.tipo-badge {
  padding: 4px 12px;
  background: var(--status-planeado-bg);
  color: var(--status-planeado-border);
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.sin-proyecto {
  color: var(--text-secondary);
  font-style: italic;
}

.anios-badge {
  padding: 4px 12px;
  background: var(--status-retrasado-bg);
  color: var(--status-retrasado-border);
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.anios-badge.nunca {
  background: var(--border-color);
  color: var(--text-secondary);
}

.estado-badge {
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
  display: inline-block;
}

.estado-badge.sin-proyectos {
  background: var(--status-retrasado-bg);
  color: var(--status-retrasado-border);
}

.estado-badge.sin-planificación-reciente {
  background: var(--status-en-curso-bg);
  color: var(--status-en-curso-border);
}

.estado-badge.con-planificación-reciente {
  background: var(--status-completado-bg);
  color: var(--status-completado-border);
}

.nunca-proyecto {
  background: rgba(239, 68, 68, 0.03);
}

.nunca-proyecto:hover {
  background: rgba(239, 68, 68, 0.08);
}

/* Estilos para modal de actualización de proyectos retrasados */
.modal-medium {
  width: 600px;
  max-width: 90%;
}

.btn-actualizar-retrasados {
  display: flex !important;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: #ef4444 !important;
  color: white !important;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.4);
}

.btn-actualizar-retrasados:hover {
  background: #dc2626 !important;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.5);
}

/* Estilos para indicar fechas retrasadas */
.fecha-retrasada {
  background: rgba(239, 68, 68, 0.1);
  border-radius: 6px;
  padding: 8px 12px !important;
  border-left: 3px solid #ef4444;
  animation: pulse-red 2s infinite;
}

@keyframes pulse-red {
  0%, 100% {
    background: rgba(239, 68, 68, 0.1);
  }
  50% {
    background: rgba(239, 68, 68, 0.2);
  }
}

.badge-retrasado {
  display: inline-block;
  margin-left: 8px;
  padding: 2px 8px;
  background: #ef4444;
  color: white;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 700;
  vertical-align: middle;
}

/* Alerta de proyectos a actualizar */
.alerta-proyectos {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border: 2px solid #f59e0b;
  border-radius: 12px;
  padding: 20px;
  margin: 20px 0;
  text-align: center;
}

.alerta-proyectos.sin-proyectos {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  border-color: #10b981;
}

.numero-proyectos {
  display: inline-block;
  width: 60px;
  height: 60px;
  background: #ef4444;
  color: white;
  border-radius: 50%;
  font-size: 28px;
  font-weight: 700;
  line-height: 60px;
  margin-bottom: 12px;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.confirmacion-section,
.resultado-section {
  text-align: center;
  padding: 24px 16px;
}

.icono-advertencia,
.icono-exito {
  margin: 0 auto 24px;
  width: 64px;
  height: 64px;
}

.icono-advertencia svg {
  color: #f59e0b;
}

.icono-exito svg {
  color: #10b981;
}

.confirmacion-section h4,
.resultado-section h4 {
  font-size: 24px;
  color: var(--text-primary);
  margin: 0 0 12px 0;
}

.confirmacion-section p,
.resultado-section p {
  font-size: 16px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0 0 24px 0;
}

.info-box {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  text-align: left;
  margin-top: 16px;
}

.info-box svg {
  flex-shrink: 0;
  color: var(--accent-primary);
  margin-top: 2px;
}

.info-box strong {
  display: block;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.info-box ul {
  margin: 0;
  padding-left: 20px;
  color: var(--text-secondary);
}

.info-box li {
  margin-bottom: 4px;
}

.success-stats {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 24px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 12px;
}

.stat-icon {
  font-size: 32px;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: var(--accent-primary);
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
  margin-top: 4px;
}

</style>
