import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from './services/auth.service';

/**
 * AppComponent — Componente raíz de la aplicación.
 * Contiene el layout principal: navbar + contenido de la ruta activa.
 */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <!-- Navbar: visible solo cuando el usuario está autenticado -->
    <nav class="navbar" *ngIf="authService.isAuthenticated()">

      <!-- Brand: logo de Easy + nombre de la app -->
      <div class="navbar-brand">
        <img src="assets/images/logos/logo-easy.png" alt="Easy" class="brand-logo">
        <span class="brand-name">Eazi</span>
      </div>

      <!-- Links de navegación -->
      <div class="navbar-links">
        <a routerLink="/dashboard" routerLinkActive="active" class="nav-link">
          <img src="assets/images/icons/nav/ic-dashboard.svg" alt="" width="16" height="16" class="nav-icon">
          Dashboard
        </a>
        <a routerLink="/offers" routerLinkActive="active" class="nav-link">
          <img src="assets/images/icons/nav/ic-offers.svg" alt="" width="16" height="16" class="nav-icon">
          Ofertas
        </a>
        <!-- Solo visible para ADMIN -->
        <a
          *ngIf="authService.isAdmin()"
          routerLink="/users"
          routerLinkActive="active"
          class="nav-link"
        >
          <img src="assets/images/icons/nav/ic-users.svg" alt="" width="16" height="16" class="nav-icon">
          Usuarios
        </a>
      </div>

      <!-- Usuario actual + cerrar sesión -->
      <div class="navbar-user">
        <span class="user-role">{{ roleLabel() }}</span>
        <button class="btn btn-secondary btn-sm" (click)="logout()">
          <img src="assets/images/icons/nav/ic-logout.svg" alt="" width="14" height="14">
          Cerrar sesión
        </button>
      </div>

    </nav>

    <!-- Contenido principal: renderiza el componente de la ruta activa -->
    <main [class.with-navbar]="authService.isAuthenticated()">
      <router-outlet></router-outlet>
    </main>
  `,
  styles: [`
    .navbar {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 24px;
      height: 56px;
      background: white;
      border-bottom: 1px solid #dee2e6;
      box-shadow: 0 1px 3px rgba(0,0,0,0.1);
      position: sticky;
      top: 0;
      z-index: 100;
    }

    .navbar-brand {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 700;
      font-size: 16px;
      color: #e63946;
      text-decoration: none;
    }

    /* Logo de Easy en el navbar */
    .brand-logo {
      height: 32px;
      width: auto;
      object-fit: contain;
    }

    .navbar-links {
      display: flex;
      gap: 4px;
    }

    .nav-link {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 6px 14px;
      border-radius: 6px;
      text-decoration: none;
      color: #495057;
      font-size: 14px;
      font-weight: 500;
      transition: background-color 0.2s;
    }

    .nav-link:hover { background-color: #f8f9fa; }

    .nav-link.active {
      background-color: #fff0f0;
      color: #e63946;
    }

    /* Icono dentro del nav-link */
    .nav-icon {
      opacity: 0.7;
      flex-shrink: 0;
    }

    .navbar-user {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .user-role {
      font-size: 12px;
      font-weight: 600;
      padding: 2px 10px;
      background: #f8f9fa;
      border-radius: 12px;
      color: #6c757d;
    }

    main.with-navbar {
      padding: 24px;
      max-width: 1200px;
      margin: 0 auto;
    }
  `]
})
export class AppComponent {

  constructor(public authService: AuthService) {}

  logout(): void {
    this.authService.logout();
  }

  /** Convierte el rol técnico a etiqueta legible en español */
  roleLabel(): string {
    const role = this.authService.getRole();
    if (role === 'ADMIN') return 'Administrador';
    if (role === 'EMPLOYEE') return 'Empleado';
    return role ?? '';
  }
}
