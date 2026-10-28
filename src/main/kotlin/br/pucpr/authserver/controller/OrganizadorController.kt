package br.pucpr.authserver.controller

import br.pucpr.authserver.dto.OrganizadorRequest
import br.pucpr.authserver.dto.OrganizadorResponse
import br.pucpr.authserver.service.OrganizadorService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
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
    fun buscarPorId(@PathVariable id: Long): OrganizadorResponse =
        organizadorService.buscarPorId(id)

    @PutMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @Valid @RequestBody request: OrganizadorRequest
    ): OrganizadorResponse = organizadorService.atualizar(id, request)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun excluir(@PathVariable id: Long) = organizadorService.excluir(id)
}
