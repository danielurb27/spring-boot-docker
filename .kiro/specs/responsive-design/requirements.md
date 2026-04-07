# Documento de Requerimientos

## Introducción

Esta feature agrega soporte de Diseño Web Responsivo (Responsive Web Design) a la plataforma Easy Offers Management. El objetivo es garantizar que todos los componentes de la interfaz sean utilizables y visualmente correctos en dispositivos móviles, tablets y escritorio, sin pérdida de funcionalidad. Los cambios se aplican exclusivamente al frontend Angular, modificando estilos CSS y lógica de presentación en los componentes existentes.

## Glosario

- **Navbar**: Barra de navegación principal definida en `AppComponent`. Contiene el logo, los enlaces de navegación y los controles de usuario.
- **Menú_Hamburguesa**: Botón con ícono ☰ que reemplaza los enlaces de navegación en pantallas móviles. Al pulsarlo, despliega el menú verticalmente.
- **Dashboard**: Componente `DashboardComponent`. Muestra tarjetas de estadísticas y listas de ofertas recientes.
- **Stats_Grid**: Contenedor CSS grid con las 4 tarjetas de estadísticas del Dashboard (activas, próximas, vencidas, total).
- **Tabla_Ofertas**: Elemento `<table>` dentro de `OfferListComponent` que lista las ofertas con columnas de título, estado, progreso, fechas y acciones.
- **Tarjeta_Oferta**: Representación alternativa de una fila de la Tabla_Ofertas en formato de tarjeta apilada, usada en pantallas móviles.
- **Formulario_Oferta**: Componente `OfferFormComponent`. Contiene campos para crear o editar una oferta, organizados en filas de dos columnas.
- **Modal_Usuarios**: Ventana modal de edición de usuarios dentro de `UserListComponent`.
- **Breakpoint_Movil**: Ancho de pantalla menor a 480px.
- **Breakpoint_Tablet**: Ancho de pantalla entre 480px y 768px (inclusive).
- **Breakpoint_Desktop**: Ancho de pantalla mayor a 768px.
- **Viewport**: Área visible del navegador en el dispositivo del usuario.
- **RWD_System**: El sistema de diseño responsivo compuesto por las variables CSS de breakpoints, utilidades globales y los estilos responsivos de cada componente.

---

## Requerimientos

### Requerimiento 1: Variables y utilidades globales de breakpoints

**User Story:** Como desarrollador, quiero que los breakpoints estén definidos como variables CSS centralizadas en `styles.css`, para poder referenciarlos de forma consistente en todos los componentes.

#### Criterios de Aceptación

1. THE RWD_System SHALL definir la variable CSS `--bp-mobile` con el valor `480px` en el selector `:root` de `styles.css`.
2. THE RWD_System SHALL definir la variable CSS `--bp-tablet` con el valor `768px` en el selector `:root` de `styles.css`.
3. THE RWD_System SHALL incluir la meta-etiqueta `<meta name="viewport" content="width=device-width, initial-scale=1">` en `index.html` para habilitar el escalado responsivo en dispositivos móviles.
4. WHEN el Viewport tiene un ancho menor a 480px, THE RWD_System SHALL aplicar los estilos del Breakpoint_Movil en todos los componentes afectados.
5. WHEN el Viewport tiene un ancho entre 480px y 768px, THE RWD_System SHALL aplicar los estilos del Breakpoint_Tablet en todos los componentes afectados.
6. WHEN el Viewport tiene un ancho mayor a 768px, THE RWD_System SHALL aplicar los estilos del Breakpoint_Desktop en todos los componentes afectados.

---

### Requerimiento 2: Navbar responsiva con menú hamburguesa

**User Story:** Como usuario móvil, quiero que la barra de navegación colapse en un menú hamburguesa, para poder navegar sin que los enlaces ocupen todo el ancho de la pantalla.

#### Criterios de Aceptación

1. WHILE el Viewport está en Breakpoint_Desktop, THE Navbar SHALL mostrar todos los enlaces de navegación en una fila horizontal con el layout actual.
2. WHILE el Viewport está en Breakpoint_Movil o Breakpoint_Tablet, THE Navbar SHALL ocultar los enlaces de navegación horizontales y mostrar el botón Menú_Hamburguesa (ícono ☰).
3. WHEN el usuario pulsa el botón Menú_Hamburguesa, THE Navbar SHALL desplegar los enlaces de navegación en una lista vertical debajo de la barra principal.
4. WHEN el menú está desplegado y el usuario pulsa nuevamente el botón Menú_Hamburguesa, THE Navbar SHALL colapsar la lista vertical de enlaces.
5. WHEN el usuario selecciona un enlace del menú desplegado, THE Navbar SHALL colapsar el menú automáticamente.
6. WHILE el menú está desplegado en Breakpoint_Movil o Breakpoint_Tablet, THE Navbar SHALL mostrar cada enlace de navegación ocupando el 100% del ancho disponible.
7. THE Navbar SHALL mantener visibles el logo y los controles de usuario (rol, toggle de tema, cerrar sesión) en todos los breakpoints.
8. IF el Viewport cambia de Breakpoint_Movil o Breakpoint_Tablet a Breakpoint_Desktop, THEN THE Navbar SHALL ocultar el menú desplegado y restaurar el layout horizontal.

---

### Requerimiento 3: Dashboard responsivo — Stats Grid

**User Story:** Como usuario móvil, quiero que las tarjetas de estadísticas del dashboard se reorganicen en una cuadrícula adaptada a mi pantalla, para poder leerlas sin hacer scroll horizontal.

#### Criterios de Aceptación

1. WHILE el Viewport está en Breakpoint_Desktop, THE Stats_Grid SHALL mostrar las 4 tarjetas de estadísticas en una fila de 4 columnas iguales.
2. WHILE el Viewport está en Breakpoint_Tablet, THE Stats_Grid SHALL mostrar las 4 tarjetas de estadísticas en una cuadrícula de 2 columnas por 2 filas.
3. WHILE el Viewport está en Breakpoint_Movil, THE Stats_Grid SHALL mostrar las 4 tarjetas de estadísticas apiladas en una sola columna.
4. THE Stats_Grid SHALL mantener el contenido de cada tarjeta (ícono, número y etiqueta) completamente visible en todos los breakpoints sin truncamiento.
5. WHILE el Viewport está en Breakpoint_Movil o Breakpoint_Tablet, THE Dashboard SHALL mostrar las listas de ofertas recientes en una sola columna.

---

### Requerimiento 4: Tabla de ofertas responsiva

**User Story:** Como usuario móvil, quiero que la lista de ofertas sea legible en pantallas pequeñas, para poder consultar y gestionar ofertas sin scroll horizontal forzado.

#### Criterios de Aceptación

1. WHILE el Viewport está en Breakpoint_Desktop, THE Tabla_Ofertas SHALL mostrarse como tabla HTML con todas las columnas visibles (título, estado, progreso, inicio, fin, acciones).
2. WHILE el Viewport está en Breakpoint_Movil, THE Tabla_Ofertas SHALL reemplazar cada fila por una Tarjeta_Oferta apilada verticalmente.
3. WHILE el Viewport está en Breakpoint_Tablet, THE Tabla_Ofertas SHALL aplicar scroll horizontal controlado con `overflow-x: auto` en el contenedor de la tabla, manteniendo el layout tabular.
4. WHEN el Viewport está en Breakpoint_Movil, THE Tarjeta_Oferta SHALL mostrar el título, el badge de estado, las fechas de inicio y fin, y los botones de acción.
5. WHILE el Viewport está en Breakpoint_Movil, THE Tarjeta_Oferta SHALL mostrar los botones de acción (Editar, Eliminar) en una fila horizontal dentro de la tarjeta.
6. THE Tabla_Ofertas SHALL mantener la funcionalidad de paginación visible y operable en todos los breakpoints.
7. THE Tabla_Ofertas SHALL mantener el panel de filtros visible y operable en todos los breakpoints, apilando los controles en una sola columna en Breakpoint_Movil.

---

### Requerimiento 5: Formulario de oferta responsivo

**User Story:** Como usuario móvil, quiero que el formulario de creación y edición de ofertas se adapte a una sola columna, para poder completar los campos sin dificultad en pantallas pequeñas.

#### Criterios de Aceptación

1. WHILE el Viewport está en Breakpoint_Desktop o Breakpoint_Tablet, THE Formulario_Oferta SHALL mostrar los campos de tipo/sector y los campos de fechas en filas de 2 columnas (`form-row`).
2. WHILE el Viewport está en Breakpoint_Movil, THE Formulario_Oferta SHALL apilar todos los campos de cada `form-row` en una sola columna.
3. THE Formulario_Oferta SHALL mantener el ancho completo del Viewport para cada campo de entrada en Breakpoint_Movil.
4. WHILE el Viewport está en Breakpoint_Movil, THE Formulario_Oferta SHALL mostrar los botones de acción (Cancelar, Guardar) en una fila que ocupe el 100% del ancho disponible.
5. THE Formulario_Oferta SHALL mantener la validación de campos y los mensajes de error visibles en todos los breakpoints.

---

### Requerimiento 6: Modal de edición de usuarios responsivo

**User Story:** Como administrador en un dispositivo móvil, quiero que el modal de edición de usuarios ocupe el ancho completo de la pantalla, para poder editar los datos sin que el modal quede cortado.

#### Criterios de Aceptación

1. WHILE el Viewport está en Breakpoint_Desktop, THE Modal_Usuarios SHALL mostrarse centrado con un ancho máximo de 460px.
2. WHILE el Viewport está en Breakpoint_Movil o Breakpoint_Tablet, THE Modal_Usuarios SHALL ocupar el 100% del ancho del Viewport descontando un margen de 16px a cada lado.
3. WHILE el Viewport está en Breakpoint_Movil, THE Modal_Usuarios SHALL ajustar su altura máxima al 90% del alto del Viewport y habilitar scroll vertical interno si el contenido lo requiere.
4. THE Modal_Usuarios SHALL mantener el botón de cierre (✕) visible y operable en todos los breakpoints.
5. THE Modal_Usuarios SHALL mantener los botones de acción (Cancelar, Guardar cambios) visibles y operables en todos los breakpoints.
6. WHILE el Viewport está en Breakpoint_Movil, THE Modal_Usuarios SHALL mostrar el formulario de creación de usuarios con los campos apilados en una sola columna.
