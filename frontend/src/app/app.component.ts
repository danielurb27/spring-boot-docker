import { Component, OnInit, Renderer2 } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from './services/auth.service';

/**
 * AppComponent — Componente raíz de la aplicación.
 * Contiene el layout principal: navbar + contenido de la ruta activa.
 *
 * Dark Mode:
 * Al hacer clic en el botón de tema, se agrega/quita la clase 'dark' en el <body>.
 * Los estilos dark mode están definidos en styles.css bajo el selector body.dark.
 * La preferencia se persiste en localStorage para que sobreviva recargas.
 *
 * Responsive / Hamburger Menu:
 * En móvil/tablet (≤768px) se muestra el botón hamburguesa que colapsa/expande
 * el menú de navegación. El estado se controla con menuOpen.
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
        <span class="brand-name"></span>
      </div>

      <!-- Botón hamburguesa (visible solo en móvil/tablet) -->
      <button
        class="hamburger-btn"
        (click)="toggleMenu()"
        [attr.aria-label]="menuOpen ? 'Cerrar menú' : 'Abrir menú'"
      >
        {{ menuOpen ? '✕' : '☰' }}
      </button>

      <!-- Links de navegación -->
      <div class="navbar-links" [class.open]="menuOpen">
        <a routerLink="/dashboard" routerLinkActive="active" class="nav-link" (click)="closeMenu()">
          <img src="assets/images/icons/nav/ic-dashboard.svg" alt="" width="16" height="16" class="nav-icon">
          Dashboard
        </a>
        <a routerLink="/offers" routerLinkActive="active" class="nav-link" (click)="closeMenu()">
          <img src="assets/images/icons/nav/ic-offers.svg" alt="" width="16" height="16" class="nav-icon">
          Ofertas
        </a>
        <a
          *ngIf="authService.isAdmin()"
          routerLink="/users"
          routerLinkActive="active"
          class="nav-link"
          (click)="closeMenu()"
        >
          <img src="assets/images/icons/nav/ic-users.svg" alt="" width="16" height="16" class="nav-icon">
          Usuarios
        </a>
      </div>

      <!-- Controles de usuario -->
      <div class="navbar-user">
        <span class="user-role">{{ roleLabel() }}</span>

        <!-- Botón toggle Dark Mode / Light Mode -->
        <button
          class="btn-theme-toggle"
          (click)="toggleDarkMode()"
          [title]="isDark ? 'Cambiar a modo claro' : 'Cambiar a modo oscuro'"
          [attr.aria-label]="isDark ? 'Modo claro' : 'Modo oscuro'"
        >
          <img
            [src]="isDark ? 'assets/images/icons/actions/ic-white.svg' : 'assets/images/icons/actions/ic-dark.svg'"
            [alt]="isDark ? 'Modo claro' : 'Modo oscuro'"
            width="18" height="18"
          >
        </button>

        <button class="btn btn-secondary btn-sm" (click)="logout()">
          <img src="assets/images/icons/nav/ic-logout.svg" alt="" width="14" height="14">
          <span class="btn-logout-text">Cerrar sesión</span>
        </button>
      </div>

    </nav>

    <!-- Contenido principal -->
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
      transition: background 0.2s, border-color 0.2s;
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

    .brand-logo {
      height: 32px;
      width: auto;
      object-fit: contain;
    }

    .navbar-links {
      display: flex;
      gap: 4px;
      flex: 1;
      justify-content: center;
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

    .nav-icon {
      opacity: 0.7;
      flex-shrink: 0;
    }

    .navbar-user {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .user-role {
      font-size: 12px;
      font-weight: 600;
      padding: 2px 10px;
      background: #f8f9fa;
      border-radius: 12px;
      color: #6c757d;
    }

    .btn-theme-toggle {
      background: none;
      border: 1px solid #dee2e6;
      border-radius: 8px;
      width: 34px;
      height: 34px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: background 0.2s, border-color 0.2s;
      padding: 0;
    }

    .btn-theme-toggle:hover {
      background: #f8f9fa;
    }

    .theme-icon {
      font-size: 16px;
      line-height: 1;
    }

    main.with-navbar {
      padding: 24px;
      max-width: 1200px;
      margin: 0 auto;
    }

    /* Botón hamburguesa: oculto en desktop */
    .hamburger-btn {
      display: none;
      background: none;
      border: 1px solid var(--color-border, #dee2e6);
      border-radius: 6px;
      font-size: 18px;
      cursor: pointer;
      padding: 4px 10px;
      color: inherit;
      line-height: 1;
    }

    /* Tablet y móvil: mostrar hamburguesa, colapsar links */
    @media (max-width: 768px) {
      .hamburger-btn { display: flex; align-items: center; }

      /* Navbar en una sola fila: brand | [user-controls + hamburger] */
      .navbar {
        position: relative;
        height: auto;
        min-height: 56px;
        padding: 0 16px;
        flex-wrap: nowrap;
      }

      /* Ocultar el label de rol en móvil para ahorrar espacio */
      .user-role { display: none; }

      /* Compactar el botón de logout: solo ícono, sin texto */
      .navbar-user .btn-logout-text { display: none; }

      .navbar-user {
        gap: 6px;
        flex-shrink: 0;
      }

      .navbar-links {
        display: none;
        position: absolute;
        top: 56px;
        left: 0;
        right: 0;
        background: white;
        border-bottom: 1px solid #dee2e6;
        flex-direction: column;
        padding: 8px 0;
        z-index: 99;
        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
      }

      .navbar-links.open {
        display: flex;
      }

      .nav-link {
        width: 100%;
        padding: 12px 24px;
        border-radius: 0;
      }
    }

    /* Dark mode: menú desplegado en móvil */
    @media (max-width: 768px) {
      :host-context(body.dark) .navbar-links {
        background: #1a1d27;
        border-bottom-color: #2e3347;
      }
    }
  `]
})
export class AppComponent implements OnInit {

  /** true cuando el modo oscuro está activo */
  isDark = false;

  /** true cuando el menú hamburguesa está abierto */
  menuOpen = false;

  constructor(
    public authService: AuthService,
    private renderer: Renderer2
  ) {}

  ngOnInit(): void {
    const saved = localStorage.getItem('theme');
    if (saved === 'dark') {
      this.isDark = true;
      this.renderer.addClass(document.body, 'dark');
    }
  }

  /** Alterna entre modo claro y oscuro */
  toggleDarkMode(): void {
    this.isDark = !this.isDark;

    if (this.isDark) {
      this.renderer.addClass(document.body, 'dark');
      localStorage.setItem('theme', 'dark');
    } else {
      this.renderer.removeClass(document.body, 'dark');
      localStorage.setItem('theme', 'light');
    }
  }

  /** Abre/cierra el menú hamburguesa */
  toggleMenu(): void {
    this.menuOpen = !this.menuOpen;
  }

  /** Cierra el menú hamburguesa */
  closeMenu(): void {
    this.menuOpen = false;
  }

  logout(): void {
    this.authService.logout();
  }

  roleLabel(): string {
    const role = this.authService.getRole();
    if (role === 'ADMIN') return 'Administrador';
    if (role === 'EMPLOYEE') return 'Empleado';
    return role ?? '';
  }
}
