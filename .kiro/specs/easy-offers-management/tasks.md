# Plan de Implementación: Plataforma de Gestión de Ofertas Comerciales Easy

## Overview

Implementación incremental del backend (Java 21 + Spring Boot) y frontend (Angular) siguiendo Clean Architecture. Cada fase construye sobre la anterior y termina con código integrado y funcional. El orden prioriza la base técnica primero (estructura, auth, dominio) antes de las funcionalidades de negocio.

## Tasks

- [x] 1. Estructura base del proyecto y configuración Docker
  - Crear proyecto Spring Boot 3.x con Java 21 usando Spring Initializr (dependencias: Web, JPA, Security, PostgreSQL Driver, Validation, Scheduled)
  - Crear `docker-compose.yml` con servicios: `postgres`, `backend`, `frontend`
  - Crear `Dockerfile` para el backend (multi-stage: build con Maven, runtime con JRE 21)
  - Configurar `application.yml` con datasource, JPA (ddl-auto=validate), JWT secret y expiración como variables de entorno
  - Crear estructura de paquetes base: `auth`, `users`, `offers`, `audit` con subcarpetas `api`, `application`, `domain`, `infrastructure`
  - _Requirements: 11.1, 11.2_

- [x] 2. Modelo de datos y migraciones SQL
  - [x] 2.1 Crear scripts SQL de esquema (`schema.sql`) con las tablas `users`, `offer_types`, `sectors`, `offers`, `offer_audit_log` según el diseño
    - Incluir constraints, FKs y `ON DELETE SET NULL` en `offer_audit_log.offer_id`
    - _Requirements: 11.5, 9.4_
  - [x] 2.2 Crear script `data.sql` con los datos de referencia de `offer_types` (8 tipos) y `sectors` (21 sectores)
    - _Requirements: 3.1, 3.6_
  - [x] 2.3 Crear los índices de performance en `offers`
    - `idx_offers_offer_type_id`, `idx_offers_sector_id`, `idx_offers_status`, `idx_offers_starts_at`
    - _Requirements: 11.5, 6.4_

- [x] 3. Dominio: entidades, enums y excepciones
  - [x] 3.1 Implementar entidades de dominio puras (records Java): `User`, `Offer`, `AuditLog`, `OfferType`, `Sector`
    - Sin anotaciones JPA ni dependencias de Spring
    - _Requirements: 11.2_
  - [x] 3.2 Implementar enums: `UserRole` (ADMIN, EMPLOYEE), `OfferStatus` (PROXIMA, ACTIVA, VENCIDA), `ChangeType` (CREATE, UPDATE, DELETE, AUTO_DELETE)
    - _Requirements: 2.5, 7.1, 9.1_
  - [x] 3.3 Implementar excepciones de dominio puras: `InvalidCredentialsException`, `TokenExpiredException`, `InsufficientRoleException`, `ResourceNotFoundException`, `DuplicateUsernameException`, `InvalidDateRangeException`, `InvalidReferenceException`, `InvalidRoleException`
    - _Requirements: 11.2_
  - [x] 3.4 Implementar `StatusEngine` como clase de dominio pura con método estático `calculate(LocalDateTime startsAt, LocalDateTime endsAt, Instant now)`
    - _Requirements: 7.1, 7.2, 3.3, 4.2_
  - [x]* 3.5 Escribir unit tests para `StatusEngine` (casos borde: now == startsAt, now == endsAt)
    - _Requirements: 7.1_
  - [x]* 3.6 Escribir property test para `StatusEngine`
    - **Property 12: StatusEngine calcula estado correcto para cualquier combinación de fechas**
    - **Validates: Requirements 3.3, 4.2, 7.1, 7.2**

- [x] 4. Checkpoint — Dominio y base de datos
  - Verificar que el esquema SQL levanta correctamente con Docker Compose
  - Verificar que los tests de `StatusEngine` pasan
  - Consultar al usuario si hay dudas antes de continuar con la capa de infraestructura

- [x] 5. Infraestructura: entidades JPA y repositorios
  - [x] 5.1 Implementar entidades JPA: `UserJpaEntity`, `OfferJpaEntity`, `AuditLogJpaEntity`, `OfferTypeJpaEntity`, `SectorJpaEntity` con todas las anotaciones necesarias
    - _Requirements: 11.2_
  - [x] 5.2 Implementar mappers: `UserMapper`, `OfferMapper`, `AuditMapper` (conversión entre entidad de dominio y entidad JPA)
    - _Requirements: 11.2_
  - [x] 5.3 Implementar repositorios JPA: `UserJpaRepository`, `OfferJpaRepository` (con `@Query` para filtros dinámicos y paginación), `AuditLogJpaRepository`, `OfferTypeJpaRepository`, `SectorJpaRepository`
    - `OfferJpaRepository` debe soportar filtros opcionales por `sector_id`, `offer_type_id`, `status`, `starts_at`, `ends_at` y ordenamiento por `starts_at DESC`
    - _Requirements: 6.1, 6.2, 6.5, 11.5_

- [x] 6. Módulo Auth: JWT y Spring Security
  - [x] 6.1 Implementar interfaz de puerto `TokenProvider` con métodos `generateToken` y `validateToken`, y el record `TokenClaims`
    - _Requirements: 11.2_
  - [x] 6.2 Implementar `JwtProvider` en Infrastructure que implemente `TokenProvider` usando la librería `jjwt`, firmando con HS256 y clave configurable por variable de entorno
    - _Requirements: 1.1, 11.3_
  - [x] 6.3 Implementar `JwtAuthFilter` (`OncePerRequestFilter`) que extrae y valida el JWT de cada request y carga el `SecurityContext`
    - _Requirements: 1.3_
  - [x] 6.4 Implementar `SecurityConfig` con `@EnableWebSecurity`: rutas públicas (`/api/auth/**`), rutas protegidas por rol, y configuración stateless (sin sesiones)
    - _Requirements: 1.3, 1.4, 2.4, 5.3, 9.3_
  - [x] 6.5 Implementar `AuthService` con método `authenticate(username, password)`: busca usuario activo, verifica BCrypt (factor >= 10), genera token de 8 horas
    - _Requirements: 1.1, 1.2, 1.5, 2.3_
  - [x] 6.6 Implementar `AuthController` con `POST /api/auth/login` y el `GlobalExceptionHandler` con `@RestControllerAdvice`
    - _Requirements: 1.1, 1.2_
  - [ ]* 6.7 Escribir unit tests para `AuthService` (login exitoso, credenciales inválidas, usuario inactivo)
    - _Requirements: 1.1, 1.2, 2.3_
  - [ ]* 6.8 Escribir property tests para `AuthService`
    - **Property 1: Autenticación exitosa genera token válido**
    - **Validates: Requirements 1.1**
    - **Property 2: Credenciales inválidas siempre son rechazadas**
    - **Validates: Requirements 1.2**
    - **Property 8: Usuario desactivado no puede autenticarse**
    - **Validates: Requirements 2.3**
  - [ ]* 6.9 Escribir property tests para autorización
    - **Property 3: Requests sin token o con token expirado son rechazados**
    - **Validates: Requirements 1.3**
    - **Property 4: Autorización por rol es estricta**
    - **Validates: Requirements 1.4, 2.4, 5.3, 9.3**
    - **Property 5: Contraseñas almacenadas con BCrypt factor >= 10**
    - **Validates: Requirements 1.5**

- [x] 7. Checkpoint — Autenticación funcional
  - Verificar que `POST /api/auth/login` retorna JWT con usuario de prueba
  - Verificar que endpoints protegidos rechazan requests sin token (401) y con rol incorrecto (403)
  - Consultar al usuario si hay dudas antes de continuar

- [x] 8. Módulo Users: gestión de cuentas
  - [x] 8.1 Implementar `UserService` con métodos `createUser` y `deactivateUser`
    - Validar username único (lanzar `DuplicateUsernameException`), hashear password con BCrypt factor 10, validar rol
    - _Requirements: 2.1, 2.2, 2.3, 2.5_
  - [x] 8.2 Implementar `UserController` con `POST /api/users` y `PATCH /api/users/{id}/deactivate`, ambos restringidos a rol ADMIN
    - _Requirements: 2.1, 2.4_
  - [ ]* 8.3 Escribir unit tests para `UserService` (creación exitosa, username duplicado, desactivación)
    - _Requirements: 2.1, 2.2, 2.3_
  - [ ]* 8.4 Escribir property tests para `UserService`
    - **Property 6: Creación de usuario persiste correctamente**
    - **Validates: Requirements 2.1**
    - **Property 7: Username duplicado siempre retorna conflicto**
    - **Validates: Requirements 2.2**
    - **Property 9: Roles inválidos son rechazados en creación de usuario**
    - **Validates: Requirements 2.5**

- [x] 9. Módulo Offers: CRUD principal
  - [x] 9.1 Implementar `CreateOfferCommand` y `UpdateOfferCommand` como records inmutables
    - _Requirements: 3.1, 4.1_
  - [x] 9.2 Implementar `OfferService.createOffer(command, userId)`: validar fechas, validar referencias (offer_type_id, sector_id), calcular estado con `StatusEngine`, persistir, registrar auditoría CREATE
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_
  - [ ]* 9.3 Escribir property tests para creación de oferta
    - **Property 10: Creación de oferta con datos válidos persiste correctamente**
    - **Validates: Requirements 3.1**
    - **Property 11: Validación de fechas rechaza rangos inválidos**
    - **Validates: Requirements 3.2, 4.6**
    - **Property 13: Trazabilidad de usuario en operaciones de escritura**
    - **Validates: Requirements 3.4, 4.3**
    - **Property 15: Referencias inválidas son rechazadas**
    - **Validates: Requirements 3.6**
  - [x] 9.4 Implementar `OfferService.updateOffer(id, command, userId)`: validar existencia (404), validar fechas, recalcular estado, actualizar `updated_by` y `updated_at`, registrar auditoría UPDATE por cada campo modificado
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6_
  - [ ]* 9.5 Escribir property tests para edición de oferta
    - **Property 16: Edición de oferta actualiza datos correctamente**
    - **Validates: Requirements 4.1**
    - **Property 17: ID inexistente retorna 404**
    - **Validates: Requirements 4.5, 5.4, 6.3**
  - [x] 9.6 Implementar `OfferService.deleteOffer(id, userId)`: validar existencia (404), eliminar, registrar auditoría DELETE
    - _Requirements: 5.1, 5.2, 5.4_
  - [x] 9.7 Implementar `OfferService.getOffers(filterParams, pageable)` con filtros dinámicos y paginación (máx 50 por página, orden por `starts_at DESC`)
    - _Requirements: 6.1, 6.2, 6.5_
  - [ ]* 9.8 Escribir property tests para consulta y filtrado
    - **Property 18: Paginación nunca excede el límite**
    - **Validates: Requirements 6.1**
    - **Property 19: Filtros retornan solo ofertas que cumplen todos los criterios**
    - **Validates: Requirements 6.2**
    - **Property 20: Listado sin filtros está ordenado por starts_at descendente**
    - **Validates: Requirements 6.5**
  - [x] 9.9 Implementar `OfferController` con todos los endpoints: `GET /api/offers`, `POST /api/offers`, `GET /api/offers/{id}`, `PUT /api/offers/{id}`, `DELETE /api/offers/{id}` (DELETE solo ADMIN)
    - Incluir `OfferFilterParams` como record con parámetros de query opcionales
    - _Requirements: 3.1, 4.1, 5.1, 5.3, 6.1, 6.2, 6.3_

- [x] 10. Checkpoint — CRUD de ofertas funcional
  - Verificar que todos los endpoints de ofertas responden correctamente con Postman o curl
  - Verificar que los property tests de filtros y paginación pasan
  - Consultar al usuario si hay dudas antes de continuar con auditoría y jobs

- [x] 11. Módulo Audit: registro de eventos
  - [x] 11.1 Implementar `AuditLogger` como interfaz de puerto y `AuditService` como implementación
    - Métodos: `logCreate(offerId, userId)`, `logUpdate(offerId, userId, fieldChanged, oldValue, newValue)`, `logDelete(offerId, userId)`, `logAutoDelete(offerId)`
    - _Requirements: 9.1_
  - [x] 11.2 Implementar `AuditController` con `GET /api/offers/{id}/audit` restringido a rol ADMIN, retornando registros ordenados por `created_at DESC`
    - _Requirements: 9.2, 9.3_
  - [ ]* 11.3 Escribir unit tests para `AuditService` (verificar campos correctos por tipo de evento)
    - _Requirements: 9.1_
  - [ ]* 11.4 Escribir property tests para auditoría
    - **Property 14: Auditoría registra evento correcto con campos completos**
    - **Validates: Requirements 3.5, 4.4, 5.2, 8.2, 9.1**
    - **Property 22: Registros de auditoría persisten después de eliminar la oferta**
    - **Validates: Requirements 9.4**
    - **Property 23: Historial de auditoría está ordenado por created_at descendente**
    - **Validates: Requirements 9.2**

- [x] 12. Jobs programados: StatusUpdateJob y CleanupJob
  - [x] 12.1 Implementar `StatusUpdateJob` con `@Scheduled(fixedRate = 3600000)`: recalcular estado de todas las ofertas usando `StatusEngine` y persistir cambios
    - _Requirements: 7.3_
  - [x] 12.2 Implementar `CleanupJob` con `@Scheduled(cron = "0 0 2 * * *")`: eliminar ofertas con `ends_at < now - 21 días` y registrar evento `AUTO_DELETE` en auditoría por cada oferta eliminada
    - _Requirements: 8.1, 8.2, 8.3_
  - [ ]* 12.3 Escribir unit tests para `CleanupJob` con fechas fijas (verificar qué se elimina y qué se preserva)
    - _Requirements: 8.1_
  - [ ]* 12.4 Escribir property test para `CleanupJob`
    - **Property 21: Cleanup_Job elimina solo ofertas dentro del período de retención**
    - **Validates: Requirements 8.1**

- [x] 13. Dashboard endpoint
  - [x] 13.1 Implementar lógica de dashboard en `OfferService.getDashboard()`: contar ofertas por estado y obtener las 10 más recientes de `ACTIVA` y `PROXIMA`
    - _Requirements: 10.1, 10.2_
  - [x] 13.2 Implementar `GET /api/dashboard` en `OfferController` retornando `DashboardResponse`
    - _Requirements: 10.1, 10.2, 10.3_
  - [ ]* 13.3 Escribir property tests para dashboard
    - **Property 24: Dashboard muestra conteos correctos por estado**
    - **Validates: Requirements 10.1**
    - **Property 25: Dashboard limita resultados por estado**
    - **Validates: Requirements 10.2**

- [x] 14. Checkpoint — Backend completo
  - Ejecutar todos los tests con `./mvnw test`
  - Verificar que el backend levanta correctamente con `docker-compose up`
  - Verificar logs de nivel ERROR ante fallos simulados
  - Consultar al usuario antes de comenzar el frontend

- [x] 15. Frontend Angular: estructura base y autenticación
  - [x] 15.1 Crear proyecto Angular con `ng new easy-offers-frontend --routing --style=css`
    - Crear módulos: `auth`, `offers`, `users`, `shared`
    - _Requirements: 11.1_
  - [x] 15.2 Implementar `AuthService` en Angular con `login()`, almacenamiento del JWT en `localStorage` y método `getRole()`
    - _Requirements: 1.1_
  - [x] 15.3 Implementar `AuthInterceptor` HTTP que agrega el header `Authorization: Bearer {token}` a todas las requests
    - _Requirements: 1.3_
  - [x] 15.4 Implementar `AuthGuard` que redirige a login si no hay token válido, y `RoleGuard` para rutas exclusivas de ADMIN
    - _Requirements: 1.4_
  - [x] 15.5 Implementar componente `LoginComponent` con formulario de credenciales y manejo de error 401
    - _Requirements: 1.1, 1.2_

- [x] 16. Frontend Angular: Dashboard y listado de ofertas
  - [x] 16.1 Implementar `OfferService` en Angular con métodos para todos los endpoints de ofertas (`getOffers`, `getOffer`, `createOffer`, `updateOffer`, `deleteOffer`, `getDashboard`)
    - _Requirements: 6.1, 6.2, 6.3, 10.1_
  - [x] 16.2 Implementar `DashboardComponent` que muestra conteos por estado y listas de ofertas activas y próximas
    - _Requirements: 10.1, 10.2, 10.3_
  - [x] 16.3 Implementar `OfferListComponent` con tabla paginada, filtros por sector, tipo, estado y rango de fechas
    - _Requirements: 6.1, 6.2, 6.5_
  - [x] 16.4 Implementar `OfferFormComponent` reutilizable para creación y edición de ofertas con validación de fechas en cliente
    - _Requirements: 3.1, 3.2, 4.1_

- [x] 17. Frontend Angular: gestión de usuarios y auditoría (solo ADMIN)
  - [x] 17.1 Implementar `UserService` en Angular y `UserListComponent` con formulario de creación y botón de desactivación
    - _Requirements: 2.1, 2.3_
  - [x] 17.2 Implementar `AuditLogComponent` que muestra el historial de cambios de una oferta (accesible desde el detalle de oferta)
    - _Requirements: 9.2_

- [x] 18. Checkpoint final — Sistema completo integrado
  - Ejecutar `docker-compose up --build` y verificar que todos los servicios levantan
  - Verificar flujo completo: login → dashboard → crear oferta → editar → ver auditoría → eliminar
  - Ejecutar todos los tests del backend con `./mvnw test`
  - Consultar al usuario si hay ajustes finales antes de cerrar

## Notes

- Las tareas marcadas con `*` son opcionales y pueden omitirse para un MVP más rápido
- Cada tarea referencia requerimientos específicos para trazabilidad
- Los property tests usan **jqwik** con mínimo 100 iteraciones (`@Property(tries = 100)`)
- Cada property test debe incluir el tag: `// Feature: easy-offers-management, Property {N}: {descripción}`
- El dominio (entidades, StatusEngine, excepciones) nunca debe importar clases de Spring ni JPA
- Los jobs usan `@Scheduled` de Spring; habilitar con `@EnableScheduling` en la clase principal
