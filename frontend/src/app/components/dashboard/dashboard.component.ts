import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { OfferService } from '../../services/offer.service';
import { AuthService } from '../../services/auth.service';
import { DashboardData, Offer } from '../../models/offer.model';

/**
 * DashboardComponent — Pantalla principal del sistema.
 *
 * Muestra:
 * 1. Tarjetas de resumen con conteos por estado (activas, próximas, vencidas)
 * 2. Lista de las 10 ofertas activas más recientes
 * 3. Lista de las 10 ofertas próximas más recientes
 *
 * Ciclo de vida de Angular:
 * OnInit es una interfaz que define el método ngOnInit().
 * Angular lo llama automáticamente después de crear el componente
 * y resolver sus dependencias. Es el lugar correcto para cargar datos.
 *
 * ¿Por qué no cargar datos en el constructor?
 * El constructor debe ser liviano — solo inicializar propiedades.
 * Las operaciones asíncronas (llamadas HTTP) van en ngOnInit().
 *
 * Requerimiento: 10.1, 10.2, 10.3
 */
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="dashboard">

      <!-- Encabezado -->
      <div class="page-header">
        <div>
          <h1 class="page-title">Dashboard</h1>
          <p class="page-subtitle">Resumen de ofertas comerciales</p>
        </div>
        <!-- Botón para crear oferta (visible para todos) -->
        <a routerLink="/offers/new" class="btn btn-primary">
          + Nueva oferta
        </a>
      </div>

      <!-- Estado de carga -->
      <div class="loading-state" *ngIf="loading">
        <div class="spinner"></div>
        <span>Cargando datos...</span>
      </div>

      <!-- Contenido principal (visible cuando no está cargando) -->
      <ng-container *ngIf="!loading && data">

        <!-- ── Tarjetas de resumen ── -->
        <!-- Requerimiento 10.1: mostrar conteos por estado -->
        <div class="stats-grid">

          <div class="stat-card stat-activa">
            <div class="stat-icon">✅</div>
            <div class="stat-info">
              <div class="stat-number">{{ data.activeCount }}</div>
              <div class="stat-label">Ofertas Activas</div>
            </div>
          </div>

          <div class="stat-card stat-proxima">
            <div class="stat-icon">🕐</div>
            <div class="stat-info">
              <div class="stat-number">{{ data.upcomingCount }}</div>
              <div class="stat-label">Próximas</div>
            </div>
          </div>

          <div class="stat-card stat-vencida">
            <div class="stat-icon">📦</div>
            <div class="stat-info">
              <div class="stat-number">{{ data.expiredCount }}</div>
              <div class="stat-label">Vencidas</div>
            </div>
          </div>

          <!-- Total calculado en el frontend -->
          <div class="stat-card stat-total">
            <div class="stat-icon">📊</div>
            <div class="stat-info">
              <div class="stat-number">
                {{ data.activeCount + data.upcomingCount + data.expiredCount }}
              </div>
              <div class="stat-label">Total</div>
            </div>
          </div>

        </div>

        <!-- ── Listas de ofertas recientes ── -->
        <!-- Requerimiento 10.2: hasta 10 por estado -->
        <div class="offers-grid">

          <!-- Ofertas activas -->
          <div class="card">
            <div class="card-header">
              <h2>Ofertas Activas</h2>
              <a routerLink="/offers" [queryParams]="{status: 'ACTIVA'}" class="link-more">
                Ver todas →
              </a>
            </div>

            <!-- Lista vacía -->
            <p class="empty-state" *ngIf="data.recentActive.length === 0">
              No hay ofertas activas en este momento.
            </p>

            <!-- Lista de ofertas -->
            <!-- *ngFor: directiva que repite el elemento por cada item del array -->
            <div class="offer-list" *ngIf="data.recentActive.length > 0">
              <div
                class="offer-item"
                *ngFor="let offer of data.recentActive"
              >
                <div class="offer-info">
                  <span class="offer-title">{{ offer.title }}</span>
                  <span class="offer-dates">
                    {{ formatDate(offer.startsAt) }} — {{ formatDate(offer.endsAt) }}
                  </span>
                </div>
                <span class="badge badge-activa">ACTIVA</span>
              </div>
            </div>
          </div>

          <!-- Ofertas próximas -->
          <div class="card">
            <div class="card-header">
              <h2>Próximas Ofertas</h2>
              <a routerLink="/offers" [queryParams]="{status: 'PROXIMA'}" class="link-more">
                Ver todas →
              </a>
            </div>

            <p class="empty-state" *ngIf="data.recentUpcoming.length === 0">
              No hay ofertas próximas programadas.
            </p>

            <div class="offer-list" *ngIf="data.recentUpcoming.length > 0">
              <div
                class="offer-item"
                *ngFor="let offer of data.recentUpcoming"
              >
                <div class="offer-info">
                  <span class="offer-title">{{ offer.title }}</span>
                  <span class="offer-dates">
                    Inicia: {{ formatDate(offer.startsAt) }}
                  </span>
                </div>
                <span class="badge badge-proxima">PRÓXIMA</span>
              </div>
            </div>
          </div>

        </div>

      </ng-container>

    </div>
  `,
  styles: [`
    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 24px;
    }

    .page-title {
      font-size: 24px;
      font-weight: 700;
      color: #212529;
      margin-bottom: 4px;
    }

    .page-subtitle {
      color: #6c757d;
      font-size: 14px;
    }

    .loading-state {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 40px;
      justify-content: center;
      color: #6c757d;
    }

    /* Grid de tarjetas de estadísticas */
    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 16px;
      margin-bottom: 24px;
    }

    .stat-card {
      background: white;
      border-radius: 8px;
      padding: 20px;
      display: flex;
      align-items: center;
      gap: 16px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.1);
      border-left: 4px solid transparent;
    }

    .stat-activa  { border-left-color: #2d6a4f; }
    .stat-proxima { border-left-color: #1d3557; }
    .stat-vencida { border-left-color: #6c757d; }
    .stat-total   { border-left-color: #e63946; }

    .stat-icon { font-size: 28px; }

    .stat-number {
      font-size: 32px;
      font-weight: 700;
      line-height: 1;
      color: #212529;
    }

    .stat-label {
      font-size: 13px;
      color: #6c757d;
      margin-top: 4px;
    }

    /* Grid de listas de ofertas */
    .offers-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 16px;
    }

    @media (max-width: 768px) {
      .offers-grid { grid-template-columns: 1fr; }
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
    }

    .card-header h2 {
      font-size: 16px;
      font-weight: 600;
    }

    .link-more {
      font-size: 13px;
      color: #e63946;
      text-decoration: none;
    }

    .link-more:hover { text-decoration: underline; }

    .empty-state {
      color: #6c757d;
      font-size: 14px;
      text-align: center;
      padding: 20px 0;
    }

    .offer-list { display: flex; flex-direction: column; gap: 8px; }

    .offer-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 10px 12px;
      background: #f8f9fa;
      border-radius: 6px;
    }

    .offer-info {
      display: flex;
      flex-direction: column;
      gap: 2px;
      flex: 1;
      min-width: 0;
    }

    .offer-title {
      font-weight: 500;
      font-size: 14px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .offer-dates {
      font-size: 12px;
      color: #6c757d;
    }
  `]
})
export class DashboardComponent implements OnInit {

  /** Datos del dashboard cargados desde el backend */
  data: DashboardData | null = null;

  /** Estado de carga para mostrar el spinner */
  loading = true;

  constructor(
    private offerService: OfferService,
    public authService: AuthService
  ) {}

  /**
   * ngOnInit: se ejecuta cuando Angular termina de crear el componente.
   * Aquí cargamos los datos del dashboard desde el backend.
   */
  ngOnInit(): void {
    this.loadDashboard();
  }

  /**
   * Carga los datos del dashboard desde el backend.
   * Requerimiento 10.3: actualizar datos en cada carga de página.
   */
  loadDashboard(): void {
    this.loading = true;

    this.offerService.getDashboard().subscribe({
      next: (data) => {
        this.data = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  /**
   * Formatea una fecha ISO 8601 a formato legible en español.
   * Ejemplo: "2024-07-01T00:00:00" → "01/07/2024"
   *
   * @param dateStr Fecha en formato ISO 8601
   */
  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return date.toLocaleDateString('es-AR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }
}
