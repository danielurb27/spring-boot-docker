# Documento de Diseño Técnico — Diseño Web Responsivo

## Visión General

Este documento describe el diseño técnico para implementar Diseño Web Responsivo (RWD) en la plataforma Easy Offers Management. La implementación es exclusivamente frontend (Angular 17, Standalone Components) y no requiere cambios en el backend.

El enfoque es **desktop-first con degradación progresiva**: los estilos base corresponden al layout de escritorio, y los media queries ajustan el layout para pantallas más pequeñas. Esta decisión se toma porque la aplicación ya tiene estilos desktop funcionales y el objetivo es extenderlos, no reescribirlos.

Los breakpoints definidos son:
- Móvil: `< 480px`
- Tablet: `480px – 768px`
- Desktop: `> 768px`

---

## Arquitectura

La implementación sigue la arquitectura existente del proyecto: estilos globales en `frontend/src/styles.css` y estilos de componente en el array `styles: []` del decorador `@Component`. No se introduce ninguna librería externa de CSS ni de componentes.

```
frontend/src/
├── index.html                          ← meta viewport (ya presente, verificar)
├── styles.css                          ← variables CSS de breakpoints + utilidades globales
└── app/
    ├── app.component.ts                ← Navbar responsiva + estado menuOpen
    └── components/
        ├── dashboard/dashboard.component.ts    ← Stats grid responsivo
        ├── offers/offer-list.component.ts      ← Tabla → tarjetas en móvil
        ├── offers/offer-form.component.ts      ← form-row 2col → 1col en móvil
        └── users/user-list.component.ts        ← Modal responsivo
```

El dark mode existente (`body.dark` en `styles.css`) es compatible con todos los cambios responsivos: los media queries se aplican independientemente de la clase `dark`.

---

## Componentes e Interfaces

### 1. Variables CSS globales (`styles.css`)

Se agregan dos variables de breakpoint al selector `:root` existente:

```css
:root {
  --bp-mobile: 480px;
  --bp-tablet: 768px;
  /* ... variables existentes ... */
}
```

> Nota: Las variables CSS no pueden usarse directamente dentro de `@media` queries (limitación del estándar CSS). Su valor está documentado aquí para referencia y consistencia, pero los media queries usan los valores literales `480px` y `768px`.

También se agrega una utilidad global para el contenedor responsivo del main:

```css
@media (max-width: 768px) {
  main.with-navbar {
    padding: 16px;
  }
}
@media (max-width: 480px) {
  main.with-navbar {
    padding: 12px;
  }
}
```

### 2. Navbar con menú hamburguesa (`AppComponent`)

**Estado nuevo en la clase:**
```typescript
menuOpen = false;
```

**Métodos nuevos:**
```typescript
toggleMenu(): void { this.menuOpen = !this.menuOpen; }
closeMenu(): void  { this.menuOpen = false; }
```

**Cambios en el template:**
- Se agrega el botón hamburguesa `☰` visible solo en móvil/tablet (clase `hamburger-btn`).
- `navbar-links` pasa a ser un elemento con visibilidad controlada por `menuOpen` en móvil/tablet.
- Cada `<a routerLink>` llama a `(click)="closeMenu()"` para colapsar el menú al navegar.

**Estilos responsivos (en el array `styles` del componente):**

```css
/* Botón hamburguesa: oculto en desktop, visible en móvil/tablet */
.hamburger-btn {
  display: none;
  background: none;
  border: none;
  font-size: 22px;
  cursor: pointer;
  padding: 4px 8px;
  color: var(--color-text);
}

@media (max-width: 768px) {
  .hamburger-btn { display: flex; }

  .navbar-links {
    display: none;           /* oculto por defecto en móvil */
    position: absolute;
    top: 56px;               /* altura del navbar */
    left: 0;
    right: 0;
    background: white;
    border-bottom: 1px solid var(--color-border);
    flex-direction: column;
    padding: 8px 0;
    z-index: 99;
    box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  }

  .navbar-links.open {
    display: flex;           /* visible cuando menuOpen = true */
  }

  .nav-link {
    width: 100%;
    padding: 12px 24px;
    border-radius: 0;
  }

  .navbar { position: relative; }
}
```

El binding en el template usa `[class.open]="menuOpen"` en el elemento `.navbar-links`.

El dark mode ya cubre `.navbar` y `.nav-link` en `styles.css`; se agrega soporte para el menú desplegado:

```css
body.dark .navbar-links {
  background: #1a1d27;
  border-bottom-color: #2e3347;
}
```

### 3. Dashboard — Stats Grid (`DashboardComponent`)

El `.stats-grid` ya usa `grid-template-columns: repeat(auto-fit, minmax(200px, 1fr))` que es parcialmente responsivo, pero no garantiza exactamente 4→2→1 columnas. Se reemplaza por columnas explícitas:

```css
/* Desktop: 4 columnas */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

/* Tablet: 2x2 */
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

/* Móvil: 1 columna */
@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
```

El `.offers-grid` ya tiene `@media (max-width: 768px) { grid-template-columns: 1fr; }` — se mantiene y se extiende para cubrir también el breakpoint de tablet explícitamente.

### 4. Tabla de ofertas responsiva (`OfferListComponent`)

**Estrategia por breakpoint:**

| Breakpoint | Comportamiento |
|---|---|
| Desktop (>768px) | Tabla HTML completa con todas las columnas |
| Tablet (480–768px) | Tabla con scroll horizontal (`overflow-x: auto`) |
| Móvil (<480px) | Tabla oculta, tarjetas apiladas visibles |

**Cambios en el template:**

Se envuelve la tabla en un `<div class="table-wrapper">` para el scroll horizontal. Se agrega un bloque de tarjetas alternativo:

```html
<!-- Tabla (desktop + tablet con scroll) -->
<div class="table-wrapper">
  <table class="table" *ngIf="offers.length > 0">
    <!-- ... contenido existente ... -->
  </table>
</div>

<!-- Tarjetas (solo móvil) -->
<div class="offer-cards" *ngIf="offers.length > 0">
  <div class="offer-card" *ngFor="let offer of offers">
    <div class="offer-card-header">
      <span class="offer-title-cell">{{ offer.title }}</span>
      <span [class]="'badge badge-' + offer.status.toLowerCase()">
        {{ statusLabel(offer.status) }}
      </span>
    </div>
    <div class="offer-card-dates">
      <span>Inicio: {{ formatDate(offer.startsAt) }}</span>
      <span>Fin: {{ formatDate(offer.endsAt) }}</span>
    </div>
    <div class="offer-card-actions">
      <a [routerLink]="['/offers', offer.id, 'edit']" class="btn btn-secondary btn-sm">Editar</a>
      <button *ngIf="authService.isAdmin()" class="btn btn-danger btn-sm"
        (click)="deleteOffer(offer)" [disabled]="deletingId === offer.id">
        {{ deletingId === offer.id ? '...' : 'Eliminar' }}
      </button>
    </div>
  </div>
</div>
```

**Estilos responsivos:**

```css
/* Tabla: scroll horizontal en tablet */
.table-wrapper {
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

/* Tarjetas: ocultas en desktop y tablet, visibles en móvil */
.offer-cards { display: none; }

@media (max-width: 480px) {
  .table-wrapper { display: none; }   /* ocultar tabla */
  .offer-cards   { display: flex; flex-direction: column; gap: 12px; }

  .offer-card {
    background: var(--color-surface);
    border: 1px solid var(--color-border);
    border-radius: var(--border-radius);
    padding: 14px;
  }

  .offer-card-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 8px;
    gap: 8px;
  }

  .offer-card-dates {
    display: flex;
    flex-direction: column;
    gap: 2px;
    font-size: 12px;
    color: var(--color-text-muted);
    margin-bottom: 10px;
  }

  .offer-card-actions {
    display: flex;
    gap: 8px;
  }
}
```

El panel de filtros ya usa `repeat(auto-fit, minmax(180px, 1fr))` — se agrega un media query explícito para móvil:

```css
@media (max-width: 480px) {
  .filters-grid { grid-template-columns: 1fr; }
}
```

### 5. Formulario de oferta responsivo (`OfferFormComponent`)

El `.form-row` ya tiene `@media (max-width: 600px) { grid-template-columns: 1fr; }`. Se ajusta el breakpoint a `480px` para alinearlo con el sistema de breakpoints definido, y se agrega el comportamiento de los botones de acción:

```css
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

@media (max-width: 480px) {
  .form-row { grid-template-columns: 1fr; }

  .form-actions {
    flex-direction: column;
  }

  .form-actions .btn {
    width: 100%;
    justify-content: center;
  }
}
```

### 6. Modal de usuarios responsivo (`UserListComponent`)

El `.modal-card` ya tiene `max-width: 460px` y `width: 100%`. Se agrega el comportamiento para móvil/tablet:

```css
/* Desktop: centrado con max-width 460px (ya existe) */
.modal-card {
  width: 100%;
  max-width: 460px;
}

/* Móvil y tablet: ancho completo con margen */
@media (max-width: 768px) {
  .modal-card {
    max-width: calc(100% - 32px);  /* 16px de margen a cada lado */
  }
}

/* Móvil: altura máxima con scroll interno */
@media (max-width: 480px) {
  .modal-card {
    max-height: 90vh;
    overflow-y: auto;
  }

  /* Formulario de creación: campos apilados */
  .form-row {
    grid-template-columns: 1fr;
  }
}
```

---

## Modelos de Datos

No se introducen nuevos modelos de datos. El único cambio de estado es la propiedad `menuOpen: boolean` en `AppComponent`, que controla la visibilidad del menú hamburguesa.

```typescript
// AppComponent — propiedad nueva
menuOpen = false;
```

---

## Propiedades de Corrección

*Una propiedad es una característica o comportamiento que debe mantenerse verdadero en todas las ejecuciones válidas del sistema — esencialmente, una declaración formal sobre lo que el sistema debe hacer. Las propiedades sirven como puente entre las especificaciones legibles por humanos y las garantías de corrección verificables por máquinas.*

### Propiedad 1: Cada componente define media queries para todos los breakpoints

*Para cualquier* componente afectado por RWD (AppComponent, DashboardComponent, OfferListComponent, OfferFormComponent, UserListComponent), sus estilos deben contener al menos un bloque `@media (max-width: 480px)` y al menos un bloque `@media (max-width: 768px)`.

**Valida: Requerimientos 1.4, 1.5**

### Propiedad 2: El toggle del menú hamburguesa es idempotente en pares

*Para cualquier* estado inicial de `menuOpen`, llamar a `toggleMenu()` dos veces consecutivas debe devolver el estado al valor original.

**Valida: Requerimientos 2.3, 2.4**

### Propiedad 3: Navegar cierra el menú

*Para cualquier* estado de `menuOpen`, llamar a `closeMenu()` debe resultar en `menuOpen === false`.

**Valida: Requerimiento 2.5**

### Propiedad 4: Los controles críticos del navbar nunca se ocultan

*Para cualquier* breakpoint, los elementos `.navbar-brand`, `.navbar-user` y `.btn-theme-toggle` no deben tener `display: none` en sus estilos.

**Valida: Requerimiento 2.7**

### Propiedad 5: Las tarjetas de oferta contienen todos los campos requeridos

*Para cualquier* oferta del sistema, la representación en tarjeta (`.offer-card`) debe contener el título, el badge de estado, la fecha de inicio, la fecha de fin y los botones de acción.

**Valida: Requerimientos 4.2, 4.4**

### Propiedad 6: Los controles de navegación y filtros nunca se ocultan

*Para cualquier* breakpoint, los elementos de paginación (`.pagination`) y el panel de filtros (`.filters-card`) no deben tener `display: none` en sus estilos.

**Valida: Requerimientos 4.6, 4.7**

### Propiedad 7: Los campos del formulario colapsan a una columna en móvil

*Para cualquier* `form-row` del formulario de oferta, en el breakpoint móvil (`max-width: 480px`) el `grid-template-columns` debe ser `1fr` (una sola columna).

**Valida: Requerimientos 5.2, 5.3**

### Propiedad 8: Los mensajes de error del formulario nunca se ocultan

*Para cualquier* breakpoint, los elementos `.alert`, `.invalid-feedback` y `.alert-error` no deben tener `display: none` en sus estilos responsivos.

**Valida: Requerimiento 5.5**

### Propiedad 9: El modal ocupa el ancho disponible en pantallas pequeñas

*Para cualquier* viewport con ancho ≤ 768px, el `.modal-card` debe tener `max-width` igual a `calc(100% - 32px)` o equivalente, garantizando que no exceda el ancho del viewport.

**Valida: Requerimientos 6.2, 6.6**

### Propiedad 10: Los botones críticos del modal nunca se ocultan

*Para cualquier* breakpoint, los elementos `.modal-close` y los botones dentro de `.modal-actions` no deben tener `display: none` en sus estilos.

**Valida: Requerimientos 6.4, 6.5**

---

## Manejo de Errores

- Si el menú hamburguesa queda abierto al redimensionar la ventana a desktop, el CSS lo oculta automáticamente (`display: none` en desktop). El estado `menuOpen` puede quedar en `true` en memoria, pero no tiene efecto visual. No se requiere lógica adicional de `HostListener` para este caso.
- Si una imagen de ícono del navbar no carga, el botón hamburguesa sigue siendo funcional porque usa texto (`☰`), no una imagen.
- El scroll horizontal en tablet (`overflow-x: auto`) no afecta el scroll vertical de la página.
- El scroll interno del modal en móvil (`overflow-y: auto`) está contenido dentro del overlay, no afecta el scroll de la página.

---

## Estrategia de Testing

### Tests unitarios

Se verifican ejemplos concretos y casos de borde:

- `AppComponent`: verificar que `menuOpen` inicia en `false`, que `toggleMenu()` lo cambia, que `closeMenu()` lo pone en `false` independientemente del estado previo.
- `OfferListComponent`: verificar que el template contiene tanto `.table-wrapper` como `.offer-cards` en el DOM.
- `OfferFormComponent`: verificar que `.form-row` existe en el template.
- `UserListComponent`: verificar que `.modal-card` existe y tiene los estilos correctos.

### Tests de propiedades (Property-Based Testing)

Se usa **Karma + Jasmine** (ya configurado en el proyecto Angular) con generadores manuales para los casos de propiedades. Para propiedades más complejas se puede usar **fast-check** (librería PBT para TypeScript/JavaScript).

Configuración mínima: 100 iteraciones por propiedad.

Cada test debe incluir un comentario de trazabilidad:
```
// Feature: responsive-design, Property N: <texto de la propiedad>
```

**Propiedad 2 — Toggle idempotente en pares:**
```typescript
// Feature: responsive-design, Property 2: toggle del menú es idempotente en pares
it('toggleMenu dos veces restaura el estado original', () => {
  fc.assert(fc.property(fc.boolean(), (initial) => {
    component.menuOpen = initial;
    component.toggleMenu();
    component.toggleMenu();
    expect(component.menuOpen).toBe(initial);
  }), { numRuns: 100 });
});
```

**Propiedad 3 — closeMenu siempre resulta en false:**
```typescript
// Feature: responsive-design, Property 3: navegar cierra el menú
it('closeMenu siempre resulta en menuOpen === false', () => {
  fc.assert(fc.property(fc.boolean(), (initial) => {
    component.menuOpen = initial;
    component.closeMenu();
    expect(component.menuOpen).toBe(false);
  }), { numRuns: 100 });
});
```

**Propiedad 5 — Tarjetas contienen todos los campos:**
```typescript
// Feature: responsive-design, Property 5: tarjetas de oferta contienen todos los campos
it('cada offer-card contiene título, badge, fechas y acciones', () => {
  fc.assert(fc.property(fc.array(arbitraryOffer(), { minLength: 1 }), (offers) => {
    component.offers = offers;
    fixture.detectChanges();
    const cards = fixture.nativeElement.querySelectorAll('.offer-card');
    cards.forEach((card: HTMLElement) => {
      expect(card.querySelector('.offer-title-cell')).toBeTruthy();
      expect(card.querySelector('.badge')).toBeTruthy();
      expect(card.querySelector('.offer-card-dates')).toBeTruthy();
      expect(card.querySelector('.offer-card-actions')).toBeTruthy();
    });
  }), { numRuns: 100 });
});
```

**Propiedad 9 — Modal no excede el viewport en pantallas pequeñas:**
```typescript
// Feature: responsive-design, Property 9: modal ocupa el ancho disponible en pantallas pequeñas
it('modal-card tiene max-width correcto en viewport <= 768px', () => {
  const styles = component.styles[0]; // estilos inline del componente
  expect(styles).toContain('max-width: calc(100% - 32px)');
  expect(styles).toContain('max-width: 768px');
});
```

### Tests de integración visual (manuales)

Dado que los media queries no se pueden activar en JSDOM (entorno de test de Angular), las siguientes verificaciones se realizan manualmente en el navegador:

- Redimensionar la ventana a 375px (iPhone SE) y verificar el menú hamburguesa.
- Redimensionar a 768px (iPad) y verificar el scroll horizontal de la tabla.
- Verificar que el dark mode funciona correctamente en todos los breakpoints.
- Verificar que el modal de usuarios no se desborda en pantallas de 375px.
