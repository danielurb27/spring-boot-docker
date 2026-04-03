import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { OfferService } from '../../services/offer.service';
import { CreateOfferRequest, OfferType, Sector } from '../../models/offer.model';
import { HttpErrorResponse } from '@angular/common/http';

/**
 * OfferFormComponent — Formulario reutilizable para crear y editar ofertas.
 *
 * ¿Cómo sabe si está creando o editando?
 * Mira la URL: si tiene un parámetro :id, está editando.
 * Si no tiene :id, está creando.
 *
 * ActivatedRoute.snapshot.paramMap.get('id'):
 * Obtiene el parámetro :id de la URL actual.
 * Ejemplo: /offers/42/edit → id = "42"
 *          /offers/new     → id = null
 *
 * Validación de fechas en cliente:
 * Validamos que starts_at < ends_at antes de enviar al backend.
 * Esto mejora la UX: el usuario ve el error inmediatamente sin esperar
 * la respuesta del servidor. El backend también valida (segunda capa).
 *
 * Requerimiento: 3.1, 3.2, 4.1
 */
@Component({
  selector: 'app-offer-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="offer-form-page">

      <!-- Encabezado con breadcrumb -->
      <div class="page-header">
        <div>
          <a routerLink="/offers" class="breadcrumb-link">← Volver a ofertas</a>
          <h1 class="page-title">{{ isEditing ? 'Editar oferta' : 'Nueva oferta' }}</h1>
        </div>
      </div>

      <!-- Estado de carga (al cargar datos para edición) -->
      <div class="loading-state" *ngIf="loadingOffer">
        <div class="spinner"></div>
        <span>Cargando oferta...</span>
      </div>

      <!-- Formulario -->
      <div class="card" *ngIf="!loadingOffer">

        <!-- Mensaje de error -->
        <div class="alert alert-error" *ngIf="errorMessage">
          {{ errorMessage }}
        </div>

        <form (ngSubmit)="onSubmit()" #offerForm="ngForm">

          <!-- Título -->
          <div class="form-group">
            <label class="form-label" for="title">
              Título <span class="required">*</span>
            </label>
            <input
              id="title"
              type="text"
              class="form-control"
              [(ngModel)]="formData.title"
              name="title"
              required
              maxlength="200"
              placeholder="Ej: Oferta de verano Ferretería"
            />
          </div>

          <!-- Descripción -->
          <div class="form-group">
            <label class="form-label" for="description">Descripción</label>
            <textarea
              id="description"
              class="form-control"
              [(ngModel)]="formData.description"
              name="description"
              rows="3"
              placeholder="Descripción opcional de la oferta..."
            ></textarea>
          </div>

          <!-- Tipo de oferta y Sector en la misma fila -->
          <div class="form-row">

            <div class="form-group">
              <label class="form-label" for="offerTypeId">
                Tipo de oferta <span class="required">*</span>
              </label>
              <select
                id="offerTypeId"
                class="form-control"
                [(ngModel)]="formData.offerTypeId"
                name="offerTypeId"
                required
              >
                <option [value]="null">Seleccionar tipo...</option>
                <option *ngFor="let type of offerTypes" [value]="type.id">
                  {{ type.name }}
                </option>
              </select>
            </div>

            <div class="form-group">
              <label class="form-label" for="sectorId">
                Sector <span class="required">*</span>
              </label>
              <select
                id="sectorId"
                class="form-control"
                [(ngModel)]="formData.sectorId"
                name="sectorId"
                required
              >
                <option [value]="null">Seleccionar sector...</option>
                <option *ngFor="let sector of sectors" [value]="sector.id">
                  {{ sector.code ? sector.code + ' - ' : '' }}{{ sector.name }}
                </option>
              </select>
            </div>

          </div>

          <!-- Fechas en la misma fila -->
          <div class="form-row">

            <div class="form-group">
              <label class="form-label" for="startsAt">
                Fecha de inicio <span class="required">*</span>
              </label>
              <!--
                type="datetime-local": input nativo para fecha y hora.
                El valor es un string en formato "YYYY-MM-DDTHH:mm".
                Lo convertimos al formato del backend en onSubmit().
              -->
              <input
                id="startsAt"
                type="datetime-local"
                class="form-control"
                [(ngModel)]="startsAtLocal"
                name="startsAt"
                required
              />
            </div>

            <div class="form-group">
              <label class="form-label" for="endsAt">
                Fecha de fin <span class="required">*</span>
              </label>
              <input
                id="endsAt"
                type="datetime-local"
                class="form-control"
                [(ngModel)]="endsAtLocal"
                name="endsAt"
                required
              />
            </div>

          </div>

          <!-- Error de validación de fechas -->
          <div class="alert alert-error" *ngIf="dateError">
            {{ dateError }}
          </div>

          <!-- Botones de acción -->
          <div class="form-actions">
            <a routerLink="/offers" class="btn btn-secondary">Cancelar</a>
            <button
              type="submit"
              class="btn btn-primary"
              [disabled]="offerForm.invalid || saving"
            >
              <span *ngIf="saving" class="spinner"></span>
              {{ saving ? 'Guardando...' : (isEditing ? 'Guardar cambios' : 'Crear oferta') }}
            </button>
          </div>

        </form>
      </div>

    </div>
  `,
  styles: [`
    .page-header { margin-bottom: 16px; }
    .page-title { font-size: 24px; font-weight: 700; margin-top: 4px; }

    .breadcrumb-link {
      color: #6c757d;
      text-decoration: none;
      font-size: 14px;
    }
    .breadcrumb-link:hover { color: #e63946; }

    .loading-state {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 40px;
      justify-content: center;
      color: #6c757d;
    }

    /* Dos columnas para tipo/sector y fechas */
    .form-row {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 16px;
    }

    @media (max-width: 600px) {
      .form-row { grid-template-columns: 1fr; }
    }

    .required { color: #e63946; }

    textarea.form-control { resize: vertical; }

    .form-actions {
      display: flex;
      gap: 12px;
      justify-content: flex-end;
      margin-top: 8px;
      padding-top: 16px;
      border-top: 1px solid #dee2e6;
    }
  `]
})
export class OfferFormComponent implements OnInit {

  /** true si estamos editando, false si estamos creando */
  isEditing = false;

  /** ID de la oferta a editar (null si estamos creando) */
  offerId: number | null = null;

  /** Datos del formulario */
  formData: Partial<CreateOfferRequest> = {
    title: '',
    description: null,
    offerTypeId: null as any,
    sectorId: null as any,
  };

  /**
   * Valores de los inputs datetime-local.
   * Los inputs datetime-local usan el formato "YYYY-MM-DDTHH:mm"
   * que es diferente al formato ISO 8601 del backend "YYYY-MM-DDTHH:mm:ss".
   * Usamos propiedades separadas para manejar esta conversión.
   */
  startsAtLocal = '';
  endsAtLocal = '';

  offerTypes: OfferType[] = [];
  sectors: Sector[] = [];

  loadingOffer = false;
  saving = false;
  errorMessage = '';
  dateError = '';

  constructor(
    private offerService: OfferService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Determinar si estamos editando o creando
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditing = true;
      this.offerId = parseInt(id, 10);
      this.loadOffer(this.offerId);
    }

    // Cargar catálogos para los selectores
    this.offerService.getOfferTypes().subscribe(types => this.offerTypes = types);
    this.offerService.getSectors().subscribe(sectors => this.sectors = sectors);
  }

  /** Carga los datos de la oferta a editar */
  loadOffer(id: number): void {
    this.loadingOffer = true;

    this.offerService.getOfferById(id).subscribe({
      next: (offer) => {
        this.formData = {
          title: offer.title,
          description: offer.description,
          offerTypeId: offer.offerTypeId,
          sectorId: offer.sectorId,
        };
        // Convertir ISO 8601 a formato datetime-local (quitar los segundos)
        // "2024-07-01T00:00:00" → "2024-07-01T00:00"
        this.startsAtLocal = offer.startsAt.substring(0, 16);
        this.endsAtLocal = offer.endsAt.substring(0, 16);
        this.loadingOffer = false;
      },
      error: () => {
        this.loadingOffer = false;
        this.router.navigate(['/offers']);
      }
    });
  }

  /** Envía el formulario al backend */
  onSubmit(): void {
    this.errorMessage = '';
    this.dateError = '';

    // Validación de fechas en cliente (Requerimiento 3.2)
    if (this.startsAtLocal && this.endsAtLocal) {
      if (new Date(this.startsAtLocal) >= new Date(this.endsAtLocal)) {
        this.dateError = 'La fecha de inicio debe ser anterior a la fecha de fin.';
        return;
      }
    }

    // Construir el request con el formato correcto para el backend
    // datetime-local: "2024-07-01T00:00" → backend espera: "2024-07-01T00:00:00"
    const request: CreateOfferRequest = {
      title: this.formData.title!,
      description: this.formData.description || null,
      offerTypeId: Number(this.formData.offerTypeId),
      sectorId: Number(this.formData.sectorId),
      startsAt: this.startsAtLocal + ':00',   // Agregar segundos
      endsAt: this.endsAtLocal + ':00',
    };

    this.saving = true;

    const operation = this.isEditing
      ? this.offerService.updateOffer(this.offerId!, request)
      : this.offerService.createOffer(request);

    operation.subscribe({
      next: () => {
        // Redirigir al listado después de guardar
        this.router.navigate(['/offers']);
      },
      error: (err: HttpErrorResponse) => {
        this.saving = false;
        if (err.error?.message) {
          this.errorMessage = err.error.message;
        } else {
          this.errorMessage = 'Error al guardar la oferta. Intente nuevamente.';
        }
      }
    });
  }
}
