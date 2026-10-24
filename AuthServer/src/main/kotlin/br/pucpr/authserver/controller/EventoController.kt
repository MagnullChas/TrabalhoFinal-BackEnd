package br.pucpr.authserver.controller

import br.pucpr.authserver.dto.EventoRequest
import br.pucpr.authserver.dto.EventoResponse
import br.pucpr.authserver.service.EventoService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.format.DateTimeParseException

@RestController
@RequestMapping("/eventos")
class EventoController(
    private val eventoService: EventoService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun criar(
        @Valid @RequestBody request: EventoRequest
    ): EventoResponse {
        return eventoService.criar(request)
    }

    @GetMapping
    fun listar(
        @RequestParam(required = false) data: String?,
        @RequestParam(defaultValue = "data") ordenar_por: String,
        @RequestParam(defaultValue = "asc") direcao: String
    ): List<EventoResponse> {

        val dataConvertida = data?.let {
            try {
                LocalDate.parse(it)
            } catch (ex: DateTimeParseException) {
                throw IllegalArgumentException(
                    "Data inválida. Utilize o formato AAAA-MM-DD."
                )
            }
        }

        return eventoService.listar(
            data = dataConvertida,
            ordenarPor = ordenar_por,
            direcao = direcao
        )
    }

    @GetMapping("/{id}")
    fun buscarPorId(
        @PathVariable id: Long
    ): EventoResponse {
        return eventoService.buscarPorId(id)
    }

    @PutMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @Valid @RequestBody request: EventoRequest
    ): EventoResponse {
        return eventoService.atualizar(id, request)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun excluir(
        @PathVariable id: Long
    ) {
        eventoService.excluir(id)
    }
}