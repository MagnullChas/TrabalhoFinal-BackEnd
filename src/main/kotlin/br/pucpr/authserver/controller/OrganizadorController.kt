package br.pucpr.authserver.controller

import br.pucpr.authserver.dto.OrganizadorRequest
import br.pucpr.authserver.dto.OrganizadorResponse
import br.pucpr.authserver.service.OrganizadorService
import br.pucpr.authserver.exception.AcessoNegadoException
import br.pucpr.authserver.security.UsuarioAutenticado
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/organizadores")
class OrganizadorController(
    private val organizadorService: OrganizadorService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun criar(@Valid @RequestBody request: OrganizadorRequest): OrganizadorResponse =
        organizadorService.criar(request)

    @GetMapping
    fun listar(): List<OrganizadorResponse> = organizadorService.listar()

    @GetMapping("/{id}")
    fun buscarPorId(
        @PathVariable id: Long,
        @AuthenticationPrincipal usuario: UsuarioAutenticado
    ): OrganizadorResponse {
        exigirProprioOrganizador(id, usuario)
        return organizadorService.buscarPorId(id)
    }

    @PutMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @Valid @RequestBody request: OrganizadorRequest,
        @AuthenticationPrincipal usuario: UsuarioAutenticado
    ): OrganizadorResponse {
        exigirProprioOrganizador(id, usuario)
        return organizadorService.atualizar(id, request)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun excluir(
        @PathVariable id: Long,
        @AuthenticationPrincipal usuario: UsuarioAutenticado
    ) {
        exigirProprioOrganizador(id, usuario)
        organizadorService.excluir(id)
    }

    // Um organizador só acessa o próprio cadastro (compara o organizadorId do token com o ID da URL)
    private fun exigirProprioOrganizador(id: Long, usuario: UsuarioAutenticado) {
        if (!usuario.ehOrganizadorDe(id)) {
            throw AcessoNegadoException("Você só pode acessar o seu próprio cadastro de organizador.")
        }
    }
}
