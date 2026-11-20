package br.pucpr.authserver.config

import br.pucpr.authserver.security.JsonAccessDeniedHandler
import br.pucpr.authserver.security.JsonAuthEntryPoint
import br.pucpr.authserver.security.JwtAuthFilter
import br.pucpr.authserver.security.JwtService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity, jwtService: JwtService): SecurityFilterChain {
        http
            .csrf { it.disable() }                       // API stateless com JWT, sem cookies de sessão
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .headers { headers -> headers.frameOptions { it.sameOrigin() } } // console do H2 usa frames
            .exceptionHandling {
                it.authenticationEntryPoint(JsonAuthEntryPoint())
                it.accessDeniedHandler(JsonAccessDeniedHandler())
            }
            .authorizeHttpRequests { auth ->
                auth
                    // Públicas: login, cadastro, consulta de eventos, documentação
                    .requestMatchers(
                        "/auth/**", "/error", "/h2-console/**",
                        "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**"
                    ).permitAll()
                    .requestMatchers(HttpMethod.POST, "/organizadores", "/participantes").permitAll()
                    .requestMatchers(HttpMethod.GET, "/eventos", "/eventos/**").permitAll()

                    // Só organizador: criar/editar/excluir eventos e listar participantes/organizadores
                    .requestMatchers("/eventos/**").hasRole("ORGANIZADOR")
                    .requestMatchers(HttpMethod.GET, "/organizadores", "/participantes").hasRole("ORGANIZADOR")

                    // Organizador só mexe no próprio cadastro (a conferência do ID é feita no controller)
                    .requestMatchers("/organizadores/**").hasRole("ORGANIZADOR")

                    // Participante: dono do cadastro ou organizador (conferido no service)
                    .requestMatchers("/participantes/**").authenticated()

                    // Tickets (ponte participante <-> evento): listar só organizador;
                    // criar/ver/excluir: dono do ticket ou organizador (conferido no service)
                    .requestMatchers(HttpMethod.GET, "/tickets").hasRole("ORGANIZADOR")
                    .requestMatchers("/tickets/**", "/tickets").authenticated()

                    .anyRequest().authenticated()
            }
            .addFilterBefore(JwtAuthFilter(jwtService), UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
