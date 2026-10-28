package br.pucpr.authserver.config

import br.pucpr.authserver.security.AuthInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig(
    private val authInterceptor: AuthInterceptor
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authInterceptor)
            .addPathPatterns("/eventos/**", "/participantes/**", "/organizadores/**")
    }
}
