import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../services/user.service';
import { User, CreateUserRequest } from '../../models/user.model';
import { HttpErrorResponse } from '@angular/common/http';

/**
 * UserListComponent — Gestión de usuarios del sistema (solo ADMIN).
 *
 * Funcionalidades:
 * 1. Formulario para crear nuevos usuarios (ADMIN o EMPLOYEE)
 * 2. Lista de usuarios con opción de desactivar
 *
 * Nota: el backend no tiene un endpoint GET /api/users para listar todos
 * los usuarios (no estaba en el MVP). Por eso mantenemos una lista local
 * que se actualiza al crear o desactivar usuarios en la sesión actual.
 *
 * En una versión futura se agregaría GET /api/users al backend.
 *
 * Requerimiento: 2.1, 2.3
 */
@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="users-page">

      <div class="page-header">
        <div>
          <h1 class="page-title">Gestión de Usuarios</h1>
          <p class="page-subtitle">Solo accesible para administradores</p>
        </div>
      </div>

      <!-- ── Formulario de creación ── -->
      <div class="card" style="margin-bottom: 24px;">
        <h2 style="font-size: 16px; font-weight: 600; margin-bottom: 16px;">
          Crear nuevo usuario
        </h2>

        <!-- Mensajes de feedback -->
        <div class="alert alert-error" *ngIf="createError">{{ createError }}</div>
        <div class="alert alert-success" *ngIf="createSuccess">{{ createSuccess }}</div>

        <form (ngSubmit)="createUser()" #createForm="ngForm">
          <div class="form-row">

            <div class="form-group">
              <label class="form-label">Nombre completo *</label>
              <input
                type="text"
                class="form-control"
                [(ngModel)]="newUser.fullName"
                name="fullName"
                required
                placeholder="Ej: Juan Pérez"
              />
            </div>

            <div class="form-group">
              <label class="form-label">Usuario *</label>
              <input
                type="text"
                class="form-control"
                [(ngModel)]="newUser.username"
                name="username"
                required
                placeholder="Ej: jperez"
              />
            </div>

            <div class="form-group">
              <label class="form-label">Contraseña *</label>
              <input
                type="password"
                class="form-control"
                [(ngModel)]="newUser.password"
                name="password"
                required
                minlength="6"
                placeholder="Mínimo 6 caracteres"
              />
            </div>

            <div class="form-group">
              <label class="form-label">Rol *</label>
              <select
                class="form-control"
                [(ngModel)]="newUser.role"
                name="role"
                required
              >
                <option value="EMPLOYEE">Empleado</option>
                <option value="ADMIN">Administrador</option>
              </select>
            </div>

          </div>

          <div style="display: flex; justify-content: flex-end; margin-top: 8px;">
            <button
              type="submit"
              class="btn btn-primary"
              [disabled]="createForm.invalid || creating"
            >
              <span *ngIf="creating" class="spinner"></span>
              {{ creating ? 'Creando...' : 'Crear usuario' }}
            </button>
          </div>
        </form>
      </div>

      <!-- ── Lista de usuarios creados en esta sesión ── -->
      <div class="card" *ngIf="createdUsers.length > 0">
        <h2 style="font-size: 16px; font-weight: 600; margin-bottom: 16px;">
          Usuarios creados en esta sesión
        </h2>

        <table class="table">
          <thead>
            <tr>
              <th>Nombre</th>
              <th>Usuario</th>
              <th>Rol</th>
              <th>Estado</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let user of createdUsers">
              <td>{{ user.fullName }}</td>
              <td>{{ user.username }}</td>
              <td>
                <!-- Badge de rol -->
                <span [class]="user.role === 'ADMIN' ? 'badge badge-proxima' : 'badge badge-activa'">
                  {{ user.role === 'ADMIN' ? 'Admin' : 'Empleado' }}
                </span>
              </td>
              <td>
                <span [class]="user.isActive ? 'badge badge-activa' : 'badge badge-vencida'">
                  {{ user.isActive ? 'Activo' : 'Inactivo' }}
                </span>
              </td>
              <td>
                <button
                  *ngIf="user.isActive"
                  class="btn btn-danger btn-sm"
                  (click)="deactivateUser(user)"
                  [disabled]="deactivatingId === user.id"
                >
                  {{ deactivatingId === user.id ? '...' : 'Desactivar' }}
                </button>
                <span *ngIf="!user.isActive" style="color: #6c757d; font-size: 13px;">
                  Desactivado
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Estado vacío -->
      <div class="card" *ngIf="createdUsers.length === 0">
        <p style="text-align: center; color: #6c757d; padding: 20px;">
          Usa el formulario de arriba para crear usuarios del sistema.
        </p>
      </div>

    </div>
  `,
  styles: [`
    .page-header { margin-bottom: 16px; }
    .page-title { font-size: 24px; font-weight: 700; margin-bottom: 4px; }
    .page-subtitle { color: #6c757d; font-size: 14px; }

    .form-row {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 12px;
    }

    .form-row .form-group { margin-bottom: 0; }
  `]
})
export class UserListComponent implements OnInit {

  /** Usuarios creados en esta sesión (lista local) */
  createdUsers: User[] = [];

  /** Datos del formulario de creación */
  newUser: CreateUserRequest = {
    fullName: '',
    username: '',
    password: '',
    role: 'EMPLOYEE'   // Rol por defecto: empleado
  };

  creating = false;
  deactivatingId: number | null = null;
  createError = '';
  createSuccess = '';

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    // Cargar todos los usuarios existentes desde el backend
    this.loadUsers();
  }

  /** Carga todos los usuarios del sistema */
  loadUsers(): void {
    this.userService.getAllUsers().subscribe({
      next: (users) => this.createdUsers = users,
      error: () => {} // silencioso si falla
    });
  }

  /** Crea un nuevo usuario */
  createUser(): void {
    this.createError = '';
    this.createSuccess = '';
    this.creating = true;

    this.userService.createUser(this.newUser).subscribe({
      next: (user) => {
        // Recargar la lista completa para mostrar el nuevo usuario
        this.loadUsers();
        this.createSuccess = `Usuario "${user.username}" creado exitosamente.`;
        this.creating = false;

        // Limpiar el formulario
        this.newUser = { fullName: '', username: '', password: '', role: 'EMPLOYEE' };

        // Limpiar el mensaje de éxito después de 3 segundos
        setTimeout(() => this.createSuccess = '', 3000);
      },
      error: (err: HttpErrorResponse) => {
        this.creating = false;
        if (err.status === 409) {
          this.createError = `El usuario "${this.newUser.username}" ya existe.`;
        } else {
          this.createError = err.error?.message || 'Error al crear el usuario.';
        }
      }
    });
  }

  /** Desactiva un usuario */
  deactivateUser(user: User): void {
    if (!confirm(`¿Desactivar al usuario "${user.username}"? No podrá iniciar sesión.`)) {
      return;
    }

    this.deactivatingId = user.id;

    this.userService.deactivateUser(user.id).subscribe({
      next: (updated) => {
        // Actualizar el usuario en la lista local
        const index = this.createdUsers.findIndex(u => u.id === user.id);
        if (index !== -1) {
          this.createdUsers[index] = updated;
        }
        this.deactivatingId = null;
      },
      error: () => {
        this.deactivatingId = null;
        alert('Error al desactivar el usuario.');
      }
    });
  }
}
