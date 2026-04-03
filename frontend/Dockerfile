# Dockerfile del frontend Angular — Multi-stage build.
#
# Stage 1 (build): compila el proyecto Angular con Node.js.
# Stage 2 (runtime): sirve los archivos estáticos con Nginx.
#
# ¿Por qué Nginx para servir el frontend?
# Angular compila a archivos estáticos (HTML, CSS, JS).
# Nginx es un servidor web muy eficiente para servir archivos estáticos.
# También actúa como proxy inverso: las requests a /api se redirigen al backend.
# Esto evita problemas de CORS (Cross-Origin Resource Sharing).

# ============================================================
# STAGE 1: BUILD — Compilar Angular
# ============================================================
FROM node:20-alpine AS build

WORKDIR /app

# Copiar package.json primero para aprovechar el cache de Docker.
# Si package.json no cambia, Docker reutiliza la capa de npm install.
COPY package.json ./

# Instalar dependencias
RUN npm install

# Copiar el resto del código fuente
COPY . .

# Compilar para producción
# --configuration production: usa environment.prod.ts (apiUrl = '/api')
RUN npm run build:prod

# ============================================================
# STAGE 2: RUNTIME — Servir con Nginx
# ============================================================
FROM nginx:alpine AS runtime

# Copiar los archivos compilados al directorio de Nginx
COPY --from=build /app/dist/easy-offers-frontend/browser /usr/share/nginx/html

# Copiar la configuración personalizada de Nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
