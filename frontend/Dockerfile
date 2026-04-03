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
COPY frontend/package.json ./
RUN npm install
COPY frontend ./
RUN npm run build:prod

# ============================================================
# STAGE 2: RUNTIME — Servir con Nginx
# ============================================================
FROM nginx:alpine AS runtime

# Copiar los archivos compilados de Angular
COPY --from=build /app/dist/easy-offers-frontend/browser /usr/share/nginx/html

# Copiar nginx.conf como template.
# La imagen nginx:alpine procesa automáticamente los archivos en
# /etc/nginx/templates/ usando envsubst, reemplazando ${PORT} con
# el valor real de la variable de entorno que Render inyecta.
# El resultado se copia a /etc/nginx/conf.d/default.conf al arrancar.
COPY frontend/nginx.conf /etc/nginx/templates/default.conf.template

# Render asigna el puerto via variable de entorno PORT.
# Exponemos 80 como documentación, pero Nginx escuchará en $PORT.
EXPOSE 80
