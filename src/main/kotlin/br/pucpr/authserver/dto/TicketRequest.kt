package br.pucpr.authserver.dto

import jakarta.validation.constraints.Positive

data class TicketRequest(

    @field:Positive(message = "O ID do participante deve ser maior que zero")
    val participanteId: Long
)
