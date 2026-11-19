package br.pucpr.authserver.repository

import br.pucpr.authserver.model.Ticket
import org.springframework.data.jpa.repository.JpaRepository

interface TicketRepository : JpaRepository<Ticket, Long> {

    fun findByParticipanteEventoId(eventoId: Long): List<Ticket>

    fun findByParticipanteId(participanteId: Long): List<Ticket>

    fun findByParticipanteEventoIdAndParticipanteId(eventoId: Long, participanteId: Long): List<Ticket>

    fun existsByParticipanteId(participanteId: Long): Boolean
}
