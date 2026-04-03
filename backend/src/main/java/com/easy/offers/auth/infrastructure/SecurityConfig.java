package com.easy.offers.auth.infrastructure;

import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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

    /**
     * Orígenes permitidos para CORS.
     * Se configura via variable de entorno ALLOWED_ORIGINS para flexibilidad.
     * Ejemplo: "https://mi-frontend.onrender.com,http://localhost:4200"
     * Si no se define, permite localhost para desarrollo local.
     */
    @Value("${cors.allowed-origins:http://localhost:4200,http://localhost:8081}")
    private String allowedOriginsStr;

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

                // CORS: usar la configuración definida en corsConfigurationSource()
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

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
     * Configuración de CORS.
     * Permite requests desde los orígenes definidos en ALLOWED_ORIGINS.
     * Para producción en Render, configurar la variable de entorno:
     * ALLOWED_ORIGINS=https://tu-frontend.onrender.com
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Parsear los orígenes separados por coma
        List<String> origins = List.of(allowedOriginsStr.split(","));
        config.setAllowedOrigins(origins);

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        // Necesario para enviar el header Authorization con el JWT
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Bean de BCryptPasswordEncoder para hashear contraseñas.
     * Requerimiento: 1.5 — BCrypt con factor de costo mínimo 10.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCryptPasswordEncoder(10): factor de costo 10 (el default también es 10)
        // Siendo explícito para documentar la decisión de seguridad.
        return new BCryptPasswordEncoder(10);
    }
}
