package br.pucpr.authserver.security

import io.jsonwebtoken.ExpiredJwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Lê o header "Authorization: Bearer <jwt>", valida o token e, se estiver ok,
 * coloca o usuário no contexto do Spring Security.
 * Se o token for inválido, só registra o motivo: quem responde 401 é o JsonAuthEntryPoint
 * (e só nas rotas que exigem autenticação).
 */
class JwtAuthFilter(
    private val jwtService: JwtService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")

        if (header != null) {
            if (!header.startsWith("Bearer ", ignoreCase = true)) {
                request.setAttribute(ATRIBUTO_ERRO, "Use o formato: Authorization: Bearer <token>.")
            } else {
                try {
                    val usuario = jwtService.lerToken(header.substring(7).trim())
                    val autoridades = listOf(SimpleGrantedAuthority("ROLE_${usuario.perfil.name}"))
                    SecurityContextHolder.getContext().authentication =
                        UsernamePasswordAuthenticationToken(usuario, null, autoridades)
                } catch (ex: ExpiredJwtException) {
                    request.setAttribute(ATRIBUTO_ERRO, "Token expirado. Faça login novamente.")
                } catch (ex: Exception) {
                    request.setAttribute(ATRIBUTO_ERRO, "Token inválido.")
                }
            }
        }

        filterChain.doFilter(request, response)
    }

    companion object {
        const val ATRIBUTO_ERRO = "jwt.erro"
    }
}
