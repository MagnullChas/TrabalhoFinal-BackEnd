package br.pucpr.authserver.repository

import br.pucpr.authserver.model.Evento
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface EventoRepository : JpaRepository<Evento, Long> {

    fun findByData(
        data: LocalDate,
        sort: Sort
    ): List<Evento>
}