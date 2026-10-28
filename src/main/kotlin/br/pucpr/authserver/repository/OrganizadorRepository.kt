package br.pucpr.authserver.repository

import br.pucpr.authserver.model.Organizador
import org.springframework.data.jpa.repository.JpaRepository

interface OrganizadorRepository : JpaRepository<Organizador, Long> {

    fun existsByEmail(email: String): Boolean

    fun existsByEmailAndIdNot(email: String, id: Long): Boolean
}
