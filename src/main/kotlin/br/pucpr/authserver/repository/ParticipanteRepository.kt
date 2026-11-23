package br.pucpr.authserver.repository

import br.pucpr.authserver.model.Participante
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ParticipanteRepository : JpaRepository<Participante, Long> {

    fun findByEmail(email: String): Participante?

    fun existsByEmail(email: String): Boolean

    fun existsByEmailAndIdNot(email: String, id: Long): Boolean

    // Participantes que possuem ticket no evento
    @Query("select t.participante from Ticket t where t.evento.id = :eventoId")
    fun buscarPorEvento(@Param("eventoId") eventoId: Long): List<Participante>
}
