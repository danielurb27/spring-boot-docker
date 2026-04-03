package com.easy.offers.auth.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig — Configuración central de Spring Security.
 *
 * Esta clase define:
 * 1. Qué endpoints son públicos y cuáles requieren autenticación
 * 2. Qué endpoints requieren un rol específico (ADMIN vs EMPLOYEE)
 * 3. Que el sistema es STATELESS (sin sesiones HTTP)
 * 4. Dónde se inserta nuestro JwtAuthFilter en la cadena de filtros
 * 5. El algoritmo de hash para contraseñas (BCrypt)
 *
 * @Configuration: esta clase define beans de Spring (métodos con @Bean).
 * @EnableWebSecurity: activa la configuración de seguridad web de Spring.
 * @EnableMethodSecurity: habilita @PreAuthorize en los controladores.
 *   Con esto podemos escribir: @PreAuthorize("hasRole('ADMIN')") en métodos.
 *
 * ¿Qué es una SecurityFilterChain?
 * Es la cadena de filtros que procesa cada request HTTP.
 * Definimos las reglas de seguridad aquí y Spring Security las aplica
 * automáticamente a cada request.
 *
 * Requerimiento: 1.3, 1.4, 2.4, 5.3, 9.3 — Control de acceso por endpoint y rol.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /**
     * Define la cadena de filtros de seguridad.
     *
     * @Bean: Spring registra el resultado de este método como un bean.
     * HttpSecurity: builder fluido para configurar la seguridad HTTP.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // ─────────────────────────────────────────────────────────────
                // 1. Deshabilitar CSRF (Cross-Site Request Forgery)
                //
                // ¿Qué es CSRF?
                // Un ataque donde un sitio malicioso hace que el navegador del
                // usuario envíe requests a nuestra API sin que el usuario lo sepa.
                //
                // ¿Por qué deshabilitarlo?
                // CSRF es relevante para aplicaciones con sesiones y cookies.
                // Nuestra API es STATELESS y usa JWT en el header Authorization.
                // Los tokens JWT no se envían automáticamente por el navegador
                // (a diferencia de las cookies), por lo que CSRF no aplica.
                // ─────────────────────────────────────────────────────────────
                .csrf(AbstractHttpConfigurer::disable)

                // ─────────────────────────────────────────────────────────────
                // 2. Configuración STATELESS (sin sesiones HTTP)
                //
                // SessionCreationPolicy.STATELESS: Spring Security NO crea ni
                // usa sesiones HTTP. Cada request debe autenticarse con su JWT.
                //
                // Ventajas del enfoque stateless:
                // - Escalabilidad horizontal: cualquier instancia del servidor
                //   puede atender cualquier request (no hay estado compartido).
                // - Sin problemas de sesiones expiradas o robadas.
                // - Más simple de razonar: cada request es independiente.
                // ─────────────────────────────────────────────────────────────
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ─────────────────────────────────────────────────────────────
                // 3. Reglas de autorización por endpoint
                //
                // El orden importa: las reglas más específicas van primero.
                // Spring Security evalúa las reglas en orden y aplica la primera
                // que coincide con la request.
                // ─────────────────────────────────────────────────────────────
                .authorizeHttpRequests(auth -> auth

                        // ── Endpoints PÚBLICOS (sin autenticación) ──
                        // Solo el login es público. Todo lo demás requiere JWT.
                        .requestMatchers("/api/auth/**").permitAll()

                        // ── Endpoints solo para ADMIN ──
                        // DELETE de ofertas: solo ADMIN puede eliminar (Req 5.3)
                        .requestMatchers(HttpMethod.DELETE, "/api/offers/**").hasRole("ADMIN")

                        // Gestión de usuarios: solo ADMIN (Req 2.4)
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        // Historial de auditoría: solo ADMIN (Req 9.3)
                        .requestMatchers("/api/offers/*/audit").hasRole("ADMIN")

                        // ── Todos los demás endpoints: autenticado (cualquier rol) ──
                        // GET, POST, PUT de ofertas: ADMIN y EMPLOYEE
                        // Dashboard: ADMIN y EMPLOYEE
                        .anyRequest().authenticated()
                )

                // ─────────────────────────────────────────────────────────────
                // 4. Insertar nuestro JwtAuthFilter ANTES del filtro estándar
                //
                // UsernamePasswordAuthenticationFilter: el filtro estándar de
                // Spring Security para autenticación con usuario/contraseña.
                // Nosotros lo reemplazamos con JWT, así que insertamos nuestro
                // filtro ANTES para que procese el JWT primero.
                //
                // Si nuestro filtro carga el SecurityContext exitosamente,
                // el filtro estándar no hace nada (ya hay autenticación).
                // ─────────────────────────────────────────────────────────────
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }

    /**
     * Bean de BCryptPasswordEncoder para hashear contraseñas.
     *
     * ¿Por qué BCrypt?
     * - Incluye un salt aleatorio automáticamente (evita ataques de rainbow table)
     * - El factor de costo (strength) controla cuántas iteraciones se hacen
     * - Factor 10 = 2^10 = 1024 iteraciones: balance entre seguridad y velocidad
     * - Cada hash tarda ~100ms en generarse, lo que hace inviable la fuerza bruta
     *
     * ¿Por qué definirlo como @Bean?
     * Al ser un bean de Spring, puede inyectarse en cualquier servicio que
     * necesite hashear o verificar contraseñas (AuthService, UserService).
     * Evita crear instancias múltiples del encoder.
     *
     * Requerimiento: 1.5 — BCrypt con factor de costo mínimo 10.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCryptPasswordEncoder(10): factor de costo 10 (el default también es 10)
        // Siendo explícito para documentar la decisión de seguridad.
        return new BCryptPasswordEncoder(10);
    }
}
