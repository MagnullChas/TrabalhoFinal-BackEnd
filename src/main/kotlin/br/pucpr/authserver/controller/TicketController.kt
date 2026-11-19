package br.pucpr.authserver.controller

import br.pucpr.authserver.dto.TicketRequest
import br.pucpr.authserver.dto.TicketResponse
import br.pucpr.authserver.security.UsuarioAutenticado
import br.pucpr.authserver.service.TicketService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/tickets")
class TicketController(
    private val ticketService: TicketService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun criar(
        @Valid @RequestBody request: TicketRequest,
        @AuthenticationPrincipal usuario: UsuarioAutenticado
    ): TicketResponse = ticketService.criar(request, usuario)

    @GetMapping
    fun listar(
        @RequestParam(required = false) eventoId: Long?,
        @RequestParam(required = false) participanteId: Long?
    ): List<TicketResponse> = ticketService.listar(eventoId, participanteId)

    @GetMapping("/{id}")
    fun buscarPorId(
        @PathVariable id: Long,
        @AuthenticationPrincipal usuario: UsuarioAutenticado
    ): TicketResponse = ticketService.buscarPorId(id, usuario)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun excluir(
        @PathVariable id: Long,
        @AuthenticationPrincipal usuario: UsuarioAutenticado
    ) = ticketService.excluir(id, usuario)
}
