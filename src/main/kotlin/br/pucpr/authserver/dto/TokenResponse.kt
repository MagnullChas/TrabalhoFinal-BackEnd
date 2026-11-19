package br.pucpr.authserver.dto

import br.pucpr.authserver.security.Perfil

/**
 * Resposta do login. Os IDs são separados: só um dos dois vem preenchido,
 * conforme o perfil de quem fez login.
 */
data class TokenResponse(
    val token: String,
    val tipo: String = "Bearer",
    val perfil: Perfil,
    val organizadorId: Long? = null,
    val participanteId: Long? = null,
    val expiraEmSegundos: Long
)
