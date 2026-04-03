import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { LoginRequest, LoginResponse, TokenPayload } from '../models/auth.model';
import { environment } from '../../environments/environment';

/**
 * AuthService — Servicio de autenticación del frontend.
 *
 * Responsabilidades:
 * 1. Hacer login contra el backend y guardar el JWT
 * 2. Proveer el token para que el interceptor lo adjunte a cada request
 * 3. Decodificar el JWT para obtener el rol del usuario
 * 4. Manejar el logout (limpiar el token)
 * 5. Verificar si el usuario está autenticado
 *
 * ¿Por qué localStorage?
 * localStorage persiste entre recargas de página y pestañas del mismo origen.
 * Alternativa: sessionStorage (se borra al cerrar la pestaña).
 * Para una app de gestión interna, localStorage es apropiado.
 *
 * Consideración de seguridad:
 * localStorage es vulnerable a ataques XSS (Cross-Site Scripting).
 * Para mayor seguridad, se podría usar cookies HttpOnly (no accesibles por JS).
 * Para el MVP de una app interna, localStorage es aceptable.
 *
 * @Injectable({ providedIn: 'root' }): Angular crea una sola instancia
 * de este servicio para toda la aplicación (singleton).
 * Cualquier componente que lo inyecte recibe la misma instancia.
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {

  /** Clave usada para guardar el token en localStorage */
  private readonly TOKEN_KEY = 'easy_offers_token';

  private readonly apiUrl = environment.apiUrl;

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  /**
   * Hace login contra el backend y guarda el JWT en localStorage.
   *
   * Observable y tap():
   * HttpClient retorna Observables (programación reactiva con RxJS).
   * tap() ejecuta un efecto secundario (guardar el token) sin modificar
   * el valor del Observable. El componente que llame a login() recibirá
   * el LoginResponse completo.
   *
   * @param credentials username y password
   * @returns Observable<LoginResponse> con el token y fecha de expiración
   */
  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/auth/login`, credentials)
      .pipe(
        tap(response => {
          // Guardar el token en localStorage al recibir la respuesta exitosa
          localStorage.setItem(this.TOKEN_KEY, response.token);
        })
      );
  }

  /**
   * Cierra la sesión: elimina el token y redirige al login.
   */
  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    this.router.navigate(['/login']);
  }

  /**
   * Retorna el token JWT almacenado, o null si no hay sesión activa.
   * Usado por el AuthInterceptor para adjuntar el token a cada request.
   */
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Verifica si el usuario está autenticado (tiene un token válido y no expirado).
   *
   * Nota: solo verificamos la expiración localmente.
   * La validación real de la firma la hace el backend.
   * Si el token está expirado, el backend retornará 401 y el interceptor
   * redirigirá al login.
   */
  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) return false;

    try {
      const payload = this.decodeToken(token);
      // exp es un timestamp Unix en segundos; Date.now() está en milisegundos
      return payload.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  }

  /**
   * Retorna el rol del usuario autenticado ('ADMIN' o 'EMPLOYEE').
   * Retorna null si no hay sesión activa.
   */
  getRole(): string | null {
    const token = this.getToken();
    if (!token) return null;

    try {
      return this.decodeToken(token).role;
    } catch {
      return null;
    }
  }

  /**
   * Verifica si el usuario tiene rol ADMIN.
   */
  isAdmin(): boolean {
    return this.getRole() === 'ADMIN';
  }

  /**
   * Decodifica el payload del JWT sin verificar la firma.
   *
   * ¿Cómo funciona?
   * Un JWT tiene el formato: header.payload.signature
   * El payload está codificado en Base64URL (no encriptado).
   * Podemos decodificarlo en el frontend para leer los claims.
   *
   * IMPORTANTE: No verificamos la firma aquí.
   * La verificación de firma la hace el backend en cada request.
   * En el frontend solo leemos los claims para UX (mostrar el rol, etc.).
   *
   * @param token El JWT completo
   * @returns El payload decodificado como objeto TypeScript
   */
  private decodeToken(token: string): TokenPayload {
    // El JWT tiene 3 partes separadas por puntos
    const parts = token.split('.');
    if (parts.length !== 3) {
      throw new Error('Token JWT inválido');
    }

    // La segunda parte es el payload en Base64URL
    // Base64URL usa - y _ en lugar de + y / del Base64 estándar
    const base64Url = parts[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');

    // Decodificar Base64 a string JSON y parsear
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );

    return JSON.parse(jsonPayload) as TokenPayload;
  }
}
