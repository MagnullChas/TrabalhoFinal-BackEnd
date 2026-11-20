package br.pucpr.authserver.model

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.LocalDateTime
import java.util.UUID

/**
 * Ticket é a ponte entre Participante e Evento:
 * um participante pode ter vários tickets (um por evento) e um evento tem vários tickets,
 * limitados à capacidade do evento.
 */
@Entity
@Table(
    name = "tickets",
    // Um participante só pode ter um ticket por evento
    uniqueConstraints = [UniqueConstraint(columnNames = ["participante_id", "evento_id"])]
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

    // Se o participante for excluído, seus tickets são excluídos junto (no banco)
    @ManyToOne(optional = false)
    @JoinColumn(name = "participante_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    val participante: Participante,

    // Se o evento for excluído, os tickets dele são excluídos junto (no banco)
    @ManyToOne(optional = false)
    @JoinColumn(name = "evento_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    val evento: Evento
)
