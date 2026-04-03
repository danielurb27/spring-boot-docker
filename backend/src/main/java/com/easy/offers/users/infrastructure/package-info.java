/**
 * Paquete: users.infrastructure — Capa de Infraestructura del módulo de usuarios.
 *
 * ¿Qué va aquí?
 * La persistencia de usuarios con JPA:
 *   - UserJpaEntity: clase con anotaciones @Entity, @Table, @Column que mapea
 *     la entidad de dominio User a la tabla "users" de PostgreSQL
 *   - UserJpaRepository: interfaz que extiende JpaRepository para operaciones CRUD
 *   - UserMapper: convierte entre User (dominio) y UserJpaEntity (infraestructura)
 *
 * ¿Por qué separar User (dominio) de UserJpaEntity (infraestructura)?
 * JPA requiere: constructor vacío, setters, y anotaciones específicas de Hibernate.
 * Estas restricciones "contaminan" el modelo de negocio si las mezclamos.
 * Al separar, el dominio permanece limpio y la infraestructura maneja los detalles
 * de persistencia. El mapper hace la traducción entre ambos mundos.
 */
package com.easy.offers.users.infrastructure;
