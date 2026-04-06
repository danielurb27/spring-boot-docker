package com.easy.offers.users.api;

import com.easy.offers.users.application.UserService;
import com.easy.offers.users.domain.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * UserController — Controlador REST para la gestión de usuarios.
 * Solo accesible por ADMIN (doble protección: SecurityConfig + @PreAuthorize).
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /api/users — Listar todos los usuarios del sistema.
     * Permite al frontend mostrar todos los usuarios existentes, no solo los
     * creados en la sesión actual.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userService.findAll();
        List<UserResponse> response = users.stream()
                .map(UserResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/users — Crear un nuevo usuario.
     * Requerimiento: 2.1
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(
                request.fullName(),
                request.username(),
                request.password(),
                request.role()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    /**
     * PUT /api/users/{id} — Modificar datos de un usuario.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        User user = userService.updateUser(id, request.fullName(), request.password(), request.role());
        return ResponseEntity.ok(UserResponse.from(user));
    }

    /**
     * PATCH /api/users/{id}/activate — Reactivar un usuario desactivado.
     */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> activateUser(@PathVariable Long id) {
        User user = userService.activateUser(id);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    /**
     * PATCH /api/users/{id}/deactivate — Desactivar un usuario (soft delete).
     * Requerimiento: 2.3
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable Long id) {
        User user = userService.deactivateUser(id);
        return ResponseEntity.ok(UserResponse.from(user));
    }
}
