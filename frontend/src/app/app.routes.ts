import { Routes } from '@angular/router';
import { authGuard, adminGuard, guestGuard } from './guards/auth.guard';

/**
 * app.routes.ts — Definición de rutas de la aplicación.
 *
 * ¿Qué es el Router de Angular?
 * Es el sistema de navegación de Angular. Mapea URLs a componentes.
 * Cuando el usuario navega a /dashboard, Angular renderiza DashboardComponent.
 *
 * Lazy loading:
 * Usamos () => import(...) para cargar los componentes de forma diferida.
 * Esto significa que el código de cada componente solo se descarga cuando
 * el usuario navega a esa ruta, reduciendo el tamaño del bundle inicial.
 *
 * canActivate: guards que se ejecutan antes de activar la ruta.
 * - authGuard: verifica que el usuario esté autenticado
 * - adminGuard: verifica que el usuario sea ADMIN
 */
export const routes: Routes = [
  // Ruta raíz: redirigir al dashboard
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full'
  },

  // Login: ruta pública — redirige al dashboard si ya está autenticado
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./components/login/login.component').then(m => m.LoginComponent)
  },

  // Dashboard: requiere autenticación
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./components/dashboard/dashboard.component').then(m => m.DashboardComponent)
  },

  // Listado de ofertas: requiere autenticación
  {
    path: 'offers',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./components/offers/offer-list.component').then(m => m.OfferListComponent)
  },

  // Crear oferta: requiere autenticación
  {
    path: 'offers/new',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./components/offers/offer-form.component').then(m => m.OfferFormComponent)
  },

  // Editar oferta: requiere autenticación
  {
    path: 'offers/:id/edit',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./components/offers/offer-form.component').then(m => m.OfferFormComponent)
  },

  // Gestión de usuarios: solo ADMIN
  {
    path: 'users',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./components/users/user-list.component').then(m => m.UserListComponent)
  },

  // Ruta comodín: redirigir al dashboard si la URL no existe
  {
    path: '**',
    redirectTo: '/dashboard'
  }
];
