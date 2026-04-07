# Plan de Implementación: Diseño Web Responsivo

## Visión General

Implementar RWD en la plataforma Easy Offers Management mediante cambios exclusivamente frontend (Angular 17, Standalone Components). Enfoque desktop-first con breakpoints en 480px (móvil) y 768px (tablet).

## Tareas

- [x] 1. Breakpoints globales y meta viewport
  - Verificar si `<meta name="viewport">` ya existe en `frontend/src/index.html`; agregarlo si no está presente
  - Agregar variables `--bp-mobile: 480px` y `--bp-tablet: 768px` al selector `:root` en `frontend/src/styles.css`
  - Agregar media queries globales para `main.with-navbar`: `padding: 16px` en `max-width: 768px` y `padding: 12px` en `max-width: 480px`
  - Agregar soporte dark mode para el menú desplegado del navbar: `body.dark .navbar-links { background: #1a1d27; border-bottom-color: #2e3347; }`
  - _Requerimientos: 1.1, 1.2, 1.3, 1.4, 1.5_

- [x] 2. Navbar responsiva con menú hamburguesa
  - [x] 2.1 Agregar lógica de estado y métodos al AppComponent
    - Agregar propiedad `menuOpen = false` en la clase `AppComponent`
    - Agregar método `toggleMenu(): void { this.menuOpen = !this.menuOpen; }`
    - Agregar método `closeMenu(): void { this.menuOpen = false; }`
    - Archivo: `frontend/src/app/app.component.ts`
    - _Requerimientos: 2.3, 2.4, 2.5_

  - [ ]* 2.2 Escribir property test: toggle del menú es idempotente en pares
    - **Propiedad 2: Toggle del menú hamburguesa es idempotente en pares**
    - **Valida: Requerimientos 2.3, 2.4**
    - Usar `fc.boolean()` para generar estado inicial aleatorio; verificar que `toggleMenu()` dos veces restaura el estado original

  - [ ]* 2.3 Escribir property test: closeMenu siempre resulta en false
    - **Propiedad 3: Navegar cierra el menú**
    - **Valida: Requerimiento 2.5**
    - Usar `fc.boolean()` para generar estado inicial; verificar que `closeMenu()` siempre produce `menuOpen === false`

  - [x] 2.4 Actualizar el template del AppComponent
    - Agregar botón `<button class="hamburger-btn" (click)="toggleMenu()">☰</button>` visible solo en móvil/tablet
    - Agregar `[class.open]="menuOpen"` en el elemento `.navbar-links`
    - Agregar `(click)="closeMenu()"` en cada `<a routerLink>` del navbar
    - Archivo: `frontend/src/app/app.component.ts`
    - _Requerimientos: 2.1, 2.2, 2.3, 2.5, 2.6, 2.7_

  - [x] 2.5 Agregar estilos responsivos al AppComponent
    - Agregar estilos para `.hamburger-btn`: `display: none` en desktop, `display: flex` en `max-width: 768px`
    - Agregar estilos para `.navbar-links`: posición absoluta, `display: none` por defecto en móvil, `display: flex` cuando tiene clase `.open`
    - Agregar estilos para `.nav-link` en móvil: `width: 100%`, `padding: 12px 24px`, `border-radius: 0`
    - Agregar `position: relative` al `.navbar` en móvil
    - Archivo: `frontend/src/app/app.component.ts`
    - _Requerimientos: 2.1, 2.2, 2.6, 2.8_

- [x] 3. Checkpoint — Verificar navbar
  - Asegurar que todos los tests pasan. Consultar al usuario si surgen dudas.

- [x] 4. Dashboard responsivo
  - [x] 4.1 Actualizar estilos del stats-grid en DashboardComponent
    - Reemplazar `auto-fit` por `repeat(4, 1fr)` en `.stats-grid` para desktop
    - Agregar `@media (max-width: 768px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } }`
    - Agregar `@media (max-width: 480px) { .stats-grid { grid-template-columns: 1fr; } }`
    - Verificar que `.offers-grid` ya tiene el media query para tablet; mantenerlo
    - Archivo: `frontend/src/app/components/dashboard/dashboard.component.ts`
    - _Requerimientos: 3.1, 3.2, 3.3, 3.4, 3.5_

  - [ ]* 4.2 Escribir property test: cada componente define media queries para todos los breakpoints
    - **Propiedad 1: Cada componente define media queries para todos los breakpoints**
    - **Valida: Requerimientos 1.4, 1.5**
    - Verificar que los estilos del componente contienen al menos un bloque `@media (max-width: 480px)` y uno `@media (max-width: 768px)`

- [x] 5. Tabla de ofertas responsiva
  - [x] 5.1 Actualizar el template de OfferListComponent
    - Envolver la tabla existente en `<div class="table-wrapper">` para scroll horizontal en tablet
    - Agregar bloque `<div class="offer-cards">` con tarjetas `.offer-card` alternativas para móvil (oculto en desktop/tablet)
    - Cada `.offer-card` debe contener: `.offer-title-cell`, badge de estado, `.offer-card-dates`, `.offer-card-actions` con botones Editar y Eliminar
    - Archivo: `frontend/src/app/components/offers/offer-list.component.ts`
    - _Requerimientos: 4.1, 4.2, 4.3, 4.4, 4.5_

  - [x] 5.2 Agregar estilos responsivos a OfferListComponent
    - Agregar `.table-wrapper { overflow-x: auto; -webkit-overflow-scrolling: touch; }`
    - Agregar `.offer-cards { display: none; }` (oculto por defecto)
    - Agregar `@media (max-width: 480px)`: ocultar `.table-wrapper`, mostrar `.offer-cards` con `flex-direction: column; gap: 12px`
    - Agregar estilos para `.offer-card`, `.offer-card-header`, `.offer-card-dates`, `.offer-card-actions` dentro del media query móvil
    - Agregar `@media (max-width: 480px) { .filters-grid { grid-template-columns: 1fr; } }`
    - Archivo: `frontend/src/app/components/offers/offer-list.component.ts`
    - _Requerimientos: 4.2, 4.3, 4.6, 4.7_

  - [ ]* 5.3 Escribir property test: tarjetas de oferta contienen todos los campos requeridos
    - **Propiedad 5: Las tarjetas de oferta contienen todos los campos requeridos**
    - **Valida: Requerimientos 4.2, 4.4**
    - Generar arrays de ofertas con `fc.array(arbitraryOffer(), { minLength: 1 })`; verificar que cada `.offer-card` contiene `.offer-title-cell`, `.badge`, `.offer-card-dates` y `.offer-card-actions`

- [x] 6. Checkpoint — Verificar tabla de ofertas
  - Asegurar que todos los tests pasan. Consultar al usuario si surgen dudas.

- [x] 7. Formulario de oferta responsivo
  - [x] 7.1 Actualizar estilos de OfferFormComponent
    - Cambiar el breakpoint existente de `600px` a `480px` en el media query de `.form-row`
    - Agregar dentro del `@media (max-width: 480px)`: `.form-actions { flex-direction: column; }` y `.form-actions .btn { width: 100%; justify-content: center; }`
    - Archivo: `frontend/src/app/components/offers/offer-form.component.ts`
    - _Requerimientos: 5.1, 5.2, 5.3, 5.4, 5.5_

  - [ ]* 7.2 Escribir property test: campos del formulario colapsan a una columna en móvil
    - **Propiedad 7: Los campos del formulario colapsan a una columna en móvil**
    - **Valida: Requerimientos 5.2, 5.3**
    - Verificar que los estilos del componente contienen `grid-template-columns: 1fr` dentro de un bloque `@media (max-width: 480px)`

- [x] 8. Modal de usuarios responsivo
  - [x] 8.1 Agregar estilos responsivos a UserListComponent
    - Agregar `@media (max-width: 768px) { .modal-card { max-width: calc(100% - 32px); } }`
    - Agregar `@media (max-width: 480px) { .modal-card { max-height: 90vh; overflow-y: auto; } .form-row { grid-template-columns: 1fr; } }`
    - Archivo: `frontend/src/app/components/users/user-list.component.ts`
    - _Requerimientos: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6_

  - [ ]* 8.2 Escribir property test: modal ocupa el ancho disponible en pantallas pequeñas
    - **Propiedad 9: El modal ocupa el ancho disponible en pantallas pequeñas**
    - **Valida: Requerimientos 6.2, 6.6**
    - Verificar que los estilos del componente contienen `max-width: calc(100% - 32px)` dentro de un bloque `@media (max-width: 768px)`

- [x] 9. Checkpoint final — Verificar todos los tests
  - Asegurar que todos los tests pasan. Consultar al usuario si surgen dudas.

## Notas

- Las tareas marcadas con `*` son opcionales y pueden omitirse para un MVP más rápido
- Cada tarea referencia los requerimientos específicos para trazabilidad
- Los media queries usan valores literales (`480px`, `768px`) ya que las variables CSS no son válidas dentro de `@media`
- Los tests de propiedades requieren `fast-check` (`npm install --save-dev fast-check` en el directorio `frontend`)
- Los tests visuales con media queries reales deben verificarse manualmente en el navegador
