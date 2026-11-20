package br.pucpr.authserver.controller

import br.pucpr.authserver.dto.ParticipanteRequest
import br.pucpr.authserver.dto.ParticipanteResponse
import br.pucpr.authserver.service.ParticipanteService
import br.pucpr.authserver.security.UsuarioAutenticado
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/participantes")
class ParticipanteController(
    private val participanteService: ParticipanteService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun criar(@Valid @RequestBody request: ParticipanteRequest): ParticipanteResponse =
        participanteService.criar(request)

    @GetMapping
    fun listar(@RequestParam(required = false) eventoId: Long?): List<ParticipanteResponse> =
        participanteService.listar(eventoId)

    @GetMapping("/{id}")
    fun buscarPorId(
        @PathVariable id: Long,
        @AuthenticationPrincipal usuario: UsuarioAutenticado
    ): ParticipanteResponse = participanteService.buscarPorId(id, usuario)

    @PutMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @Valid @RequestBody request: ParticipanteRequest,
        @AuthenticationPrincipal usuario: UsuarioAutenticado
    ): ParticipanteResponse = participanteService.atualizar(id, request, usuario)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun excluir(
        @PathVariable id: Long,
        @AuthenticationPrincipal usuario: UsuarioAutenticado
    ) = participanteService.excluir(id, usuario)
}
