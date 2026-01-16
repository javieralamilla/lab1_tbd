package cl.usach.tbd.grupo4.plataforma_urbanismo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Autenticación pública
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        
                        // ZONAS - Administrador (CRUD), Planificador (editar), Autoridad Municipal (solo lectura)
                        .requestMatchers(HttpMethod.GET, "/api/zonas/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/zonas/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/zonas/**").hasAnyRole("ADMINISTRADOR", "PLANIFICADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/zonas/**").hasRole("ADMINISTRADOR")
                        
                        // PROYECTOS - Administrador y Planificador (CRUD), Autoridad Municipal (solo lectura)
                        .requestMatchers(HttpMethod.GET, "/api/proyectos/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/proyectos/**").hasAnyRole("ADMINISTRADOR", "PLANIFICADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/proyectos/**").hasAnyRole("ADMINISTRADOR", "PLANIFICADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/proyectos/**").hasAnyRole("ADMINISTRADOR", "PLANIFICADOR")
                        
                        // PUNTOS DE INTERÉS - Administrador y Planificador (CRUD), Autoridad Municipal (solo lectura)
                        .requestMatchers(HttpMethod.GET, "/api/puntos-interes/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/puntos-interes/**").hasAnyRole("ADMINISTRADOR", "PLANIFICADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/puntos-interes/**").hasAnyRole("ADMINISTRADOR", "PLANIFICADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/puntos-interes/**").hasAnyRole("ADMINISTRADOR", "PLANIFICADOR")
                        
                        // DATOS DEMOGRÁFICOS - Administrador (CRUD), Planificador (editar), Autoridad Municipal (solo lectura)
                        .requestMatchers(HttpMethod.GET, "/api/datos-demograficos/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/datos-demograficos/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/datos-demograficos/**").hasAnyRole("ADMINISTRADOR", "PLANIFICADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/datos-demograficos/**").hasRole("ADMINISTRADOR")
                        
                        // REPORTES - Administrador (gestión), Planificador (generar), Autoridad Municipal (ver/descargar)
                        .requestMatchers(HttpMethod.GET, "/api/reportes/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/reportes/**").hasAnyRole("ADMINISTRADOR", "PLANIFICADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/reportes/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/reportes/**").hasRole("ADMINISTRADOR")
                        
                        // Otros endpoints requieren autenticación
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://localhost:8080"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}