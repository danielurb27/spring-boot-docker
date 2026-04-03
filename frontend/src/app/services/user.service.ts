import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User, CreateUserRequest, AuditLog } from '../models/user.model';
import { environment } from '../../environments/environment';

/**
 * UserService — Servicio para gestión de usuarios y auditoría.
 *
 * Todos los endpoints que consume este servicio requieren rol ADMIN.
 * El guard adminGuard protege las rutas que usan este servicio,
 * y el interceptor agrega el JWT automáticamente.
 */
@Injectable({
  providedIn: 'root'
})
export class UserService {

  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  /** Lista todos los usuarios del sistema (solo ADMIN). GET /api/users */
  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/users`);
  }

  /** Crea un nuevo usuario (solo ADMIN). POST /api/users */
  createUser(request: CreateUserRequest): Observable<User> {
    return this.http.post<User>(`${this.apiUrl}/users`, request);
  }

  /**
   * Desactiva un usuario (solo ADMIN).
   * PATCH /api/users/{id}/deactivate
   */
  deactivateUser(id: number): Observable<User> {
    return this.http.patch<User>(`${this.apiUrl}/users/${id}/deactivate`, {});
  }

  /**
   * Obtiene el historial de auditoría de una oferta (solo ADMIN).
   * GET /api/offers/{id}/audit
   */
  getAuditHistory(offerId: number): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(`${this.apiUrl}/offers/${offerId}/audit`);
  }
}
