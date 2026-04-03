/**
 * Paquete: users.application — Capa de Aplicación del módulo de usuarios.
 *
 * ¿Qué va aquí?
 *   - UserService: lógica de creación y desactivación de usuarios
 *     - Valida que el username sea único (lanza DuplicateUsernameException si no)
 *     - Hashea la contraseña con BCrypt factor >= 10 (Requerimiento 1.5)
 *     - Valida que el rol sea ADMIN o EMPLOYEE (Requerimiento 2.5)
 *
 * Decisión de diseño: el hashing de contraseñas ocurre en la capa Application,
 * no en el controlador ni en el repositorio. Es lógica de negocio (seguridad),
 * no lógica de presentación ni de persistencia.
 */
package com.easy.offers.users.application;
