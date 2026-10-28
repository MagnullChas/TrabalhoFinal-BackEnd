package br.pucpr.authserver.dto

import jakarta.validation.constraints.Future
import jakarta.validation.constraints.FutureOrPresent
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class EventoRequest(

    @field:NotBlank(message = "O nome do evento é obrigatório")
    @field:Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
    val nome: String,

    @field:NotBlank(message = "A descrição do evento é obrigatória")
    @field:Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
    val descricao: String,

    @field:NotBlank(message = "O local do evento é obrigatório")
    @field:Size(max = 200, message = "O local deve ter no máximo 200 caracteres")
    val local: String,

    @field:FutureOrPresent(message = "A data do evento não pode estar no passado")
    val data: LocalDate,

    val hora: LocalTime,

    @field:Positive(message = "A capacidade deve ser maior que zero")
    val capacidade: Int
)