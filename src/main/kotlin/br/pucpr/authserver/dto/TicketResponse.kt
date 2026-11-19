package br.pucpr.authserver.dto

import java.time.LocalDateTime

data class TicketResponse(
    val id: Long,
    val codigo: String,
    val emitidoEm: LocalDateTime,
    val participanteId: Long,
    val eventoId: Long
)
