package br.pucpr.authserver.security

import br.pucpr.authserver.exception.NaoAutorizadoException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AuthInterceptor(
    @Value("\${app.security.token}") private val tokenEsperado: String
) : HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        if (!exigeToken(request)) return true

        val header = request.getHeader("Authorization")
            ?: throw NaoAutorizadoException("Header Authorization ausente.")

        if (!header.startsWith("Bearer ", ignoreCase = true)) {
            throw NaoAutorizadoException("Use o formato: Authorization: Bearer <token>.")
        }

        val token = header.substring(7).trim()
        if (token.isBlank() || token != tokenEsperado) {
            throw NaoAutorizadoException("Token inválido.")
        }

        return true
    }

    /** Define quais rotas são protegidas. */
    private fun exigeToken(request: HttpServletRequest): Boolean {
        val caminho = request.requestURI
        val metodo = request.method.uppercase()

        return when {
            caminho.startsWith("/eventos") -> metodo == "DELETE"
            caminho.startsWith("/participantes") -> metodo == "PUT" || metodo == "DELETE"
            caminho.startsWith("/organizadores") -> metodo == "PUT" || metodo == "DELETE"
            else -> false
        }
    }
}
