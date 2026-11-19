package br.pucpr.authserver.service

import br.pucpr.authserver.dto.ParticipanteRequest
import br.pucpr.authserver.dto.ParticipanteResponse
import br.pucpr.authserver.exception.ConflitoException
import br.pucpr.authserver.model.Evento
import br.pucpr.authserver.model.Participante
import br.pucpr.authserver.repository.EventoRepository
import br.pucpr.authserver.repository.ParticipanteRepository
import br.pucpr.authserver.exception.AcessoNegadoException
import br.pucpr.authserver.security.UsuarioAutenticado
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class ParticipanteService(
    private val participanteRepository: ParticipanteRepository,
    private val eventoRepository: EventoRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun criar(request: ParticipanteRequest): ParticipanteResponse {
        val evento = buscarEvento(request.eventoId)
        val email = request.email.trim().lowercase()

        if (participanteRepository.existsByEmailAndEventoId(email, evento.id)) {
            throw ConflitoException("Este e-mail já está inscrito no evento ${evento.id}.")
        }
        validarCapacidade(evento)

        val senha = requireNotNull(request.senha) { "A senha é obrigatória." }

        val participante = Participante(
            nome = request.nome,
            email = email,
            senha = passwordEncoder.encode(senha)!!,
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

    /** Organizador vê qualquer participante; participante só vê o próprio cadastro. */
    fun buscarPorId(id: Long, solicitante: UsuarioAutenticado): ParticipanteResponse {
        val participante = buscarEntidade(id)
        exigirDonoOuOrganizador(participante, solicitante)
        return participante.toResponse()
    }

    /** Só o próprio participante altera o seu cadastro. */
    fun atualizar(id: Long, request: ParticipanteRequest, solicitante: UsuarioAutenticado): ParticipanteResponse {
        val participante = buscarEntidade(id)
        exigirDono(participante, solicitante)
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
        request.senha?.let { participante.senha = passwordEncoder.encode(it)!! }

        return participanteRepository.save(participante).toResponse()
    }

    /** O próprio participante cancela a inscrição, ou um organizador remove. */
    fun excluir(id: Long, solicitante: UsuarioAutenticado) {
        val participante = buscarEntidade(id)
        exigirDonoOuOrganizador(participante, solicitante)
        participanteRepository.delete(participante)
    }

    // O participante é "dono" do cadastro quando o e-mail do token é o e-mail do cadastro
    // (a mesma pessoa pode ter inscrições em vários eventos, cada uma com seu ID de participante).
    private fun ehDono(participante: Participante, solicitante: UsuarioAutenticado) =
        solicitante.ehParticipante && solicitante.email == participante.email

    private fun exigirDono(participante: Participante, solicitante: UsuarioAutenticado) {
        if (!ehDono(participante, solicitante)) {
            throw AcessoNegadoException("Você só pode alterar o seu próprio cadastro de participante.")
        }
    }

    private fun exigirDonoOuOrganizador(participante: Participante, solicitante: UsuarioAutenticado) {
        if (!solicitante.ehOrganizador && !ehDono(participante, solicitante)) {
            throw AcessoNegadoException("Você não tem permissão para acessar este participante.")
        }
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
