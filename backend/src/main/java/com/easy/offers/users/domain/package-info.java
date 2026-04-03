/**
 * Paquete: users.domain — Capa de Dominio del módulo de usuarios.
 *
 * ¿Qué va aquí?
 * Las entidades y enums de dominio puro (sin Spring ni JPA):
 *   - User: record inmutable que representa un usuario del sistema
 *   - UserRole: enum con los roles válidos (ADMIN, EMPLOYEE)
 *
 * ¿Por qué usar records de Java para las entidades de dominio?
 * Los records son inmutables por defecto: una vez creados, sus campos no cambian.
 * Para "modificar" un usuario, se crea una nueva instancia con los campos actualizados.
 * Esto elimina bugs de estado mutable y hace el código más predecible.
 *
 * Ejemplo: en lugar de user.setActive(false), hacemos:
 *   User deactivated = new User(user.id(), user.username(), ..., false, ...);
 *
 * Regla: NUNCA importar @Entity, @Column, @Service ni ninguna anotación de framework aquí.
 */
package com.easy.offers.users.domain;
