/**
 * Paquete: users.api — Capa de API del módulo de gestión de usuarios.
 *
 * ¿Qué va aquí?
 * Los controladores REST para gestión de usuarios (solo accesibles por ADMIN):
 *   - UserController: maneja POST /api/users y PATCH /api/users/{id}/deactivate
 *
 * Ver Requerimiento 2: Gestión de usuarios (Admin).
 * Solo el rol ADMIN puede crear usuarios y desactivarlos.
 * Los empleados (EMPLOYEE) reciben HTTP 403 si intentan acceder.
 */
package com.easy.offers.users.api;
