package br.pucpr.authserver.service

import br.pucpr.authserver.dto.EventoRequest
import br.pucpr.authserver.dto.EventoResponse
import br.pucpr.authserver.model.Evento
import br.pucpr.authserver.repository.EventoRepository
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class EventoService(
    private val eventoRepository: EventoRepository
) {

    fun criar(request: EventoRequest): EventoResponse {
        validarDataHora(request.data, request.hora)

        val evento = Evento(
            nome = request.nome,
            descricao = request.descricao,
            local = request.local,
            data = request.data,
            hora = request.hora,
            capacidade = request.capacidade
        )

        return eventoRepository.save(evento).toResponse()
    }

    fun listar(
        data: LocalDate?,
        ordenarPor: String,
        direcao: String
    ): List<EventoResponse> {

        val camposPermitidos = setOf("nome", "data", "hora", "local")

        require(ordenarPor in camposPermitidos) {
            "Campo de ordenação inválido. Utilize nome, data, hora ou local."
        }

        val direcaoOrdenacao = when (direcao.lowercase()) {
            "asc" -> Sort.Direction.ASC
            "desc" -> Sort.Direction.DESC
            else -> throw IllegalArgumentException(
                "Direção inválida. Utilize asc ou desc."
            )
        }

        val ordenacao = Sort.by(direcaoOrdenacao, ordenarPor)

        val eventos = if (data != null) {
            eventoRepository.findByData(data, ordenacao)
        } else {
            eventoRepository.findAll(ordenacao)
        }

        return eventos.map { it.toResponse() }
    }

    fun buscarPorId(id: Long): EventoResponse {
        val evento = eventoRepository.findById(id)
            .orElseThrow {
                NoSuchElementException("Evento com ID $id não encontrado.")
            }

        return evento.toResponse()
    }

    fun atualizar(
        id: Long,
        request: EventoRequest
    ): EventoResponse {

        validarDataHora(request.data, request.hora)

        val evento = eventoRepository.findById(id)
            .orElseThrow {
                NoSuchElementException("Evento com ID $id não encontrado.")
            }

        evento.nome = request.nome
        evento.descricao = request.descricao
        evento.local = request.local
        evento.data = request.data
        evento.hora = request.hora
        evento.capacidade = request.capacidade

        return eventoRepository.save(evento).toResponse()
    }

    fun excluir(id: Long) {
        val evento = eventoRepository.findById(id)
            .orElseThrow {
                NoSuchElementException("Evento com ID $id não encontrado.")
            }

        eventoRepository.delete(evento)
    }

    private fun validarDataHora(
        data: LocalDate,
        hora: java.time.LocalTime
    ) {
        val dataHora = LocalDateTime.of(data, hora)

        require(dataHora.isAfter(LocalDateTime.now())) {
            "A data e o horário do evento devem estar no futuro."
        }
    }

    private fun Evento.toResponse() = EventoResponse(
        id = id,
        nome = nome,
        descricao = descricao,
        local = local,
        data = data,
        hora = hora,
        capacidade = capacidade
    )
}