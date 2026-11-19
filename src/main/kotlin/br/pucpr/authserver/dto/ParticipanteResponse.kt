package br.pucpr.authserver.dto

data class ParticipanteResponse(
    val id: Long,
    val nome: String,
    val email: String,
    val eventoId: Long
)
