# Dockerfile — Construcción multi-stage de la imagen Docker del backend.
#
# ¿Qué es un Dockerfile multi-stage?
# Es una técnica que usa múltiples etapas (FROM) en un solo Dockerfile.
# La idea: usar una imagen grande con todas las herramientas de compilación
# para construir el artefacto, y luego copiar SOLO el resultado a una imagen
# pequeña y limpia para producción.
#
# Ventajas del multi-stage:
# - La imagen final es mucho más pequeña (solo JRE, no JDK + Maven)
# - No incluye código fuente ni herramientas de build en producción
# - Mejor seguridad: menos superficie de ataque
# - Más rápido de descargar y desplegar
#
# Ejemplo de tamaños:
# - maven:3.9-eclipse-temurin-21 ≈ 500MB (imagen de build)
# - eclipse-temurin:21-jre-alpine ≈ 80MB (imagen de runtime)
# - Imagen final con el JAR ≈ 120MB (vs 500MB+ si usáramos una sola etapa)

# ============================================================
# STAGE 1: BUILD
# Compilar el código Java y generar el JAR ejecutable.
# ============================================================
FROM maven:3.9-eclipse-temurin-21 AS build

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Paso 1: Copiar SOLO el pom.xml primero.
# Decisión de optimización: Docker cachea cada capa. Si copiamos el pom.xml
# antes que el código fuente, Docker puede reutilizar la capa de descarga
# de dependencias mientras el código no cambie.
# Sin esto, cada cambio de código forzaría re-descargar todas las dependencias.
COPY pom.xml .

# Paso 2: Descargar dependencias (esta capa se cachea si pom.xml no cambia)
RUN mvn dependency:go-offline -B

# Paso 3: Copiar el código fuente
COPY src ./src

# Paso 4: Compilar y empaquetar como JAR, saltando los tests.
# Los tests se ejecutan en CI/CD, no en el build de Docker.
# -DskipTests: no ejecutar tests
# -B: modo batch (sin colores, para logs más limpios)
RUN mvn package -DskipTests -B

# ============================================================
# STAGE 2: RUNTIME
# Imagen final liviana con solo lo necesario para ejecutar el JAR.
# ============================================================
FROM eclipse-temurin:21-jre-alpine AS runtime

# Metadatos de la imagen (buena práctica)
LABEL maintainer="Easy Offers Team"
LABEL description="Backend API para la plataforma de gestión de ofertas Easy"

# Crear un usuario no-root para ejecutar la aplicación.
# Decisión de seguridad: nunca ejecutar aplicaciones como root en producción.
# Si hay una vulnerabilidad, el atacante obtiene permisos limitados.
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copiar el JAR generado en el stage de build.
# El patrón *.jar copia el JAR sin importar la versión exacta del nombre.
COPY --from=build /app/target/*.jar app.jar

# Cambiar al usuario no-root
USER appuser

# Documentar el puerto que expone la aplicación.
# EXPOSE es solo documentación; el mapeo real se hace en docker-compose.yml.
EXPOSE 8080

# Punto de entrada: ejecutar el JAR con la JVM.
# -Djava.security.egd: mejora el rendimiento del generador de números aleatorios
#   en contenedores Linux (importante para la generación de tokens JWT).
# -Dspring.profiles.active: se puede sobreescribir con variable de entorno.
ENTRYPOINT ["java", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", \
            "app.jar"]
