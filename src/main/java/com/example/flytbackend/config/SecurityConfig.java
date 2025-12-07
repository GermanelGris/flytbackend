// java
    package com.example.flytbackend.config;

    import com.example.flytbackend.filter.JwtFilter;
    import lombok.RequiredArgsConstructor;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.http.HttpMethod;
    import org.springframework.security.authentication.AuthenticationManager;
    import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
    import org.springframework.security.config.annotation.web.builders.HttpSecurity;
    import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
    import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
    import org.springframework.security.config.http.SessionCreationPolicy;
    import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.security.web.SecurityFilterChain;
    import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
    import org.springframework.web.cors.CorsConfigurationSource;

    @Configuration
    @EnableWebSecurity
    @RequiredArgsConstructor
    public class SecurityConfig {

        private final CorsConfigurationSource corsConfigurationSource;
        private final JwtFilter jwtFilter;

        @Bean
        public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
            return cfg.getAuthenticationManager();
        }

        @Bean
        public SecurityFilterChain chain(HttpSecurity http) throws Exception {
            http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(c -> c.configurationSource(corsConfigurationSource))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                    // permitir auth y recursos públicos
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/lugares/buscar/**").permitAll()
                    .requestMatchers("/api/aeropuertos/**").permitAll()
                    .requestMatchers("/api/aerolineas/**").permitAll()
                    .requestMatchers("/api/vuelos-programados/**").permitAll()

                    // permitir OpenAPI / Swagger UI
                    .requestMatchers("/v3/api-docs/**").permitAll()
                    .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/swagger-ui/index.html").permitAll()

                    // protege clientes con JWT
                    .requestMatchers(HttpMethod.GET, "/api/clientes/me").authenticated()
                    .requestMatchers("/api/clientes/**").authenticated()
                    // rutas admin separadas (si existen)
                    .requestMatchers("/api/clientes/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
            return http.build();
        }
}