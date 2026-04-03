package com.easy.offers.users.domain;

/**
 * UserRole — Enum que representa los roles posibles de un usuario en el sistema.
 *
 * ¿Qué es un enum en Java?
 * Un enum (enumeración) es un tipo especial que define un conjunto fijo y conocido
 * de constantes. En lugar de usar strings como "ADMIN" o "EMPLOYEE" dispersos
 * por el código (lo que se llama "magic strings"), centralizamos los valores
 * válidos en un solo lugar.
 *
 * Ventajas de usar enum en lugar de String:
 * 1. Seguridad de tipos: el compilador detecta errores. Si escribes UserRole.ADMINN
 *    (con doble N), el código no compila. Con strings, el error aparece en runtime.
 * 2. Autocompletado: el IDE sugiere los valores válidos.
 * 3. Refactoring seguro: si renombramos ADMIN a ADMINISTRATOR, el IDE actualiza
 *    todas las referencias automáticamente.
 * 4. Exhaustividad: en un switch/when, el compilador avisa si falta un caso.
 *
 * Ubicación en la arquitectura:
 * Este enum vive en la capa DOMAIN del módulo users.
 * No tiene dependencias de Spring ni de JPA — es código Java puro.
 * La capa Infrastructure (JPA) lo usa con @Enumerated(EnumType.STRING)
 * para almacenarlo como texto en la BD.
 *
 * Requerimiento: 2.5 — Solo se permiten roles ADMIN y EMPLOYEE.
 */
public enum UserRole {

    /**
     * ADMIN: Administrador del sistema.
     * Puede: crear/editar/eliminar ofertas, ver auditoría, gestionar usuarios.
     * Es el rol con acceso completo.
     */
    ADMIN,

    /**
     * EMPLOYEE: Empleado que carga promociones.
     * Puede: crear y editar ofertas.
     * No puede: eliminar ofertas, ver auditoría, gestionar usuarios.
     */
    EMPLOYEE
}
