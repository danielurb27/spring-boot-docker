# icons/

Iconos SVG o PNG para usar en los distintos componentes de la aplicación.

## Estructura sugerida

```
icons/
├── dashboard/
│   ├── ic-activas.svg      — Icono para ofertas activas
│   ├── ic-proximas.svg     — Icono para ofertas próximas
│   └── ic-vencidas.svg     — Icono para ofertas vencidas
├── nav/
│   ├── ic-offers.svg       — Icono del menú Ofertas
│   ├── ic-users.svg        — Icono del menú Usuarios
│   ├── ic-audit.svg        — Icono del menú Auditoría
│   └── ic-logout.svg       — Icono de cerrar sesión
└── actions/
    ├── ic-edit.svg         — Icono de editar
    ├── ic-delete.svg       — Icono de eliminar
    └── ic-add.svg          — Icono de agregar
```

## Recomendaciones

- Formato preferido: **SVG** (escalable, liviano, sin pérdida de calidad)
- Tamaño estándar: 24x24px para iconos de interfaz
- Colores: usar `currentColor` en SVG para que hereden el color del texto
- Fuentes alternativas: puedes usar Font Awesome, Material Icons o Heroicons

## Uso en Angular (SVG inline)

```html
<!-- Opción 1: img tag -->
<img src="assets/images/icons/nav/ic-offers.svg" alt="Ofertas" width="20" height="20">

<!-- Opción 2: CSS background -->
.icon-offers {
  background-image: url('/assets/images/icons/nav/ic-offers.svg');
  width: 20px;
  height: 20px;
}
```

## Fuentes gratuitas de iconos SVG

- https://heroicons.com (recomendado para apps modernas)
- https://feathericons.com
- https://fonts.google.com/icons (Material Icons)
