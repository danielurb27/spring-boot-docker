# Documento de Requerimientos

## Introducción

Plataforma web centralizada para gestionar ofertas comerciales de Easy, reemplazando el uso de Excel y procesos manuales. El sistema permite a administradores y empleados registrar, modificar y visualizar ofertas con control de vigencia automático, auditoría de cambios y gestión de usuarios por roles.

---

## Glosario

- **System**: El sistema de gestión de ofertas comerciales Easy.
- **Offer**: Promoción o descuento comercial con fechas de vigencia, tipo, sector y estado.
- **Offer_Manager**: Módulo responsable de la creación, edición y eliminación de ofertas.
- **Status_Engine**: Componente que calcula y actualiza automáticamente el estado de las ofertas.
- **Cleanup_Job**: Proceso programado que elimina ofertas vencidas pasado el período de retención.
- **Audit_Logger**: Componente que registra todos los cambios realizados sobre una oferta.
- **Auth_Service**: Servicio responsable de autenticación y autorización mediante JWT.
- **User_Manager**: Módulo de gestión de usuarios del sistema.
- **Dashboard**: Vista principal que muestra ofertas agrupadas por estado.
- **Admin**: Usuario con rol ADMIN, con acceso completo al sistema.
- **Employee**: Usuario con rol EMPLOYEE, con acceso restringido a operaciones sobre ofertas.
- **Offer_Type**: Categoría de oferta. Valores válidos: Oferta interna, Feria de descuento, Folleto, Octavilla, Ladrillazo, Oportuneasy, Black Week, Ciberweek.
- **Sector**: Área comercial de la tienda. Valores válidos: 13 Ferretería, 41 Baños, 45 Herramientas, 46 Automotor, 58 Plomería, 43 Construcción, 48 Pinturas, 56 Aberturas, 57 Maderas, 49 Flooring, 59 Organizadores, 47 Muebles, 39 Electro, 12 Textil Hogar, 16 Menaje y Deco, 23 Outdoor, 50 Iluminación, 51 Electricidad, 53 Jardín, 64 Ampolletas, Hacks and Racks.
- **JWT**: JSON Web Token, mecanismo de autenticación stateless.
- **Audit_Log**: Registro inmutable de cambios realizados sobre una oferta.

---

## Requerimientos

### Requerimiento 1: Autenticación de usuarios

**User Story:** Como usuario del sistema, quiero autenticarme con mis credenciales, para que pueda acceder a las funcionalidades según mi rol.

#### Criterios de Aceptación

1. WHEN un usuario envía credenciales válidas (username y password), THE Auth_Service SHALL retornar un JWT con el rol del usuario y una vigencia de 8 horas.
2. WHEN un usuario envía credenciales inválidas, THE Auth_Service SHALL retornar un error HTTP 401 con un mensaje descriptivo.
3. WHEN una solicitud llega sin JWT o con un JWT expirado, THE Auth_Service SHALL rechazar la solicitud con HTTP 401.
4. WHEN una solicitud llega con un JWT válido pero sin el rol requerido para el recurso, THE Auth_Service SHALL rechazar la solicitud con HTTP 403.
5. THE Auth_Service SHALL almacenar las contraseñas usando un algoritmo de hash seguro (BCrypt con factor de costo mínimo 10).

---

### Requerimiento 2: Gestión de usuarios (Admin)

**User Story:** Como Admin, quiero crear y gestionar cuentas de empleados, para que el equipo pueda acceder al sistema con sus propias credenciales.

#### Criterios de Aceptación

1. WHEN un Admin envía una solicitud de creación de usuario con username, full_name, password y rol válidos, THE User_Manager SHALL persistir el usuario con estado activo y retornar HTTP 201.
2. WHEN un Admin intenta crear un usuario con un username ya existente, THE User_Manager SHALL retornar HTTP 409 con un mensaje descriptivo.
3. WHEN un Admin desactiva un usuario, THE User_Manager SHALL marcar el campo is_active como false y el usuario no podrá autenticarse.
4. WHEN un Employee intenta acceder a los endpoints de gestión de usuarios, THE Auth_Service SHALL rechazar la solicitud con HTTP 403.
5. THE User_Manager SHALL permitir únicamente los roles ADMIN y EMPLOYEE como valores válidos para el campo role.

---

### Requerimiento 3: Creación de ofertas

**User Story:** Como Admin o Employee, quiero registrar una nueva oferta, para que quede centralizada en el sistema con toda su información relevante.

#### Criterios de Aceptación

1. WHEN un usuario autenticado envía una solicitud POST /api/offers con title, offer_type_id, sector_id, starts_at y ends_at válidos, THE Offer_Manager SHALL persistir la oferta y retornar HTTP 201 con el recurso creado.
2. WHEN la fecha starts_at es posterior a ends_at, THE Offer_Manager SHALL rechazar la solicitud con HTTP 400 y un mensaje descriptivo.
3. WHEN se crea una oferta, THE Status_Engine SHALL asignar automáticamente el estado según las fechas: "próxima" si starts_at es futura, "activa" si la fecha actual está entre starts_at y ends_at.
4. WHEN se crea una oferta, THE Offer_Manager SHALL registrar el campo created_by con el id del usuario autenticado.
5. WHEN se crea una oferta, THE Audit_Logger SHALL registrar un evento de tipo CREATE en el Audit_Log con el id del usuario y timestamp.
6. WHEN el offer_type_id o sector_id enviado no existe en el sistema, THE Offer_Manager SHALL retornar HTTP 400 con un mensaje descriptivo.

---

### Requerimiento 4: Edición de ofertas

**User Story:** Como Admin o Employee, quiero modificar los datos de una oferta existente, para que la información se mantenga actualizada.

#### Criterios de Aceptación

1. WHEN un usuario autenticado envía una solicitud PUT /api/offers/{id} con datos válidos, THE Offer_Manager SHALL actualizar la oferta y retornar HTTP 200 con el recurso actualizado.
2. WHEN se edita una oferta, THE Status_Engine SHALL recalcular el estado de la oferta en función de las nuevas fechas.
3. WHEN se edita una oferta, THE Offer_Manager SHALL actualizar el campo updated_by con el id del usuario autenticado y updated_at con el timestamp actual.
4. WHEN se edita una oferta, THE Audit_Logger SHALL registrar un evento de tipo UPDATE en el Audit_Log por cada campo modificado, incluyendo old_value y new_value.
5. WHEN un usuario intenta editar una oferta con id inexistente, THE Offer_Manager SHALL retornar HTTP 404.
6. WHEN la nueva fecha starts_at es posterior a ends_at, THE Offer_Manager SHALL rechazar la solicitud con HTTP 400.

---

### Requerimiento 5: Eliminación de ofertas

**User Story:** Como Admin, quiero eliminar una oferta manualmente, para que registros incorrectos o cancelados no permanezcan en el sistema.

#### Criterios de Aceptación

1. WHEN un Admin envía una solicitud DELETE /api/offers/{id}, THE Offer_Manager SHALL eliminar la oferta y retornar HTTP 204.
2. WHEN un Admin elimina una oferta, THE Audit_Logger SHALL registrar un evento de tipo DELETE en el Audit_Log con el id del usuario y timestamp.
3. WHEN un Employee intenta eliminar una oferta, THE Auth_Service SHALL rechazar la solicitud con HTTP 403.
4. WHEN un Admin intenta eliminar una oferta con id inexistente, THE Offer_Manager SHALL retornar HTTP 404.

---

### Requerimiento 6: Consulta y filtrado de ofertas

**User Story:** Como Admin o Employee, quiero buscar y filtrar ofertas, para que pueda encontrar rápidamente la información que necesito.

#### Criterios de Aceptación

1. WHEN un usuario autenticado envía GET /api/offers, THE Offer_Manager SHALL retornar la lista de ofertas paginada con un máximo de 50 registros por página.
2. WHEN un usuario envía GET /api/offers con parámetros de filtro (sector_id, offer_type_id, status, starts_at, ends_at), THE Offer_Manager SHALL retornar únicamente las ofertas que cumplan todos los filtros aplicados.
3. WHEN un usuario envía GET /api/offers/{id}, THE Offer_Manager SHALL retornar la oferta correspondiente o HTTP 404 si no existe.
4. THE Offer_Manager SHALL responder a las consultas de listado en menos de 500ms para colecciones de hasta 10.000 ofertas.
5. WHEN un usuario envía GET /api/offers sin filtros, THE Offer_Manager SHALL retornar las ofertas ordenadas por starts_at descendente por defecto.

---

### Requerimiento 7: Cálculo automático de estados

**User Story:** Como usuario del sistema, quiero que el estado de las ofertas se actualice automáticamente, para que no tenga que hacerlo manualmente.

#### Criterios de Aceptación

1. THE Status_Engine SHALL calcular el estado de cada oferta según las siguientes reglas: "próxima" cuando la fecha actual es anterior a starts_at, "activa" cuando la fecha actual está entre starts_at y ends_at (inclusive), "vencida" cuando la fecha actual es posterior a ends_at.
2. WHEN el Status_Engine evalúa el estado de una oferta, THE Status_Engine SHALL usar la fecha y hora del servidor en UTC como referencia.
3. THE Status_Engine SHALL recalcular los estados de todas las ofertas con una frecuencia máxima de 1 hora mediante un proceso programado.

---

### Requerimiento 8: Eliminación automática de ofertas vencidas

**User Story:** Como Admin, quiero que las ofertas vencidas se eliminen automáticamente después de un período de retención, para que el sistema no acumule datos obsoletos.

#### Criterios de Aceptación

1. THE Cleanup_Job SHALL ejecutarse diariamente y eliminar todas las ofertas cuyo campo ends_at sea anterior a 21 días desde la fecha actual.
2. WHEN el Cleanup_Job elimina una oferta, THE Audit_Logger SHALL registrar un evento de tipo AUTO_DELETE en el Audit_Log con el timestamp de ejecución.
3. WHILE el Cleanup_Job está en ejecución, THE System SHALL continuar respondiendo solicitudes de la API sin interrupción.

---

### Requerimiento 9: Auditoría de cambios

**User Story:** Como Admin, quiero consultar el historial de cambios de una oferta, para que pueda rastrear quién modificó qué y cuándo.

#### Criterios de Aceptación

1. THE Audit_Logger SHALL registrar en Offer_Audit_Log los campos: offer_id, changed_by, change_type, field_changed, old_value, new_value, observation y created_at para cada evento de cambio.
2. WHEN un Admin consulta GET /api/offers/{id}/audit, THE Audit_Logger SHALL retornar todos los registros de auditoría asociados a esa oferta ordenados por created_at descendente.
3. WHEN un Employee intenta acceder al historial de auditoría, THE Auth_Service SHALL rechazar la solicitud con HTTP 403.
4. THE Audit_Logger SHALL preservar los registros de auditoría incluso después de que la oferta asociada sea eliminada.

---

### Requerimiento 10: Dashboard de ofertas

**User Story:** Como Admin o Employee, quiero ver un resumen visual de las ofertas agrupadas por estado, para que pueda tener una visión general rápida del estado comercial.

#### Criterios de Aceptación

1. WHEN un usuario autenticado accede al Dashboard, THE System SHALL mostrar el conteo de ofertas activas, próximas y vencidas.
2. WHEN un usuario autenticado accede al Dashboard, THE System SHALL mostrar las ofertas activas y próximas más recientes (hasta 10 por estado).
3. THE System SHALL actualizar los datos del Dashboard en cada carga de página sin requerir acción adicional del usuario.

---

### Requerimiento 11: Requerimientos no funcionales

**User Story:** Como equipo de desarrollo, queremos que el sistema cumpla estándares de calidad técnica, para que sea seguro, mantenible y escalable.

#### Criterios de Aceptación

1. THE System SHALL ejecutarse en contenedores Docker, incluyendo backend, frontend y base de datos, orquestados mediante docker-compose.
2. THE System SHALL implementar la arquitectura en capas: API (Controllers), Application (Services), Domain (Entities), Infrastructure (Persistencia), sin dependencias inversas entre capas.
3. THE Auth_Service SHALL usar JWT firmado con algoritmo HS256 y clave secreta configurable por variable de entorno.
4. THE System SHALL registrar logs de nivel ERROR o superior en todos los componentes ante fallos inesperados.
5. THE System SHALL usar PostgreSQL como motor de base de datos con índices en los campos offer_type_id, sector_id, status y starts_at de la tabla offers para garantizar el criterio de performance del Requerimiento 6.
