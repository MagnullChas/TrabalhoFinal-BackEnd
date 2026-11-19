package br.pucpr.authserver.model

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "tickets",
    // Cada inscrição (participante) possui no máximo um ticket
    uniqueConstraints = [UniqueConstraint(columnNames = ["participante_id"])]
)
class Ticket(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    // Código único apresentado na entrada do evento
    @Column(nullable = false, unique = true, updatable = false, length = 36)
    val codigo: String = UUID.randomUUID().toString(),

    @Column(nullable = false, updatable = false)
    val emitidoEm: LocalDateTime = LocalDateTime.now(),

    // Se o participante (ou o evento dele) for excluído, o ticket é excluído junto (no banco)
    @OneToOne(optional = false)
    @JoinColumn(name = "participante_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    val participante: Participante
)
