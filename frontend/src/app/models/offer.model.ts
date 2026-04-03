/**
 * offer.model.ts — Modelos TypeScript para ofertas y catálogos.
 *
 * Estas interfaces mapean exactamente a los DTOs del backend:
 * - Offer → OfferResponse.java
 * - CreateOfferRequest → CreateOfferRequest.java
 * - DashboardData → DashboardResponse.java
 * - OfferType → CatalogController.OfferTypeResponse
 * - Sector → CatalogController.SectorResponse
 */

/** Tipos de estado posibles para una oferta */
export type OfferStatus = 'PROXIMA' | 'ACTIVA' | 'VENCIDA';

/** Representa una oferta tal como la retorna el backend */
export interface Offer {
  id: number;
  title: string;
  description: string | null;
  offerTypeId: number;
  sectorId: number;
  status: OfferStatus;
  startsAt: string;    // ISO 8601 LocalDateTime: "2024-07-01T00:00:00"
  endsAt: string;
  createdBy: number | null;
  updatedBy: number | null;
  createdAt: string;   // ISO 8601 Instant: "2024-07-01T10:30:00Z"
  updatedAt: string | null;
}

/** Datos para crear una nueva oferta */
export interface CreateOfferRequest {
  title: string;
  description: string | null;
  offerTypeId: number;
  sectorId: number;
  startsAt: string;    // Formato: "2024-07-01T00:00:00"
  endsAt: string;
}

/** Datos para editar una oferta existente (mismos campos que crear) */
export interface UpdateOfferRequest extends CreateOfferRequest {}

/** Respuesta paginada de Spring Data Page<T> */
export interface PageResponse<T> {
  content: T[];           // Los registros de esta página
  totalElements: number;  // Total de registros en todas las páginas
  totalPages: number;     // Total de páginas
  number: number;         // Número de página actual (0-indexed)
  size: number;           // Tamaño de página
  first: boolean;         // ¿Es la primera página?
  last: boolean;          // ¿Es la última página?
}

/** Datos del dashboard */
export interface DashboardData {
  activeCount: number;
  upcomingCount: number;
  expiredCount: number;
  recentActive: Offer[];
  recentUpcoming: Offer[];
}

/** Tipo de oferta del catálogo */
export interface OfferType {
  id: number;
  name: string;
  code: string;
}

/** Sector del catálogo */
export interface Sector {
  id: number;
  code: string | null;   // null para "Hacks and Racks"
  name: string;
}

/** Filtros para el listado de ofertas */
export interface OfferFilters {
  sectorId?: number;
  offerTypeId?: number;
  status?: OfferStatus;
  startsAfter?: string;
  endsBefore?: string;
  page: number;
  size: number;
}
