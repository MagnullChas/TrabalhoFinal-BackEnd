package br.pucpr.authserver.repository

import br.pucpr.authserver.model.Participante
import org.springframework.data.jpa.repository.JpaRepository

interface ParticipanteRepository : JpaRepository<Participante, Long> {

    fun findByEventoId(eventoId: Long): List<Participante>

    fun countByEventoId(eventoId: Long): Long

    fun existsByEmailAndEventoId(email: String, eventoId: Long): Boolean

    fun existsByEmailAndEventoIdAndIdNot(email: String, eventoId: Long, id: Long): Boolean
}
