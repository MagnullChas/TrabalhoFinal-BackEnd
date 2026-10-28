package br.pucpr.authserver.service

import br.pucpr.authserver.dto.ParticipanteRequest
import br.pucpr.authserver.dto.ParticipanteResponse
import br.pucpr.authserver.exception.ConflitoException
import br.pucpr.authserver.model.Evento
import br.pucpr.authserver.model.Participante
import br.pucpr.authserver.repository.EventoRepository
import br.pucpr.authserver.repository.ParticipanteRepository
import org.springframework.stereotype.Service

@Service
class ParticipanteService(
    private val participanteRepository: ParticipanteRepository,
    private val eventoRepository: EventoRepository
) {

    fun criar(request: ParticipanteRequest): ParticipanteResponse {
        val evento = buscarEvento(request.eventoId)
        val email = request.email.trim().lowercase()

        if (participanteRepository.existsByEmailAndEventoId(email, evento.id)) {
            throw ConflitoException("Este e-mail já está inscrito no evento ${evento.id}.")
        }
        validarCapacidade(evento)

        val participante = Participante(
            nome = request.nome,
            email = email,
            evento = evento
        )
        return participanteRepository.save(participante).toResponse()
    }

    fun listar(eventoId: Long?): List<ParticipanteResponse> {
        val participantes = if (eventoId != null) {
            participanteRepository.findByEventoId(eventoId)
        } else {
            participanteRepository.findAll()
        }
        return participantes.map { it.toResponse() }
    }

    fun buscarPorId(id: Long): ParticipanteResponse = buscarEntidade(id).toResponse()

    fun atualizar(id: Long, request: ParticipanteRequest): ParticipanteResponse {
        val participante = buscarEntidade(id)
        val evento = buscarEvento(request.eventoId)
        val email = request.email.trim().lowercase()

        if (participanteRepository.existsByEmailAndEventoIdAndIdNot(email, evento.id, id)) {
            throw ConflitoException("Este e-mail já está inscrito no evento ${evento.id}.")
        }
        // só confere a capacidade se o participante está mudando de evento
        if (evento.id != participante.evento.id) {
            validarCapacidade(evento)
        }

        participante.nome = request.nome
        participante.email = email
        participante.evento = evento

        return participanteRepository.save(participante).toResponse()
    }

    fun excluir(id: Long) {
        participanteRepository.delete(buscarEntidade(id))
    }

    private fun buscarEntidade(id: Long): Participante =
        participanteRepository.findById(id).orElseThrow {
            NoSuchElementException("Participante com ID $id não encontrado.")
        }

    private fun buscarEvento(id: Long): Evento =
        eventoRepository.findById(id).orElseThrow {
            NoSuchElementException("Evento com ID $id não encontrado.")
        }

    private fun validarCapacidade(evento: Evento) {
        val inscritos = participanteRepository.countByEventoId(evento.id)
        if (inscritos >= evento.capacidade) {
            throw ConflitoException("O evento ${evento.id} já atingiu a capacidade máxima.")
        }
    }

    private fun Participante.toResponse() = ParticipanteResponse(
        id = id,
        nome = nome,
        email = email,
        eventoId = evento.id
    )
}
