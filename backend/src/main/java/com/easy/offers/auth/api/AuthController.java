package com.easy.offers.auth.api;

import com.easy.offers.auth.application.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController — Controlador REST para el módulo de autenticación.
 *
 * Responsabilidad: recibir requests HTTP, delegar al servicio, retornar respuestas.
 * NO contiene lógica de negocio — eso es responsabilidad de AuthService.
 *
 * @RestController: combina @Controller + @ResponseBody.
 * - @Controller: marca la clase como un controlador Spring MVC.
 * - @ResponseBody: los métodos retornan datos directamente en el body de la response
 *   (serializado a JSON por Jackson), en lugar de nombres de vistas HTML.
 *
 * @RequestMapping("/api/auth"): prefijo de URL para todos los endpoints de este controlador.
 * Todos los métodos heredan este prefijo: /api/auth/login, /api/auth/logout, etc.
 *
 * Regla de Clean Architecture: los controladores solo conocen DTOs y servicios.
 * No importan entidades de dominio ni entidades JPA directamente.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/login — Autenticar usuario y obtener JWT.
     *
     * @PostMapping: mapea requests POST a este método.
     * @RequestBody: deserializa el JSON del body a LoginRequest.
     * @Valid: activa las validaciones de Bean Validation (@NotBlank, etc.).
     *   Si la validación falla, Spring retorna 400 automáticamente antes de
     *   llegar a este método.
     *
     * ResponseEntity<T>: permite controlar el código HTTP de la respuesta.
     * ResponseEntity.ok(body): retorna HTTP 200 con el body serializado a JSON.
     *
     * Flujo:
     * 1. Jackson deserializa el JSON a LoginRequest
     * 2. @Valid valida los campos (@NotBlank)
     * 3. authService.authenticate() verifica credenciales y genera JWT
     * 4. Retornamos HTTP 200 con el token
     *
     * Si las credenciales son inválidas, AuthService lanza InvalidCredentialsException
     * que el GlobalExceptionHandler convierte a HTTP 401.
     *
     * Requerimiento: 1.1 — Retornar JWT con rol y vigencia de 8 horas.
     * Requerimiento: 1.2 — HTTP 401 para credenciales inválidas.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // Delegar al servicio — el controlador no sabe cómo se autentica
        AuthService.TokenResponse tokenResponse = authService.authenticate(
                request.username(),
                request.password()
        );

        // Construir la respuesta con el factory method
        return ResponseEntity.ok(
                LoginResponse.of(tokenResponse.token(), tokenResponse.expiresAt())
        );
    }
}
