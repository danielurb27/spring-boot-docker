import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserService } from '../../services/user.service';
import { AuditLog } from '../../models/user.model';

/**
 * AuditLogComponent — Muestra el historial de cambios de una oferta.
 *
 * Es un componente "presentacional" que recibe el offerId como @Input
 * y carga el historial de auditoría del backend.
 *
 * @Input: decorador que marca una propiedad como entrada del componente.
 * El componente padre pasa el valor así:
 * <app-audit-log [offerId]="42"></app-audit-log>
 *
 * Este componente se usa dentro de OfferListComponent cuando el usuario
 * hace clic en "Ver historial" de una oferta.
 *
 * Requerimiento: 9.2 — Historial ordenado por created_at DESC.
 */
@Component({
  selector: 'app-audit-log',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="audit-log">

      <h3 class="audit-title">Historial de cambios</h3>

      <!-- Estado de carga -->
      <div class="loading-state" *ngIf="loading">
        <div class="spinner"></div>
        <span>Cargando historial...</span>
      </div>

      <!-- Sin registros -->
      <p class="empty-state" *ngIf="!loading && logs.length === 0">
        No hay registros de auditoría para esta oferta.
      </p>

      <!-- Lista de eventos -->
      <div class="audit-list" *ngIf="!loading && logs.length > 0">
        <div class="audit-item" *ngFor="let log of logs">

          <!-- Icono y tipo de evento -->
          <div class="audit-icon" [class]="'audit-icon-' + log.changeType.toLowerCase()">
            {{ changeTypeIcon(log.changeType) }}
          </div>

          <div class="audit-content">
            <!-- Tipo de cambio y campo -->
            <div class="audit-header">
              <span class="audit-type">{{ changeTypeLabel(log.changeType) }}</span>
              <span class="audit-field" *ngIf="log.fieldChanged">
                — campo: <strong>{{ log.fieldChanged }}</strong>
              </span>
            </div>

            <!-- Valores anterior y nuevo (para UPDATE) -->
            <div class="audit-values" *ngIf="log.oldValue || log.newValue">
              <span class="old-value" *ngIf="log.oldValue">
                Antes: {{ log.oldValue }}
              </span>
              <span class="arrow" *ngIf="log.oldValue && log.newValue">→</span>
              <span class="new-value" *ngIf="log.newValue">
                Después: {{ log.newValue }}
              </span>
            </div>

            <!-- Observación -->
            <div class="audit-observation" *ngIf="log.observation">
              {{ log.observation }}
            </div>

            <!-- Timestamp -->
            <div class="audit-timestamp">
              {{ formatDateTime(log.createdAt) }}
              <span *ngIf="log.changedBy"> · Usuario ID: {{ log.changedBy }}</span>
              <span *ngIf="!log.changedBy"> · Sistema automático</span>
            </div>
          </div>

        </div>
      </div>

    </div>
  `,
  styles: [`
    .audit-log { padding: 16px 0; }

    .audit-title {
      font-size: 15px;
      font-weight: 600;
      margin-bottom: 16px;
      color: #212529;
    }

    .loading-state {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #6c757d;
      font-size: 14px;
    }

    .empty-state {
      color: #6c757d;
      font-size: 14px;
      text-align: center;
      padding: 16px 0;
    }

    .audit-list { display: flex; flex-direction: column; gap: 12px; }

    .audit-item {
      display: flex;
      gap: 12px;
      padding: 12px;
      background: #f8f9fa;
      border-radius: 6px;
    }

    .audit-icon {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 14px;
      flex-shrink: 0;
    }

    .audit-icon-create   { background: #d4edda; }
    .audit-icon-update   { background: #cce5ff; }
    .audit-icon-delete   { background: #f8d7da; }
    .audit-icon-auto_delete { background: #fff3cd; }

    .audit-content { flex: 1; min-width: 0; }

    .audit-header {
      font-size: 13px;
      margin-bottom: 4px;
    }

    .audit-type { font-weight: 600; }
    .audit-field { color: #6c757d; }

    .audit-values {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 12px;
      margin-bottom: 4px;
    }

    .old-value { color: #dc3545; text-decoration: line-through; }
    .new-value { color: #28a745; }
    .arrow { color: #6c757d; }

    .audit-observation {
      font-size: 12px;
      color: #6c757d;
      font-style: italic;
      margin-bottom: 4px;
    }

    .audit-timestamp {
      font-size: 11px;
      color: #adb5bd;
    }
  `]
})
export class AuditLogComponent implements OnInit {

  /**
   * @Input: el ID de la oferta cuyo historial queremos mostrar.
   * El componente padre lo pasa como atributo HTML:
   * <app-audit-log [offerId]="selectedOfferId"></app-audit-log>
   */
  @Input() offerId!: number;

  logs: AuditLog[] = [];
  loading = true;

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.loadAuditHistory();
  }

  loadAuditHistory(): void {
    this.userService.getAuditHistory(this.offerId).subscribe({
      next: (logs) => {
        this.logs = logs;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  /** Retorna el emoji correspondiente al tipo de cambio */
  changeTypeIcon(type: string): string {
    const icons: Record<string, string> = {
      'CREATE': '✅',
      'UPDATE': '✏️',
      'DELETE': '🗑️',
      'AUTO_DELETE': '🤖'
    };
    return icons[type] || '📝';
  }

  /** Retorna la etiqueta legible del tipo de cambio */
  changeTypeLabel(type: string): string {
    const labels: Record<string, string> = {
      'CREATE': 'Creación',
      'UPDATE': 'Modificación',
      'DELETE': 'Eliminación',
      'AUTO_DELETE': 'Eliminación automática'
    };
    return labels[type] || type;
  }

  /** Formatea un timestamp ISO a fecha y hora legible */
  formatDateTime(dateStr: string): string {
    return new Date(dateStr).toLocaleString('es-AR', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }
}
