/**
 * auth.model.ts — Modelos TypeScript para autenticación.
 *
 * ¿Qué son las interfaces en TypeScript?
 * Son contratos que definen la forma de un objeto.
 * A diferencia de Java, TypeScript usa "duck typing": si un objeto
 * tiene las propiedades correctas, es del tipo correcto.
 *
 * Estas interfaces deben coincidir exactamente con los DTOs del backend:
 * - LoginRequest → LoginRequest.java
 * - LoginResponse → LoginResponse.java
 * - TokenPayload → los claims del JWT
 */

/** Datos que enviamos al backend para hacer login */
export interface LoginRequest {
  username: string;
  password: string;
}

/** Respuesta del backend al hacer login exitoso */
export interface LoginResponse {
  token: string;       // El JWT que usaremos en cada request
  expiresAt: string;   // ISO 8601: "2024-07-15T18:30:00Z"
  tokenType: string;   // Siempre "Bearer"
}

/**
 * Payload decodificado del JWT.
 * El JWT tiene tres partes: header.payload.signature
 * El payload contiene estos claims que podemos leer en el frontend.
 *
 * Nota: nunca verificamos la firma del JWT en el frontend.
 * La verificación la hace el backend en cada request.
 * En el frontend solo leemos los claims para saber el rol del usuario.
 */
export interface TokenPayload {
  sub: string;    // userId como string (claim estándar JWT)
  role: string;   // "ADMIN" o "EMPLOYEE"
  iat: number;    // issued at (timestamp Unix)
  exp: number;    // expiration (timestamp Unix)
}
