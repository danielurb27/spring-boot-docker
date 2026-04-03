import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { HttpErrorResponse } from '@angular/common/http';

/**
 * LoginComponent — Pantalla de inicio de sesión.
 *
 * ¿Qué es un componente Angular?
 * Es la unidad básica de la UI en Angular. Combina:
 * - Template (HTML): la vista
 * - Clase TypeScript: la lógica
 * - Estilos CSS: el diseño
 *
 * @Component: decorador que marca la clase como componente Angular.
 * - selector: el tag HTML que representa este componente (<app-login>)
 * - standalone: true = no necesita un NgModule (Angular 17+)
 * - imports: dependencias que este componente necesita
 * - template: el HTML del componente (inline en este caso)
 * - styles: CSS encapsulado (solo aplica a este componente)
 *
 * Requerimiento: 1.1, 1.2
 */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <!-- Contenedor centrado en la pantalla -->
    <div class="login-container">
      <div class="login-card">

        <!-- Logo y título -->
        <div class="login-header">
          <div class="logo">🏪</div>
          <h1>Eazi</h1>
          <p>Gestión de Ofertas</p>
        </div>

        <!-- Mensaje de error (visible solo si hay error) -->
        <div class="alert alert-error" *ngIf="errorMessage">
          {{ errorMessage }}
        </div>

        <!-- Formulario de login -->
        <!-- (ngSubmit): ejecuta onSubmit() cuando se envía el formulario -->
        <!-- #loginForm="ngForm": referencia al formulario para validación -->
        <form (ngSubmit)="onSubmit()" #loginForm="ngForm">

          <div class="form-group">
            <label class="form-label" for="username">Usuario</label>
            <!-- [(ngModel)]: two-way binding — sincroniza el input con la propiedad username -->
            <!-- required: validación HTML5 + Angular -->
            <input
              id="username"
              type="text"
              class="form-control"
              [(ngModel)]="username"
              name="username"
              required
              placeholder="Ingrese su usuario"
              [disabled]="loading"
            />
          </div>

          <div class="form-group">
            <label class="form-label" for="password">Contraseña</label>
            <input
              id="password"
              type="password"
              class="form-control"
              [(ngModel)]="password"
              name="password"
              required
              placeholder="Ingrese su contraseña"
              [disabled]="loading"
            />
          </div>

          <!-- Botón de submit -->
          <!-- [disabled]: deshabilita el botón si el formulario es inválido o está cargando -->
          <button
            type="submit"
            class="btn btn-primary btn-full"
            [disabled]="loginForm.invalid || loading"
          >
            <!-- Mostrar spinner mientras carga -->
            <span *ngIf="loading" class="spinner"></span>
            <span>{{ loading ? 'Iniciando sesión...' : 'Iniciar sesión' }}</span>
          </button>

        </form>
      </div>
    </div>
  `,
  styles: [`
    .login-container {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #e63946 0%, #c1121f 100%);
      padding: 16px;
    }

    .login-card {
      background: white;
      border-radius: 12px;
      padding: 40px;
      width: 100%;
      max-width: 400px;
      box-shadow: 0 20px 40px rgba(0,0,0,0.2);
    }

    .login-header {
      text-align: center;
      margin-bottom: 32px;
    }

    .logo {
      font-size: 48px;
      margin-bottom: 8px;
    }

    .login-header h1 {
      font-size: 24px;
      font-weight: 700;
      color: #212529;
      margin-bottom: 4px;
    }

    .login-header p {
      color: #6c757d;
      font-size: 14px;
    }

    .btn-full {
      width: 100%;
      justify-content: center;
      padding: 12px;
      font-size: 15px;
      margin-top: 8px;
    }
  `]
})
export class LoginComponent {

  /** Valores del formulario — sincronizados con los inputs via [(ngModel)] */
  username = '';
  password = '';

  /** Estado de carga — deshabilita el formulario mientras espera la respuesta */
  loading = false;

  /** Mensaje de error a mostrar al usuario */
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  /**
   * Se ejecuta cuando el usuario envía el formulario.
   *
   * Flujo:
   * 1. Mostrar spinner y limpiar errores anteriores
   * 2. Llamar al AuthService.login()
   * 3. Si éxito: redirigir al dashboard
   * 4. Si error 401: mostrar mensaje de credenciales inválidas
   * 5. Si otro error: mostrar mensaje genérico
   */
  onSubmit(): void {
    if (!this.username || !this.password) return;

    this.loading = true;
    this.errorMessage = '';

    this.authService.login({ username: this.username, password: this.password })
      .subscribe({
        next: () => {
          // Login exitoso → redirigir al dashboard
          this.router.navigate(['/dashboard']);
        },
        error: (err: HttpErrorResponse) => {
          this.loading = false;

          if (err.status === 401) {
            // Credenciales inválidas (Requerimiento 1.2)
            this.errorMessage = 'Usuario o contraseña incorrectos.';
          } else {
            this.errorMessage = 'Error al conectar con el servidor. Intente nuevamente.';
          }
        }
      });
  }
}
