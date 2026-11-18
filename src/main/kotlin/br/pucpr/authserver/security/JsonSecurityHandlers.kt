package br.pucpr.authserver.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler

/** 401: sem token, token inválido ou expirado. Responde no mesmo formato do ErroResponse. */
class JsonAuthEntryPoint : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        val mensagem = request.getAttribute(JwtAuthFilter.ATRIBUTO_ERRO) as? String
            ?: "Autenticação necessária. Envie o header Authorization: Bearer <token>."
        escreverErro(response, 401, "Unauthorized", mensagem)
    }
}

/** 403: token válido, mas o perfil não pode acessar a rota. */
class JsonAccessDeniedHandler : AccessDeniedHandler {
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        escreverErro(response, 403, "Forbidden", "Seu perfil não tem permissão para acessar este recurso.")
    }
}

private fun escreverErro(response: HttpServletResponse, status: Int, erro: String, mensagem: String) {
    val mensagemJson = mensagem.replace("\\", "\\\\").replace("\"", "\\\"")
    response.status = status
    response.contentType = "application/json;charset=UTF-8"
    response.writer.write("""{"status":$status,"erro":"$erro","mensagem":"$mensagemJson"}""")
}
