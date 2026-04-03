package com.easy.offers.auth.api;

import jakarta.validation.constraints.NotBlank;

/**
 * LoginRequest — DTO de entrada para el endpoint POST /api/auth/login.
 *
 * ¿Qué es un DTO (Data Transfer Object)?
 * Es un objeto cuya única responsabilidad es transportar datos entre capas.
 * En la API, los DTOs representan el contrato de entrada/salida de los endpoints.
 * Son diferentes de las entidades de dominio: no tienen lógica de negocio.
 *
 * @NotBlank: validación de Bean Validation.
 * Si el campo es null, vacío o solo espacios, Spring retorna automáticamente
 * HTTP 400 con un mensaje de error antes de llegar al controlador.
 * Esto evita que el servicio reciba datos inválidos.
 *
 * message: personaliza el mensaje de error en la respuesta.
 *
 * ¿Por qué record y no clase?
 * Los DTOs de entrada son inmutables: una vez deserializados del JSON,
 * no deben cambiar. Los records son perfectos para esto.
 * Jackson (la librería JSON de Spring) puede deserializar records
 * a partir de Spring Boot 2.7+.
 */
public record LoginRequest(
        @NotBlank(message = "El username es obligatorio")
        String username,

        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {}
