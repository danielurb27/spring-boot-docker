import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../services/user.service';
import { User, CreateUserRequest, UpdateUserRequest } from '../../models/user.model';
import { HttpErrorResponse } from '@angular/common/http';

/**
 * UserListComponent — Gestión de usuarios del sistema (solo ADMIN).
 *
 * Funcionalidades:
 * 1. Formulario para crear nuevos usuarios (ADMIN o EMPLOYEE)
 * 2. Lista de todos los usuarios con opciones de:
 *    - Modificar datos (nombre, contraseña, rol)
 *    - Desactivar / Activar (toggle, sin borrar de la BD)
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

        <div class="alert alert-error" *ngIf="createError">{{ createError }}</div>
        <div class="alert alert-success" *ngIf="createSuccess">{{ createSuccess }}</div>

        <form (ngSubmit)="createUser()" #createForm="ngForm">
          <div class="form-row">

            <div class="form-group">
              <label class="form-label">Nombre completo *</label>
              <input type="text" class="form-control" [(ngModel)]="newUser.fullName"
                name="fullName" required placeholder="Ej: Juan Pérez" />
            </div>

            <div class="form-group">
              <label class="form-label">Usuario *</label>
              <input type="text" class="form-control" [(ngModel)]="newUser.username"
                name="username" required placeholder="Ej: jperez" />
            </div>

            <div class="form-group">
              <label class="form-label">Contraseña *</label>
              <input type="password" class="form-control" [(ngModel)]="newUser.password"
                name="password" required minlength="6" placeholder="Mínimo 6 caracteres" />
            </div>

            <div class="form-group">
              <label class="form-label">Rol *</label>
              <select class="form-control" [(ngModel)]="newUser.role" name="role" required>
                <option value="EMPLOYEE">Empleado</option>
                <option value="ADMIN">Administrador</option>
              </select>
            </div>

          </div>

          <div style="display: flex; justify-content: flex-end; margin-top: 8px;">
            <button type="submit" class="btn btn-primary" [disabled]="createForm.invalid || creating">
              <span *ngIf="creating" class="spinner"></span>
              {{ creating ? 'Creando...' : 'Crear usuario' }}
            </button>
          </div>
        </form>
      </div>

      <!-- ── Modal de edición ── -->
      <!-- Overlay oscuro que aparece cuando editingUser no es null -->
      <div class="modal-overlay" *ngIf="editingUser" (click)="cancelEdit()">
        <div class="modal-card" (click)="$event.stopPropagation()">

          <div class="modal-header">
            <h2>Modificar usuario</h2>
            <button class="modal-close" (click)="cancelEdit()">✕</button>
          </div>

          <div class="alert alert-error" *ngIf="editError">{{ editError }}</div>

          <form (ngSubmit)="saveEdit()" #editForm="ngForm">

            <div class="form-group">
              <label class="form-label">Nombre completo *</label>
              <input type="text" class="form-control" [(ngModel)]="editData.fullName"
                name="editFullName" required />
            </div>

            <!-- El username no se puede cambiar -->
            <div class="form-group">
              <label class="form-label">Usuario (no editable)</label>
              <input type="text" class="form-control" [value]="editingUser.username" disabled />
            </div>

            <div class="form-group">
              <label class="form-label">Nueva contraseña</label>
              <input type="password" class="form-control" [(ngModel)]="editData.password"
                name="editPassword" minlength="6"
                placeholder="Dejar vacío para no cambiar" />
            </div>

            <div class="form-group">
              <label class="form-label">Rol *</label>
              <select class="form-control" [(ngModel)]="editData.role" name="editRole" required>
                <option value="EMPLOYEE">Empleado</option>
                <option value="ADMIN">Administrador</option>
              </select>
            </div>

            <div class="modal-actions">
              <button type="button" class="btn btn-secondary" (click)="cancelEdit()">
                Cancelar
              </button>
              <button type="submit" class="btn btn-primary" [disabled]="editForm.invalid || saving">
                <span *ngIf="saving" class="spinner"></span>
                {{ saving ? 'Guardando...' : 'Guardar cambios' }}
              </button>
            </div>

          </form>
        </div>
      </div>

      <!-- ── Lista de usuarios ── -->
      <div class="card" *ngIf="users.length > 0">
        <h2 style="font-size: 16px; font-weight: 600; margin-bottom: 16px;">
          Usuarios del sistema
        </h2>

        <!-- Tabla (desktop + tablet con scroll) -->
        <div class="table-wrapper">
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
              <tr *ngFor="let user of users">
                <td>{{ user.fullName }}</td>
                <td>{{ user.username }}</td>
                <td>
                  <span [class]="user.role === 'ADMIN' ? 'badge badge-proxima' : 'badge badge-activa'">
                    {{ user.role === 'ADMIN' ? 'Administrador' : 'Empleado' }}
                  </span>
                </td>
                <td>
                  <span [class]="user.isActive ? 'badge badge-activa' : 'badge badge-vencida'">
                    {{ user.isActive ? 'Activo' : 'Inactivo' }}
                  </span>
                </td>
                <td>
                  <div class="action-buttons">
                    <button class="btn btn-secondary btn-sm" (click)="startEdit(user)">
                      <img src="assets/images/icons/actions/ic-edit.svg" alt="" width="13" height="13">
                      Modificar
                    </button>
                    <button *ngIf="user.isActive" class="btn btn-danger btn-sm"
                      (click)="deactivateUser(user)" [disabled]="togglingId === user.id">
                      {{ togglingId === user.id ? '...' : 'Desactivar' }}
                    </button>
                    <button *ngIf="!user.isActive" class="btn btn-success btn-sm"
                      (click)="activateUser(user)" [disabled]="togglingId === user.id">
                      {{ togglingId === user.id ? '...' : 'Activar' }}
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Tarjetas (solo móvil <480px) -->
        <div class="user-cards">
          <div class="user-card" *ngFor="let user of users">
            <div class="user-card-header">
              <div class="user-card-info">
                <span class="user-card-name">{{ user.fullName }}</span>
                <span class="user-card-username">&#64;{{ user.username }}</span>
              </div>
              <div class="user-card-badges">
                <span [class]="user.role === 'ADMIN' ? 'badge badge-proxima' : 'badge badge-activa'">
                  {{ user.role === 'ADMIN' ? 'Admin' : 'Empleado' }}
                </span>
                <span [class]="user.isActive ? 'badge badge-activa' : 'badge badge-vencida'">
                  {{ user.isActive ? 'Activo' : 'Inactivo' }}
                </span>
              </div>
            </div>
            <div class="user-card-actions">
              <button class="btn btn-secondary btn-sm" (click)="startEdit(user)">
                <img src="assets/images/icons/actions/ic-edit.svg" alt="" width="13" height="13">
                Modificar
              </button>
              <button *ngIf="user.isActive" class="btn btn-danger btn-sm"
                (click)="deactivateUser(user)" [disabled]="togglingId === user.id">
                {{ togglingId === user.id ? '...' : 'Desactivar' }}
              </button>
              <button *ngIf="!user.isActive" class="btn btn-success btn-sm"
                (click)="activateUser(user)" [disabled]="togglingId === user.id">
                {{ togglingId === user.id ? '...' : 'Activar' }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="card" *ngIf="users.length === 0 && !loading">
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
      grid-template-columns: repeat(2, 1fr);
      gap: 12px;
    }
    .form-row .form-group { margin-bottom: 0; }

    .action-buttons { display: flex; gap: 6px; flex-wrap: wrap; }

    /* Scroll horizontal en tablet */
    .table-wrapper {
      overflow-x: auto;
      -webkit-overflow-scrolling: touch;
    }

    /* Tarjetas de usuario: ocultas en desktop/tablet */
    .user-cards { display: none; }

    /* Botón verde para activar */
    .btn-success {
      background-color: #2d6a4f;
      color: white;
      border: none;
    }
    .btn-success:hover:not(:disabled) { background-color: #1b4332; }

    /* ── Modal de edición ── */
    .modal-overlay {
      position: fixed;
      inset: 0;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 200;
      padding: 16px;
    }

    .modal-card {
      background: white;
      border-radius: 10px;
      padding: 28px;
      width: 100%;
      max-width: 460px;
      box-shadow: 0 20px 60px rgba(0,0,0,0.3);
    }

    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
    }

    .modal-header h2 { font-size: 18px; font-weight: 600; }

    .modal-close {
      background: none;
      border: none;
      font-size: 18px;
      cursor: pointer;
      color: #6c757d;
      padding: 4px 8px;
      border-radius: 4px;
    }
    .modal-close:hover { background: #f8f9fa; }

    .modal-actions {
      display: flex;
      gap: 12px;
      justify-content: flex-end;
      margin-top: 8px;
      padding-top: 16px;
      border-top: 1px solid #dee2e6;
    }

    @media (max-width: 768px) {
      .modal-card {
        max-width: calc(100% - 32px);
      }
    }

    @media (max-width: 480px) {
      .form-row {
        grid-template-columns: 1fr;
      }

      /* Ocultar tabla, mostrar tarjetas */
      .table-wrapper { display: none; }
      .user-cards {
        display: flex;
        flex-direction: column;
        gap: 10px;
      }

      .user-card {
        background: var(--color-surface);
        border: 1px solid var(--color-border);
        border-radius: var(--border-radius);
        padding: 12px 14px;
      }

      .user-card-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        gap: 8px;
        margin-bottom: 10px;
      }

      .user-card-info {
        display: flex;
        flex-direction: column;
        gap: 2px;
      }

      .user-card-name {
        font-weight: 600;
        font-size: 14px;
      }

      .user-card-username {
        font-size: 12px;
        color: var(--color-text-muted);
      }

      .user-card-badges {
        display: flex;
        flex-direction: column;
        gap: 4px;
        align-items: flex-end;
      }

      .user-card-actions {
        display: flex;
        gap: 8px;
      }

      .modal-card {
        max-height: 90vh;
        overflow-y: auto;
      }

      .modal-actions {
        flex-direction: column;
      }

      .modal-actions .btn {
        width: 100%;
        justify-content: center;
      }
    }
  `]
})
export class UserListComponent implements OnInit {

  users: User[] = [];
  loading = false;

  /** Formulario de creación */
  newUser: CreateUserRequest = { fullName: '', username: '', password: '', role: 'EMPLOYEE' };
  creating = false;
  createError = '';
  createSuccess = '';

  /** Modal de edición — null cuando está cerrado */
  editingUser: User | null = null;
  editData: UpdateUserRequest = { fullName: '', password: '', role: 'EMPLOYEE' };
  saving = false;
  editError = '';

  /** ID del usuario cuyo estado se está cambiando (para deshabilitar el botón) */
  togglingId: number | null = null;

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.userService.getAllUsers().subscribe({
      next: (users) => { this.users = users; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  /** Crea un nuevo usuario */
  createUser(): void {
    this.createError = '';
    this.createSuccess = '';
    this.creating = true;

    this.userService.createUser(this.newUser).subscribe({
      next: (user) => {
        this.users = [...this.users, user];
        this.createSuccess = `Usuario "${user.username}" creado exitosamente.`;
        this.creating = false;
        this.newUser = { fullName: '', username: '', password: '', role: 'EMPLOYEE' };
        setTimeout(() => this.createSuccess = '', 3000);
      },
      error: (err: HttpErrorResponse) => {
        this.creating = false;
        this.createError = err.status === 409
          ? `El usuario "${this.newUser.username}" ya existe.`
          : err.error?.message || 'Error al crear el usuario.';
      }
    });
  }

  /** Abre el modal de edición con los datos actuales del usuario */
  startEdit(user: User): void {
    this.editingUser = user;
    this.editData = { fullName: user.fullName, password: '', role: user.role };
    this.editError = '';
  }

  cancelEdit(): void {
    this.editingUser = null;
    this.editError = '';
  }

  /** Guarda los cambios del modal de edición */
  saveEdit(): void {
    if (!this.editingUser) return;
    this.editError = '';
    this.saving = true;

    // Si la contraseña está vacía, no la enviamos al backend
    const request: UpdateUserRequest = {
      fullName: this.editData.fullName,
      role: this.editData.role,
      ...(this.editData.password ? { password: this.editData.password } : {})
    };

    this.userService.updateUser(this.editingUser.id, request).subscribe({
      next: (updated) => {
        // Actualizar el usuario en la lista local
        this.users = this.users.map(u => u.id === updated.id ? updated : u);
        this.saving = false;
        this.editingUser = null;
      },
      error: (err: HttpErrorResponse) => {
        this.saving = false;
        this.editError = err.error?.message || 'Error al guardar los cambios.';
      }
    });
  }

  /** Desactiva un usuario (soft delete — no borra de la BD) */
  deactivateUser(user: User): void {
    if (!confirm(`¿Desactivar al usuario "${user.username}"? No podrá iniciar sesión.`)) return;

    this.togglingId = user.id;
    this.userService.deactivateUser(user.id).subscribe({
      next: (updated) => {
        this.users = this.users.map(u => u.id === updated.id ? updated : u);
        this.togglingId = null;
      },
      error: () => { this.togglingId = null; }
    });
  }

  /** Reactiva un usuario previamente desactivado */
  activateUser(user: User): void {
    if (!confirm(`¿Activar al usuario "${user.username}"? Podrá volver a iniciar sesión.`)) return;

    this.togglingId = user.id;
    this.userService.activateUser(user.id).subscribe({
      next: (updated) => {
        this.users = this.users.map(u => u.id === updated.id ? updated : u);
        this.togglingId = null;
      },
      error: () => { this.togglingId = null; }
    });
  }
}
