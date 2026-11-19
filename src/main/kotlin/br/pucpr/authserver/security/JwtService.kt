package br.pucpr.authserver.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${app.security.jwt.secret}") secret: String,
    @Value("\${app.security.jwt.expiracao-minutos}") expiracaoMinutos: Long
) {
    private val chave: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray(Charsets.UTF_8))
    private val validade: Duration = Duration.ofMinutes(expiracaoMinutos)

    val expiracaoEmSegundos: Long get() = validade.seconds

    fun gerarToken(usuario: UsuarioAutenticado): String {
        val agora = Instant.now()

        val builder = Jwts.builder()
            .subject(usuario.email)
            .claim(CLAIM_PERFIL, usuario.perfil.name)
            .issuedAt(Date.from(agora))
            .expiration(Date.from(agora.plus(validade)))

        // IDs separados: cada perfil grava apenas o seu próprio ID
        usuario.organizadorId?.let { builder.claim(CLAIM_ORGANIZADOR_ID, it) }
        usuario.participanteId?.let { builder.claim(CLAIM_PARTICIPANTE_ID, it) }

        return builder.signWith(chave).compact()
    }

    /** Valida assinatura e expiração. Lança JwtException se o token for inválido. */
    fun lerToken(token: String): UsuarioAutenticado {
        val claims = Jwts.parser()
            .verifyWith(chave)
            .build()
            .parseSignedClaims(token)
            .payload

        val perfil = Perfil.valueOf(claims.get(CLAIM_PERFIL, String::class.java))

        return UsuarioAutenticado(
            perfil = perfil,
            email = claims.subject,
            organizadorId = (claims[CLAIM_ORGANIZADOR_ID] as? Number)?.toLong(),
            participanteId = (claims[CLAIM_PARTICIPANTE_ID] as? Number)?.toLong()
        )
    }

    companion object {
        const val CLAIM_PERFIL = "perfil"
        const val CLAIM_ORGANIZADOR_ID = "organizadorId"
        const val CLAIM_PARTICIPANTE_ID = "participanteId"
    }
}
