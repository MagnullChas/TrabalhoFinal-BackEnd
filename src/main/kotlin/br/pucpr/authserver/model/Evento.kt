package br.pucpr.authserver.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity
@Table(name = "eventos")
class Evento(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, length = 120)
    var nome: String,

    @Column(nullable = false, length = 1000)
    var descricao: String,

    @Column(nullable = false, length = 200)
    var local: String,

    @Column(nullable = false)
    var data: LocalDate,

    @Column(nullable = false)
    var hora: LocalTime,

    @Column(nullable = false)
    var capacidade: Int

)