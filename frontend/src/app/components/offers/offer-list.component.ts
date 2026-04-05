import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { OfferService } from '../../services/offer.service';
import { AuthService } from '../../services/auth.service';
import { Offer, OfferFilters, OfferStatus, OfferType, Sector, PageResponse } from '../../models/offer.model';

/**
 * OfferListComponent — Listado de ofertas con filtros y paginación.
 *
 * Funcionalidades:
 * - Tabla paginada de ofertas (máx 20 por página)
 * - Filtros por sector, tipo, estado y rango de fechas
 * - Botones de editar y eliminar (eliminar solo para ADMIN)
 * - Navegación a crear nueva oferta
 *
 * ActivatedRoute: servicio de Angular que da acceso a los parámetros
 * de la ruta actual. Lo usamos para leer queryParams como ?status=ACTIVA
 * cuando el usuario llega desde el dashboard.
 *
 * Requerimiento: 6.1, 6.2, 6.5
 */
@Component({
  selector: 'app-offer-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="offer-list-page">

      <!-- Encabezado -->
      <div class="page-header">
        <div>
          <h1 class="page-title">Ofertas</h1>
          <p class="page-subtitle" *ngIf="page">
            {{ page.totalElements }} ofertas encontradas
          </p>
        </div>
        <a routerLink="/offers/new" class="btn btn-primary">
          <img src="assets/images/icons/actions/ic-add.svg" alt="" width="14" height="14" style="filter: brightness(0) invert(1);">
          Nueva oferta
        </a>
      </div>

      <!-- ── Panel de filtros ── -->
      <div class="card filters-card">
        <div class="filters-grid">

          <!-- Filtro por estado -->
          <div class="form-group">
            <label class="form-label">Estado</label>
            <select class="form-control" [(ngModel)]="filters.status" (change)="applyFilters()">
              <option value="">Todos</option>
              <option value="ACTIVA">Activa</option>
              <option value="PROXIMA">Próxima</option>
              <option value="VENCIDA">Vencida</option>
            </select>
          </div>

          <!-- Filtro por sector -->
          <div class="form-group">
            <label class="form-label">Sector</label>
            <select class="form-control" [(ngModel)]="filters.sectorId" (change)="applyFilters()">
              <option [value]="undefined">Todos</option>
              <!-- *ngFor sobre el array de sectores cargado del backend -->
              <option *ngFor="let sector of sectors" [value]="sector.id">
                {{ sector.code ? sector.code + ' - ' : '' }}{{ sector.name }}
              </option>
            </select>
          </div>

          <!-- Filtro por tipo de oferta -->
          <div class="form-group">
            <label class="form-label">Tipo</label>
            <select class="form-control" [(ngModel)]="filters.offerTypeId" (change)="applyFilters()">
              <option [value]="undefined">Todos</option>
              <option *ngFor="let type of offerTypes" [value]="type.id">
                {{ type.name }}
              </option>
            </select>
          </div>

          <!-- Botón limpiar filtros -->
          <div class="form-group" style="justify-content: flex-end;">
            <label class="form-label">&nbsp;</label>
            <button class="btn btn-secondary" (click)="clearFilters()">
              Limpiar filtros
            </button>
          </div>

        </div>
      </div>

      <!-- Estado de carga -->
      <div class="loading-state" *ngIf="loading">
        <div class="spinner"></div>
        <span>Cargando ofertas...</span>
      </div>

      <!-- Tabla de ofertas -->
      <div class="card" *ngIf="!loading">

        <!-- Sin resultados -->
        <div class="empty-state" *ngIf="offers.length === 0">
          <p>No se encontraron ofertas con los filtros aplicados.</p>
          <a routerLink="/offers/new" class="btn btn-primary" style="margin-top: 12px;">
            Crear primera oferta
          </a>
        </div>

        <!-- Tabla -->
        <table class="table" *ngIf="offers.length > 0">
          <thead>
            <tr>
              <th>Título</th>
              <th>Estado</th>
              <th>Progreso</th>
              <th>Inicio</th>
              <th>Fin</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let offer of offers">
              <td>
                <span class="offer-title-cell">{{ offer.title }}</span>
                <span class="offer-desc" *ngIf="offer.description">
                  {{ offer.description | slice:0:60 }}{{ offer.description!.length > 60 ? '...' : '' }}
                </span>
              </td>
              <td>
                <!-- Badge de estado con clase dinámica según el valor -->
                <span [class]="'badge badge-' + offer.status.toLowerCase()">
                  {{ statusLabel(offer.status) }}
                </span>
              </td>
              <td class="progress-cell">
                <!--
                  Barra de progreso de la oferta.
                  Muestra qué porcentaje del período de vigencia ya transcurrió.
                  - PROXIMA: 0% (aún no empezó)
                  - ACTIVA: (ahora - inicio) / (fin - inicio) * 100
                  - VENCIDA: 100% (ya terminó)
                  El color cambia según el estado para dar contexto visual.
                -->
                <div class="progress-bar-container" [title]="progressLabel(offer)">
                  <div
                    class="progress-bar-fill"
                    [class]="'progress-fill-' + offer.status.toLowerCase()"
                    [style.width.%]="calculateProgress(offer)"
                  ></div>
                </div>
                <span class="progress-text">{{ calculateProgress(offer) }}%</span>
              </td>
              <td>{{ formatDate(offer.startsAt) }}</td>
              <td>{{ formatDate(offer.endsAt) }}</td>
              <td>
                <div class="action-buttons">
                  <!-- Editar: disponible para todos -->
                  <a
                    [routerLink]="['/offers', offer.id, 'edit']"
                    class="btn btn-secondary btn-sm"
                    title="Editar oferta"
                  >
                    <img src="assets/images/icons/actions/ic-edit.svg" alt="Editar" width="14" height="14">
                    Editar
                  </a>
                  <!-- Eliminar: solo para ADMIN -->
                  <button
                    *ngIf="authService.isAdmin()"
                    class="btn btn-danger btn-sm"
                    (click)="deleteOffer(offer)"
                    [disabled]="deletingId === offer.id"
                    title="Eliminar oferta"
                  >
                    <img
                      *ngIf="deletingId !== offer.id"
                      src="assets/images/icons/actions/ic-delete.svg"
                      alt="Eliminar"
                      width="14" height="14"
                      style="filter: brightness(0) invert(1);"
                    >
                    {{ deletingId === offer.id ? '...' : 'Eliminar' }}
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <!-- ── Paginación ── -->
        <div class="pagination" *ngIf="page && page.totalPages > 1">
          <button
            class="btn btn-secondary btn-sm"
            [disabled]="page.first"
            (click)="goToPage(filters.page - 1)"
          >
            ← Anterior
          </button>

          <!-- Indicador de página actual -->
          <span class="page-info">
            Página {{ page.number + 1 }} de {{ page.totalPages }}
          </span>

          <button
            class="btn btn-secondary btn-sm"
            [disabled]="page.last"
            (click)="goToPage(filters.page + 1)"
          >
            Siguiente →
          </button>
        </div>

      </div>

    </div>
  `,
  styles: [`
    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 16px;
    }

    .page-title { font-size: 24px; font-weight: 700; margin-bottom: 4px; }
    .page-subtitle { color: #6c757d; font-size: 14px; }

    .filters-card { margin-bottom: 16px; padding: 16px; }

    .filters-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
      gap: 12px;
      align-items: end;
    }

    .filters-grid .form-group { margin-bottom: 0; }

    .loading-state {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 40px;
      justify-content: center;
      color: #6c757d;
    }

    .empty-state {
      text-align: center;
      padding: 40px;
      color: #6c757d;
    }

    .offer-title-cell {
      display: block;
      font-weight: 500;
    }

    .offer-desc {
      display: block;
      font-size: 12px;
      color: #6c757d;
      margin-top: 2px;
    }

    .action-buttons {
      display: flex;
      gap: 6px;
    }

    .pagination {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 16px;
      padding-top: 16px;
      border-top: 1px solid #dee2e6;
      margin-top: 8px;
    }

    .page-info {
      font-size: 14px;
      color: #6c757d;
    }

    /* ── Barra de progreso ── */
    .progress-cell {
      min-width: 120px;
    }

    .progress-bar-container {
      width: 100%;
      height: 8px;
      background-color: #e9ecef;
      border-radius: 4px;
      overflow: hidden;
      margin-bottom: 3px;
    }

    .progress-bar-fill {
      height: 100%;
      border-radius: 4px;
      transition: width 0.3s ease;
    }

    /* Color según estado */
    .progress-fill-activa  { background-color: #2d6a4f; }
    .progress-fill-proxima { background-color: #a8dadc; }
    .progress-fill-vencida { background-color: #6c757d; }

    .progress-text {
      font-size: 11px;
      color: #6c757d;
      font-weight: 500;
    }
  `]
})
export class OfferListComponent implements OnInit {

  offers: Offer[] = [];
  page: PageResponse<Offer> | null = null;
  offerTypes: OfferType[] = [];
  sectors: Sector[] = [];
  loading = true;
  deletingId: number | null = null;

  /** Filtros actuales — se sincronizan con los selects del template */
  filters: OfferFilters = {
    page: 0,
    size: 20
  };

  constructor(
    private offerService: OfferService,
    public authService: AuthService,
    private route: ActivatedRoute   // Para leer queryParams de la URL
  ) {}

  ngOnInit(): void {
    // Leer queryParams de la URL (ej: ?status=ACTIVA desde el dashboard)
    this.route.queryParams.subscribe(params => {
      if (params['status']) {
        this.filters.status = params['status'] as OfferStatus;
      }
      this.loadOffers();
    });

    // Cargar catálogos para los selectores de filtros
    this.loadCatalogs();
  }

  /** Carga la lista de ofertas con los filtros actuales */
  loadOffers(): void {
    this.loading = true;

    this.offerService.getOffers(this.filters).subscribe({
      next: (page) => {
        this.page = page;
        this.offers = page.content;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  /** Carga tipos de oferta y sectores para los selectores */
  loadCatalogs(): void {
    this.offerService.getOfferTypes().subscribe(types => this.offerTypes = types);
    this.offerService.getSectors().subscribe(sectors => this.sectors = sectors);
  }

  /** Aplica los filtros y vuelve a la primera página */
  applyFilters(): void {
    this.filters.page = 0;
    this.loadOffers();
  }

  /** Limpia todos los filtros */
  clearFilters(): void {
    this.filters = { page: 0, size: 20 };
    this.loadOffers();
  }

  /** Navega a una página específica */
  goToPage(page: number): void {
    this.filters.page = page;
    this.loadOffers();
  }

  /** Elimina una oferta con confirmación del usuario */
  deleteOffer(offer: Offer): void {
    // confirm() muestra un diálogo nativo del navegador
    // En producción se reemplazaría por un modal personalizado
    if (!confirm(`¿Eliminar la oferta "${offer.title}"? Esta acción no se puede deshacer.`)) {
      return;
    }

    this.deletingId = offer.id;

    this.offerService.deleteOffer(offer.id).subscribe({
      next: () => {
        // Remover la oferta de la lista local sin recargar
        this.offers = this.offers.filter(o => o.id !== offer.id);
        this.deletingId = null;
      },
      error: () => {
        this.deletingId = null;
        alert('Error al eliminar la oferta. Intente nuevamente.');
      }
    });
  }

  /** Convierte el status enum a etiqueta legible en español */
  statusLabel(status: OfferStatus): string {
    const labels: Record<OfferStatus, string> = {
      'ACTIVA': 'Activa',
      'PROXIMA': 'Próxima',
      'VENCIDA': 'Vencida'
    };
    return labels[status] || status;
  }

  /** Formatea fecha ISO a formato local */
  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleDateString('es-AR', {
      day: '2-digit', month: '2-digit', year: 'numeric'
    });
  }

  /**
   * Calcula el porcentaje de progreso de una oferta.
   *
   * Lógica:
   * - PROXIMA: 0% (la oferta aún no comenzó)
   * - VENCIDA: 100% (la oferta ya terminó)
   * - ACTIVA: (ahora - inicio) / (fin - inicio) * 100
   *
   * Ejemplo: oferta del 1 al 31 de julio, hoy es el 16 de julio
   * → (16 - 1) / (31 - 1) * 100 = 50%
   *
   * Math.min/max garantizan que el resultado esté entre 0 y 100
   * aunque haya pequeñas diferencias de zona horaria.
   */
  calculateProgress(offer: Offer): number {
    if (offer.status === 'PROXIMA') return 0;
    if (offer.status === 'VENCIDA') return 100;

    const start = new Date(offer.startsAt).getTime();
    const end   = new Date(offer.endsAt).getTime();
    const now   = Date.now();

    const total   = end - start;
    const elapsed = now - start;

    if (total <= 0) return 100;

    const percent = Math.round((elapsed / total) * 100);
    return Math.min(100, Math.max(0, percent));
  }

  /**
   * Texto descriptivo para el tooltip de la barra de progreso.
   * Muestra cuántos días quedan o cuántos días lleva activa.
   */
  progressLabel(offer: Offer): string {
    const now = Date.now();
    const end = new Date(offer.endsAt).getTime();
    const start = new Date(offer.startsAt).getTime();

    if (offer.status === 'PROXIMA') {
      const daysUntil = Math.ceil((start - now) / (1000 * 60 * 60 * 24));
      return `Comienza en ${daysUntil} día(s)`;
    }
    if (offer.status === 'VENCIDA') {
      return 'Oferta finalizada';
    }
    const daysLeft = Math.ceil((end - now) / (1000 * 60 * 60 * 24));
    return `${this.calculateProgress(offer)}% completado · ${daysLeft} día(s) restante(s)`;
  }
}
