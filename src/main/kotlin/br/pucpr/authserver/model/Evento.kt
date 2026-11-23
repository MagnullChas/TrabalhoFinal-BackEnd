package br.pucpr.authserver.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
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
    var capacidade: Int,

    // Organizador responsável pelo evento (um organizador organiza vários eventos).
    // Se o organizador for excluído, seus eventos são excluídos junto (no banco)
    @ManyToOne(optional = false)
    @JoinColumn(name = "organizador_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    val organizador: Organizador

)