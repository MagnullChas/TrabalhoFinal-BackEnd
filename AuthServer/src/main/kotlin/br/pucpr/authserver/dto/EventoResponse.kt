package br.pucpr.authserver.dto

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class EventoResponse(
    val id: Long,
    val nome: String,
    val descricao: String,
    val local: String,
    val data: LocalDate,
    val hora: LocalTime,
    val capacidade: Int
)