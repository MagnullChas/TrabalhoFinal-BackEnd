package br.pucpr.authserver.service

import br.pucpr.authserver.dto.OrganizadorRequest
import br.pucpr.authserver.dto.OrganizadorResponse
import br.pucpr.authserver.exception.ConflitoException
import br.pucpr.authserver.model.Organizador
import br.pucpr.authserver.repository.OrganizadorRepository
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class OrganizadorService(
    private val organizadorRepository: OrganizadorRepository,
    private val passwordEncoder: PasswordEncoder
) {

    private val log = LoggerFactory.getLogger(OrganizadorService::class.java)

    fun criar(request: OrganizadorRequest): OrganizadorResponse {
        val email = request.email.trim().lowercase()

        if (organizadorRepository.existsByEmail(email)) {
            log.warn("Cadastro de organizador recusado: e-mail {} já existe.", email)
            throw ConflitoException("Já existe um organizador com o e-mail $email.")
        }

        val senha = requireNotNull(request.senha) { "A senha é obrigatória." }

        val organizador = Organizador(
            nome = request.nome,
            email = email,
            senha = passwordEncoder.encode(senha)!!,
            telefone = request.telefone
        )
        val salvo = organizadorRepository.save(organizador)
        log.info("Organizador {} criado.", salvo.id)
        return salvo.toResponse()
    }

    fun listar(): List<OrganizadorResponse> =
        organizadorRepository.findAll().map { it.toResponse() }

    fun buscarPorId(id: Long): OrganizadorResponse = buscarEntidade(id).toResponse()

    fun atualizar(id: Long, request: OrganizadorRequest): OrganizadorResponse {
        val organizador = buscarEntidade(id)
        val email = request.email.trim().lowercase()

        if (organizadorRepository.existsByEmailAndIdNot(email, id)) {
            log.warn("Atualização do organizador {} recusada: e-mail {} já existe.", id, email)
            throw ConflitoException("Já existe um organizador com o e-mail $email.")
        }

        organizador.nome = request.nome
        organizador.email = email
        organizador.telefone = request.telefone
        request.senha?.let { organizador.senha = passwordEncoder.encode(it)!! }

        val salvo = organizadorRepository.save(organizador)
        log.info("Organizador {} atualizado.", id)
        return salvo.toResponse()
    }

    fun excluir(id: Long) {
        organizadorRepository.delete(buscarEntidade(id))
        log.info("Organizador {} excluído.", id)
    }

    private fun buscarEntidade(id: Long): Organizador =
        organizadorRepository.findById(id).orElseThrow {
            NoSuchElementException("Organizador com ID $id não encontrado.")
        }

    private fun Organizador.toResponse() = OrganizadorResponse(
        id = id,
        nome = nome,
        email = email,
        telefone = telefone
    )
}
