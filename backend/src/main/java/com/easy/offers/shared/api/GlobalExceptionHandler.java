package com.easy.offers.shared.api;

import com.easy.offers.shared.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * GlobalExceptionHandler — Manejador centralizado de excepciones de la API.
 *
 * ¿Qué problema resuelve?
 * Sin este handler, cada excepción no capturada retornaría un error 500 genérico
 * de Spring Boot con información interna del sistema (stack traces, nombres de clases).
 * Esto es malo por dos razones:
 * 1. Seguridad: expone detalles internos que un atacante podría aprovechar.
 * 2. UX: el cliente recibe mensajes incomprensibles en lugar de errores claros.
 *
 * @RestControllerAdvice: combina @ControllerAdvice + @ResponseBody.
 * - @ControllerAdvice: intercepta excepciones lanzadas por cualquier controlador.
 * - @ResponseBody: los métodos retornan JSON en lugar de vistas HTML.
 *
 * Funciona como un "catch global" para toda la capa API.
 * Cuando un controlador o servicio lanza una excepción, Spring la propaga
 * hasta aquí donde la convertimos en una respuesta HTTP apropiada.
 *
 * Principio: las excepciones de dominio no saben nada de HTTP.
 * Este handler es el único lugar donde se hace la traducción
 * excepción de dominio → código HTTP.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Logger para registrar errores inesperados.
     * SLF4J es la fachada de logging estándar en Java.
     * Spring Boot usa Logback como implementación por defecto.
     */
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // =========================================================================
    // Handlers específicos por tipo de excepción
    // Orden: de más específico a más general
    // =========================================================================

    /**
     * 401 Unauthorized — Credenciales inválidas.
     * Requerimiento 1.2: HTTP 401 para credenciales incorrectas.
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    /**
     * 401 Unauthorized — Token JWT inválido o expirado.
     * Requerimiento 1.3: HTTP 401 para tokens inválidos.
     */
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleTokenExpired(
            TokenExpiredException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    /**
     * 403 Forbidden — Rol insuficiente para el recurso.
     * Requerimiento 1.4: HTTP 403 cuando el rol es insuficiente.
     *
     * También captura AccessDeniedException de Spring Security,
     * que se lanza cuando @PreAuthorize falla.
     */
    @ExceptionHandler(InsufficientRoleException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientRole(
            InsufficientRoleException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    /**
     * 403 Forbidden — Spring Security lanza AccessDeniedException
     * cuando un usuario autenticado intenta acceder a un recurso sin permiso.
     * Necesitamos capturarla aquí para retornar nuestro formato estándar.
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            org.springframework.security.access.AccessDeniedException ex,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN,
                "Acceso denegado. No tiene permisos para realizar esta operación.", request);
    }

    /**
     * 404 Not Found — Recurso no encontrado.
     * Requerimiento 4.5, 5.4, 6.3: HTTP 404 para recursos inexistentes.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /**
     * 404 Not Found — Endpoint no existe (controlador no implementado aún).
     * NoResourceFoundException se lanza cuando Spring MVC no encuentra ningún
     * handler para la URL solicitada y tampoco existe como recurso estático.
     */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            org.springframework.web.servlet.resource.NoResourceFoundException ex,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND,
                "El endpoint '" + request.getRequestURI() + "' no existe.", request);
    }

    /**
     * 409 Conflict — Username duplicado.
     * Requerimiento 2.2: HTTP 409 cuando el username ya existe.
     */
    @ExceptionHandler(DuplicateUsernameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateUsername(
            DuplicateUsernameException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /**
     * 400 Bad Request — Rango de fechas inválido.
     * Requerimiento 3.2, 4.6: HTTP 400 cuando starts_at >= ends_at.
     */
    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDateRange(
            InvalidDateRangeException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /**
     * 400 Bad Request — Referencia inválida (offer_type_id o sector_id inexistente).
     * Requerimiento 3.6: HTTP 400 para referencias inválidas.
     */
    @ExceptionHandler(InvalidReferenceException.class)
    public ResponseEntity<ErrorResponse> handleInvalidReference(
            InvalidReferenceException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /**
     * 400 Bad Request — Rol inválido.
     * Requerimiento 2.5: HTTP 400 para roles no válidos.
     */
    @ExceptionHandler(InvalidRoleException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRole(
            InvalidRoleException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /**
     * 400 Bad Request — Validación de Bean Validation fallida (@NotBlank, @Size, etc.).
     *
     * MethodArgumentNotValidException se lanza cuando @Valid falla en un @RequestBody.
     * Contiene todos los errores de validación de todos los campos.
     *
     * Construimos un mensaje que lista todos los campos con error:
     * "username: El username es obligatorio; password: La contraseña es obligatoria"
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        // Recopilar todos los mensajes de error de validación
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    /**
     * 500 Internal Server Error — Cualquier excepción no manejada.
     *
     * Este es el handler de "último recurso". Captura cualquier excepción
     * que no fue capturada por los handlers anteriores.
     *
     * Decisión de seguridad:
     * - Logueamos el stack trace completo (para debugging interno).
     * - Retornamos un mensaje GENÉRICO al cliente (no exponemos detalles internos).
     *
     * Requerimiento 11.4: registrar logs de nivel ERROR ante fallos inesperados.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception ex, HttpServletRequest request) {

        // Loguear con nivel ERROR incluyendo el stack trace completo
        // El segundo parámetro de log.error() es el Throwable para incluir el stack trace
        log.error("Error inesperado en {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ha ocurrido un error interno. Por favor intente nuevamente.",
                request
        );
    }

    // =========================================================================
    // Método auxiliar para construir respuestas de error consistentes
    // =========================================================================

    /**
     * Construye una ResponseEntity con el formato estándar de error.
     *
     * @param status  Código HTTP
     * @param message Mensaje descriptivo del error
     * @param request La request HTTP (para extraer el path)
     * @return ResponseEntity con ErrorResponse en el body
     */
    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status, String message, HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),  // "Bad Request", "Unauthorized", etc.
                message,
                Instant.now(),
                request.getRequestURI()    // El path del endpoint que generó el error
        );

        return ResponseEntity.status(status).body(error);
    }
}
