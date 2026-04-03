import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from './services/auth.service';

/**
 * AppComponent — Componente raíz de la aplicación.
 *
 * Es el componente que se renderiza en <app-root> del index.html.
 * Contiene el layout principal: navbar + contenido de la ruta activa.
 *
 * <router-outlet>: el "slot" donde Angular renderiza el componente
 * de la ruta activa. Cuando el usuario navega a /dashboard,
 * Angular renderiza DashboardComponent dentro de <router-outlet>.
 *
 * RouterLink: directiva para navegación sin recargar la página.
 * Equivalente a <a href="..."> pero usando el Router de Angular.
 *
 * RouterLinkActive: agrega una clase CSS cuando la ruta está activa.
 * Útil para resaltar el ítem del menú de la página actual.
 */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <!-- Mostrar navbar solo si el usuario está autenticado -->
    <nav class="navbar" *ngIf="authService.isAuthenticated()">
      <div class="navbar-brand">
        <span class="brand-icon"></span>
        <span class="brand-name">Eazi Offers</span>
      </div>

      <div class="navbar-links">
        <!-- routerLink: navega a la ruta sin recargar la página -->
        <!-- routerLinkActive: agrega la clase 'active' cuando la ruta está activa -->
        <a routerLink="/dashboard" routerLinkActive="active" class="nav-link">
          Dashboard
        </a>
        <a routerLink="/offers" routerLinkActive="active" class="nav-link">
          Ofertas
        </a>
        <!-- Solo mostrar "Usuarios" si el usuario es ADMIN -->
        <a
          *ngIf="authService.isAdmin()"
          routerLink="/users"
          routerLinkActive="active"
          class="nav-link"
        >
          Usuarios
        </a>
      </div>

      <div class="navbar-user">
        <span class="user-role">{{ authService.getRole() }}</span>
        <button class="btn btn-secondary btn-sm" (click)="logout()">
          Cerrar sesión
        </button>
      </div>
    </nav>

    <!-- Contenido principal: aquí se renderiza el componente de la ruta activa -->
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
    }

    .brand-icon { font-size: 20px; }

    .navbar-links {
      display: flex;
      gap: 4px;
    }

    .nav-link {
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

    .navbar-user {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .user-role {
      font-size: 12px;
      font-weight: 600;
      padding: 2px 8px;
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
  // Inyectamos AuthService como público para usarlo en el template
  constructor(public authService: AuthService) {}

  logout(): void {
    this.authService.logout();
  }
}
