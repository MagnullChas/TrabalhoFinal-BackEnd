package br.pucpr.authserver.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class ParticipanteRequest(

    @field:NotBlank(message = "O nome do participante é obrigatório")
    @field:Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
    val nome: String,

    @field:NotBlank(message = "O e-mail é obrigatório")
    @field:Email(message = "E-mail inválido")
    @field:Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres")
    val email: String,

    @field:Positive(message = "O ID do evento deve ser maior que zero")
    val eventoId: Long
)
