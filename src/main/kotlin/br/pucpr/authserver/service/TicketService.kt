package br.pucpr.authserver.service

import br.pucpr.authserver.dto.TicketRequest
import br.pucpr.authserver.dto.TicketResponse
import br.pucpr.authserver.exception.AcessoNegadoException
import br.pucpr.authserver.exception.ConflitoException
import br.pucpr.authserver.model.Participante
import br.pucpr.authserver.model.Ticket
import br.pucpr.authserver.repository.ParticipanteRepository
import br.pucpr.authserver.repository.TicketRepository
import br.pucpr.authserver.security.UsuarioAutenticado
import org.springframework.stereotype.Service

@Service
class TicketService(
    private val ticketRepository: TicketRepository,
    private val participanteRepository: ParticipanteRepository
) {

    /** O dono da inscrição emite o próprio ticket; um organizador pode emitir para qualquer inscrição. */
    fun criar(request: TicketRequest, solicitante: UsuarioAutenticado): TicketResponse {
        val participante = buscarParticipante(request.participanteId)
        exigirDonoOuOrganizador(participante, solicitante)

        if (ticketRepository.existsByParticipanteId(participante.id)) {
            throw ConflitoException("A inscrição ${participante.id} já possui um ticket.")
        }

        return ticketRepository.save(Ticket(participante = participante)).toResponse()
    }

    /** Apenas organizador (garantido no SecurityConfig). Filtros opcionais por evento e participante. */
    fun listar(eventoId: Long?, participanteId: Long?): List<TicketResponse> {
        val tickets = when {
            eventoId != null && participanteId != null ->
                ticketRepository.findByParticipanteEventoIdAndParticipanteId(eventoId, participanteId)
            eventoId != null -> ticketRepository.findByParticipanteEventoId(eventoId)
            participanteId != null -> ticketRepository.findByParticipanteId(participanteId)
            else -> ticketRepository.findAll()
        }
        return tickets.map { it.toResponse() }
    }

    /** Organizador vê qualquer ticket; participante só vê o próprio. */
    fun buscarPorId(id: Long, solicitante: UsuarioAutenticado): TicketResponse {
        val ticket = buscarEntidade(id)
        exigirDonoOuOrganizador(ticket.participante, solicitante)
        return ticket.toResponse()
    }

    /** O dono cancela o próprio ticket, ou um organizador remove. */
    fun excluir(id: Long, solicitante: UsuarioAutenticado) {
        val ticket = buscarEntidade(id)
        exigirDonoOuOrganizador(ticket.participante, solicitante)
        ticketRepository.delete(ticket)
    }

    // Mesma regra de "dono" usada em ParticipanteService: e-mail do token == e-mail do cadastro
    private fun ehDono(participante: Participante, solicitante: UsuarioAutenticado) =
        solicitante.ehParticipante && solicitante.email == participante.email

    private fun exigirDonoOuOrganizador(participante: Participante, solicitante: UsuarioAutenticado) {
        if (!solicitante.ehOrganizador && !ehDono(participante, solicitante)) {
            throw AcessoNegadoException("Você não tem permissão para acessar este ticket.")
        }
    }

    private fun buscarEntidade(id: Long): Ticket =
        ticketRepository.findById(id).orElseThrow {
            NoSuchElementException("Ticket com ID $id não encontrado.")
        }

    private fun buscarParticipante(id: Long): Participante =
        participanteRepository.findById(id).orElseThrow {
            NoSuchElementException("Participante com ID $id não encontrado.")
        }

    private fun Ticket.toResponse() = TicketResponse(
        id = id,
        codigo = codigo,
        emitidoEm = emitidoEm,
        participanteId = participante.id,
        eventoId = participante.evento.id
    )
}
