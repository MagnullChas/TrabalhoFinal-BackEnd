package br.pucpr.authserver.repository

import br.pucpr.authserver.model.Evento
import jakarta.persistence.LockModeType
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface EventoRepository : JpaRepository<Evento, Long> {

    fun findByData(
        data: LocalDate,
        sort: Sort
    ): List<Evento>

    // Trava a linha do evento até o fim da transação: duas emissões simultâneas
    // não conseguem ultrapassar a capacidade.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Evento e where e.id = :id")
    fun buscarComTrava(@Param("id") id: Long): Evento?
}
