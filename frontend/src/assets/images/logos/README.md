# logos/

Logos e imágenes de marca de la aplicación Easy Offers.

## Archivos esperados

```
logos/
├── logo-easy.png           — Logo principal de Easy (con fondo)
├── logo-easy-white.png     — Logo blanco para fondos oscuros (navbar, login)
├── logo-easy.svg           — Versión SVG del logo (preferida)
└── favicon.ico             — Ícono del navegador (también en src/)
```

## Recomendaciones

- Formato: SVG para logos vectoriales, PNG con transparencia para logos rasterizados
- El logo blanco se usa en la navbar (fondo rojo) y en el login (fondo oscuro)
- Tamaño máximo recomendado: 50KB por logo

## Uso en Angular

```html
<!-- En el navbar -->
<img src="assets/images/logos/logo-easy-white.png" alt="Easy" height="32">

<!-- En el login -->
<img src="assets/images/logos/logo-easy.svg" alt="Easy Offers" class="login-logo">
```

## Uso en CSS

```css
.navbar-brand {
  background-image: url('/assets/images/logos/logo-easy-white.png');
  background-size: contain;
  background-repeat: no-repeat;
  width: 120px;
  height: 32px;
}
```
