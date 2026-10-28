package br.pucpr.authserver.model

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

@Entity
@Table(
    name = "participantes",
    uniqueConstraints = [UniqueConstraint(columnNames = ["email", "evento_id"])]
)
class Participante(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, length = 120)
    var nome: String,

    @Column(nullable = false, length = 150)
    var email: String,

    // Se o evento for excluído, os participantes dele são excluídos junto (no banco)
    @ManyToOne(optional = false)
    @JoinColumn(name = "evento_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var evento: Evento
)
