package br.pucpr.authserver.controller

import br.pucpr.authserver.dto.ParticipanteRequest
import br.pucpr.authserver.dto.ParticipanteResponse
import br.pucpr.authserver.service.ParticipanteService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
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
    fun buscarPorId(@PathVariable id: Long): ParticipanteResponse =
        participanteService.buscarPorId(id)

    @PutMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @Valid @RequestBody request: ParticipanteRequest
    ): ParticipanteResponse = participanteService.atualizar(id, request)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun excluir(@PathVariable id: Long) = participanteService.excluir(id)
}
