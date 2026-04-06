/**
 * user.model.ts — Modelos TypeScript para usuarios y auditoría.
 *
 * Mapean exactamente a los DTOs del backend:
 * - User → UserResponse.java
 * - CreateUserRequest → CreateUserRequest.java
 * - AuditLog → AuditLogResponse.java
 */

/** Representa un usuario tal como lo retorna el backend */
export interface User {
  id: number;
  fullName: string;
  username: string;
  role: 'ADMIN' | 'EMPLOYEE';
  isActive: boolean;
  createdAt: string;
}

/** Datos para crear un nuevo usuario */
export interface CreateUserRequest {
  fullName: string;
  username: string;
  password: string;
  role: string;
}

/** Datos para modificar un usuario existente */
export interface UpdateUserRequest {
  fullName: string;
  password?: string;  // Opcional: si no se envía, no se cambia
  role: string;
}

/** Tipos de cambio posibles en la auditoría */
export type ChangeType = 'CREATE' | 'UPDATE' | 'DELETE' | 'AUTO_DELETE';

/** Representa un registro de auditoría */
export interface AuditLog {
  id: number;
  offerId: number | null;
  changedBy: number | null;
  changeType: ChangeType;
  fieldChanged: string | null;
  oldValue: string | null;
  newValue: string | null;
  observation: string | null;
  createdAt: string;
}
