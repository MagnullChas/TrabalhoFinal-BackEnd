package br.pucpr.authserver.repository

import br.pucpr.authserver.model.Ticket
import org.springframework.data.jpa.repository.JpaRepository

interface TicketRepository : JpaRepository<Ticket, Long> {

    fun findByEventoId(eventoId: Long): List<Ticket>

    fun findByParticipanteId(participanteId: Long): List<Ticket>

    fun findByEventoIdAndParticipanteId(eventoId: Long, participanteId: Long): List<Ticket>

    fun countByEventoId(eventoId: Long): Long

    fun existsByParticipanteIdAndEventoId(participanteId: Long, eventoId: Long): Boolean
}
