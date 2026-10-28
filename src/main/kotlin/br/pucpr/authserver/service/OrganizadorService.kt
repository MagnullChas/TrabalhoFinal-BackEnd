package br.pucpr.authserver.service

import br.pucpr.authserver.dto.OrganizadorRequest
import br.pucpr.authserver.dto.OrganizadorResponse
import br.pucpr.authserver.exception.ConflitoException
import br.pucpr.authserver.model.Organizador
import br.pucpr.authserver.repository.OrganizadorRepository
import org.springframework.stereotype.Service

@Service
class OrganizadorService(
    private val organizadorRepository: OrganizadorRepository
) {

    fun criar(request: OrganizadorRequest): OrganizadorResponse {
        val email = request.email.trim().lowercase()

        if (organizadorRepository.existsByEmail(email)) {
            throw ConflitoException("Já existe um organizador com o e-mail $email.")
        }

        val organizador = Organizador(
            nome = request.nome,
            email = email,
            telefone = request.telefone
        )
        return organizadorRepository.save(organizador).toResponse()
    }

    fun listar(): List<OrganizadorResponse> =
        organizadorRepository.findAll().map { it.toResponse() }

    fun buscarPorId(id: Long): OrganizadorResponse = buscarEntidade(id).toResponse()

    fun atualizar(id: Long, request: OrganizadorRequest): OrganizadorResponse {
        val organizador = buscarEntidade(id)
        val email = request.email.trim().lowercase()

        if (organizadorRepository.existsByEmailAndIdNot(email, id)) {
            throw ConflitoException("Já existe um organizador com o e-mail $email.")
        }

        organizador.nome = request.nome
        organizador.email = email
        organizador.telefone = request.telefone

        return organizadorRepository.save(organizador).toResponse()
    }

    fun excluir(id: Long) {
        organizadorRepository.delete(buscarEntidade(id))
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
