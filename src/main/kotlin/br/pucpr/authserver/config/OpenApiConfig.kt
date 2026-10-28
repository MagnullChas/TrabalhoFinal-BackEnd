package br.pucpr.authserver.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// Opcional: habilita o botão "Authorize" no Swagger UI (/swagger-ui/index.html)
@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI = OpenAPI()
        .components(
            Components().addSecuritySchemes(
                "bearerAuth",
                SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer")
            )
        )
        .addSecurityItem(SecurityRequirement().addList("bearerAuth"))
}
