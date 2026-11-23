package br.pucpr.authserver.service

import br.pucpr.authserver.dto.ParticipanteRequest
import br.pucpr.authserver.dto.ParticipanteResponse
import br.pucpr.authserver.exception.AcessoNegadoException
import br.pucpr.authserver.exception.ConflitoException
import br.pucpr.authserver.model.Participante
import br.pucpr.authserver.repository.ParticipanteRepository
import br.pucpr.authserver.security.UsuarioAutenticado
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class ParticipanteService(
    private val participanteRepository: ParticipanteRepository,
    private val passwordEncoder: PasswordEncoder
) {

    private val log = LoggerFactory.getLogger(ParticipanteService::class.java)

    fun criar(request: ParticipanteRequest): ParticipanteResponse {
        val email = request.email.trim().lowercase()

        if (participanteRepository.existsByEmail(email)) {
            log.warn("Cadastro de participante recusado: e-mail {} já existe.", email)
            throw ConflitoException("Já existe um participante cadastrado com o e-mail $email.")
        }

        val senha = requireNotNull(request.senha) { "A senha é obrigatória." }

        val participante = Participante(
            nome = request.nome,
            email = email,
            senha = passwordEncoder.encode(senha)!!
        )
        val salvo = participanteRepository.save(participante)
        log.info("Participante {} criado.", salvo.id)
        return salvo.toResponse()
    }

    /** Sem filtro lista todos; com eventoId lista só quem possui ticket no evento. */
    fun listar(eventoId: Long?): List<ParticipanteResponse> {
        val participantes = if (eventoId != null) {
            participanteRepository.buscarPorEvento(eventoId)
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
        val email = request.email.trim().lowercase()

        if (participanteRepository.existsByEmailAndIdNot(email, id)) {
            log.warn("Atualização do participante {} recusada: e-mail {} já existe.", id, email)
            throw ConflitoException("Já existe um participante cadastrado com o e-mail $email.")
        }

        participante.nome = request.nome
        participante.email = email
        request.senha?.let { participante.senha = passwordEncoder.encode(it)!! }

        val salvo = participanteRepository.save(participante)
        log.info("Participante {} atualizado.", id)
        return salvo.toResponse()
    }

    /** O próprio participante exclui o cadastro (e seus tickets), ou um organizador remove. */
    fun excluir(id: Long, solicitante: UsuarioAutenticado) {
        val participante = buscarEntidade(id)
        exigirDonoOuOrganizador(participante, solicitante)
        participanteRepository.delete(participante)
        log.info("Participante {} excluído.", id)
    }

    private fun exigirDono(participante: Participante, solicitante: UsuarioAutenticado) {
        if (!solicitante.ehParticipanteDe(participante.id)) {
            log.warn("Acesso negado: usuário {} tentou alterar o participante {}.", solicitante.email, participante.id)
            throw AcessoNegadoException("Você só pode alterar o seu próprio cadastro de participante.")
        }
    }

    private fun exigirDonoOuOrganizador(participante: Participante, solicitante: UsuarioAutenticado) {
        if (!solicitante.ehOrganizador && !solicitante.ehParticipanteDe(participante.id)) {
            log.warn("Acesso negado: usuário {} tentou acessar o participante {}.", solicitante.email, participante.id)
            throw AcessoNegadoException("Você não tem permissão para acessar este participante.")
        }
    }

    private fun buscarEntidade(id: Long): Participante =
        participanteRepository.findById(id).orElseThrow {
            NoSuchElementException("Participante com ID $id não encontrado.")
        }

    private fun Participante.toResponse() = ParticipanteResponse(
        id = id,
        nome = nome,
        email = email
    )
}
