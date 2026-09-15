package br.com.campusgigs.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuracao de seguranca da API.
 *
 * Stateless: nenhuma sessao e criada: a identidade vem do JWT a cada requisicao.
 * Leitura publica dos servicos; qualquer operacao que altera dados exige token.
 * A autorizacao fina (dono do recurso / papel) fica no @PreAuthorize e nos services.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RespostaDeSegurancaHandlers handlers;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          RespostaDeSegurancaHandlers handlers) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.handlers = handlers;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // API stateless com token: nao ha sessao/cookie para um ataque CSRF explorar.
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(handlers.entryPoint())
                        .accessDeniedHandler(handlers.accessDeniedHandler()))
                .authorizeHttpRequests(auth -> auth
                        // Publico: cadastro, login e vitrine de servicos
                        .requestMatchers(HttpMethod.POST, "/auth/registrar", "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/servicos", "/servicos/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                        // Area administrativa: apenas ADMIN
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // Todo o resto exige token valido
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // A senha nunca e persistida em texto puro.
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
