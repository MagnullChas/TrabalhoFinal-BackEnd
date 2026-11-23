package br.pucpr.authserver.service

import br.pucpr.authserver.dto.TicketRequest
import br.pucpr.authserver.dto.TicketResponse
import br.pucpr.authserver.exception.AcessoNegadoException
import br.pucpr.authserver.exception.ConflitoException
import br.pucpr.authserver.model.Participante
import br.pucpr.authserver.model.Ticket
import br.pucpr.authserver.repository.EventoRepository
import br.pucpr.authserver.repository.ParticipanteRepository
import br.pucpr.authserver.repository.TicketRepository
import br.pucpr.authserver.security.UsuarioAutenticado
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TicketService(
    private val ticketRepository: TicketRepository,
    private val participanteRepository: ParticipanteRepository,
    private val eventoRepository: EventoRepository
) {

    private val log = LoggerFactory.getLogger(TicketService::class.java)

    @Transactional
    fun criar(request: TicketRequest, solicitante: UsuarioAutenticado): TicketResponse {
        val participante = participanteRepository.findById(request.participanteId).orElseThrow {
            NoSuchElementException("Participante com ID ${request.participanteId} não encontrado.")
        }
        exigirDonoOuOrganizador(participante, solicitante)

        val evento = eventoRepository.buscarComTrava(request.eventoId)
            ?: throw NoSuchElementException("Evento com ID ${request.eventoId} não encontrado.")

        if (ticketRepository.existsByParticipanteIdAndEventoId(participante.id, evento.id)) {
            log.warn("Ticket recusado: participante {} já possui ticket no evento {}.", participante.id, evento.id)
            throw ConflitoException("O participante ${participante.id} já possui ticket para o evento ${evento.id}.")
        }

        val emitidos = ticketRepository.countByEventoId(evento.id)
        if (emitidos >= evento.capacidade) {
            log.warn("Ticket recusado: evento {} lotado ({} de {}).", evento.id, emitidos, evento.capacidade)
            throw ConflitoException("O evento ${evento.id} já atingiu a capacidade máxima de ${evento.capacidade} tickets.")
        }

        val ticket = Ticket(participante = participante, evento = evento)
        val salvo = ticketRepository.save(ticket)
        log.info("Ticket {} emitido para o participante {} no evento {}.", salvo.id, participante.id, evento.id)
        return salvo.toResponse()
    }

    fun listar(eventoId: Long?, participanteId: Long?): List<TicketResponse> {
        val tickets = when {
            eventoId != null && participanteId != null ->
                ticketRepository.findByEventoIdAndParticipanteId(eventoId, participanteId)
            eventoId != null -> ticketRepository.findByEventoId(eventoId)
            participanteId != null -> ticketRepository.findByParticipanteId(participanteId)
            else -> ticketRepository.findAll()
        }
        return tickets.map { it.toResponse() }
    }

    fun buscarPorId(id: Long, solicitante: UsuarioAutenticado): TicketResponse {
        val ticket = buscarEntidade(id)
        exigirDonoOuOrganizador(ticket.participante, solicitante)
        return ticket.toResponse()
    }

    fun excluir(id: Long, solicitante: UsuarioAutenticado) {
        val ticket = buscarEntidade(id)
        exigirDonoOuOrganizador(ticket.participante, solicitante)
        ticketRepository.delete(ticket)
        log.info("Ticket {} excluído.", id)
    }

    private fun exigirDonoOuOrganizador(participante: Participante, solicitante: UsuarioAutenticado) {
        if (!solicitante.ehOrganizador && !solicitante.ehParticipanteDe(participante.id)) {
            log.warn("Acesso negado: usuário {} tentou acessar ticket do participante {}.", solicitante.email, participante.id)
            throw AcessoNegadoException("Você não tem permissão para acessar este ticket.")
        }
    }

    private fun buscarEntidade(id: Long): Ticket =
        ticketRepository.findById(id).orElseThrow {
            NoSuchElementException("Ticket com ID $id não encontrado.")
        }

    private fun Ticket.toResponse() = TicketResponse(
        id = id,
        codigo = codigo,
        emitidoEm = emitidoEm,
        participanteId = participante.id,
        eventoId = evento.id
    )
}
