import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Offer, CreateOfferRequest, UpdateOfferRequest,
  PageResponse, DashboardData, OfferFilters,
  OfferType, Sector
} from '../models/offer.model';
import { environment } from '../../environments/environment';

/**
 * OfferService — Servicio que encapsula todas las llamadas HTTP a la API de ofertas.
 *
 * ¿Por qué centralizar las llamadas HTTP en un servicio?
 * 1. Reutilización: múltiples componentes pueden usar el mismo servicio.
 * 2. Mantenimiento: si cambia la URL del backend, solo cambia aquí.
 * 3. Testabilidad: podemos mockear el servicio en tests de componentes.
 * 4. Separación de responsabilidades: los componentes no saben de HTTP.
 *
 * HttpClient: el cliente HTTP de Angular. Retorna Observables.
 * Los componentes se suscriben a estos Observables para recibir los datos.
 */
@Injectable({
  providedIn: 'root'
})
export class OfferService {

  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // =========================================================================
  // OFERTAS
  // =========================================================================

  /**
   * Obtiene la lista paginada de ofertas con filtros opcionales.
   *
   * HttpParams: construye los query parameters de la URL.
   * Ejemplo: /api/offers?sectorId=5&status=ACTIVA&page=0&size=20
   */
  getOffers(filters: OfferFilters): Observable<PageResponse<Offer>> {
    let params = new HttpParams()
      .set('page', filters.page.toString())
      .set('size', filters.size.toString());

    // Solo agregar el parámetro si tiene un valor numérico válido.
    // Usar Number() y verificar que no sea NaN evita enviar "undefined" o "null"
    // como string al backend, lo que causaría un error de conversión de tipos.
    if (filters.sectorId != null && !isNaN(Number(filters.sectorId))) {
      params = params.set('sectorId', filters.sectorId.toString());
    }
    if (filters.offerTypeId != null && !isNaN(Number(filters.offerTypeId))) {
      params = params.set('offerTypeId', filters.offerTypeId.toString());
    }
    if (filters.status) {
      params = params.set('status', filters.status);
    }
    if (filters.startsAfter) {
      params = params.set('startsAfter', filters.startsAfter);
    }
    if (filters.endsBefore) {
      params = params.set('endsBefore', filters.endsBefore);
    }

    return this.http.get<PageResponse<Offer>>(`${this.apiUrl}/offers`, { params });
  }

  /** Obtiene una oferta por su ID */
  getOfferById(id: number): Observable<Offer> {
    return this.http.get<Offer>(`${this.apiUrl}/offers/${id}`);
  }

  /** Crea una nueva oferta */
  createOffer(request: CreateOfferRequest): Observable<Offer> {
    return this.http.post<Offer>(`${this.apiUrl}/offers`, request);
  }

  /** Actualiza una oferta existente */
  updateOffer(id: number, request: UpdateOfferRequest): Observable<Offer> {
    return this.http.put<Offer>(`${this.apiUrl}/offers/${id}`, request);
  }

  /**
   * Elimina una oferta (solo ADMIN).
   * void: el backend retorna HTTP 204 sin body.
   */
  deleteOffer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/offers/${id}`);
  }

  // =========================================================================
  // DASHBOARD
  // =========================================================================

  /** Obtiene los datos del dashboard */
  getDashboard(): Observable<DashboardData> {
    return this.http.get<DashboardData>(`${this.apiUrl}/dashboard`);
  }

  // =========================================================================
  // CATÁLOGOS
  // =========================================================================

  /** Obtiene los tipos de oferta para poblar el selector */
  getOfferTypes(): Observable<OfferType[]> {
    return this.http.get<OfferType[]>(`${this.apiUrl}/catalog/offer-types`);
  }

  /** Obtiene los sectores para poblar el selector */
  getSectors(): Observable<Sector[]> {
    return this.http.get<Sector[]>(`${this.apiUrl}/catalog/sectors`);
  }
}
