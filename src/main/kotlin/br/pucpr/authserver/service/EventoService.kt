package br.pucpr.authserver.service

import br.pucpr.authserver.dto.EventoRequest
import br.pucpr.authserver.dto.EventoResponse
import br.pucpr.authserver.exception.AcessoNegadoException
import br.pucpr.authserver.exception.ConflitoException
import br.pucpr.authserver.model.Evento
import br.pucpr.authserver.repository.EventoRepository
import br.pucpr.authserver.repository.OrganizadorRepository
import br.pucpr.authserver.repository.TicketRepository
import br.pucpr.authserver.security.UsuarioAutenticado
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class EventoService(
    private val eventoRepository: EventoRepository,
    private val ticketRepository: TicketRepository,
    private val organizadorRepository: OrganizadorRepository
) {

    private val log = LoggerFactory.getLogger(EventoService::class.java)

    /** O organizador dono do evento é o que está autenticado (vem do token). */
    fun criar(request: EventoRequest, solicitante: UsuarioAutenticado): EventoResponse {
        validarDataHora(request.data, request.hora)

        val organizadorId = solicitante.organizadorId
            ?: throw AcessoNegadoException("Apenas organizadores podem criar eventos.")
        val organizador = organizadorRepository.findById(organizadorId).orElseThrow {
            NoSuchElementException("Organizador com ID $organizadorId não encontrado.")
        }

        val evento = Evento(
            nome = request.nome,
            descricao = request.descricao,
            local = request.local,
            data = request.data,
            hora = request.hora,
            capacidade = request.capacidade,
            organizador = organizador
        )

        val salvo = eventoRepository.save(evento)
        log.info("Evento {} criado pelo organizador {}.", salvo.id, organizadorId)
        return salvo.toResponse()
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

        log.debug("Listagem de eventos: {} resultado(s).", eventos.size)
        return eventos.map { it.toResponse() }
    }

    fun buscarPorId(id: Long): EventoResponse {
        val evento = eventoRepository.findById(id)
            .orElseThrow {
                NoSuchElementException("Evento com ID $id não encontrado.")
            }

        return evento.toResponse()
    }

    /** Só o organizador dono do evento pode alterá-lo. */
    fun atualizar(
        id: Long,
        request: EventoRequest,
        solicitante: UsuarioAutenticado
    ): EventoResponse {

        validarDataHora(request.data, request.hora)

        val evento = eventoRepository.findById(id)
            .orElseThrow {
                NoSuchElementException("Evento com ID $id não encontrado.")
            }
        exigirDono(evento, solicitante)

        val emitidos = ticketRepository.countByEventoId(id)
        if (request.capacidade < emitidos) {
            log.warn("Atualização do evento {} recusada: capacidade {} menor que {} tickets emitidos.", id, request.capacidade, emitidos)
            throw ConflitoException(
                "A capacidade não pode ser menor que a quantidade de tickets já emitidos ($emitidos)."
            )
        }

        evento.nome = request.nome
        evento.descricao = request.descricao
        evento.local = request.local
        evento.data = request.data
        evento.hora = request.hora
        evento.capacidade = request.capacidade

        val salvo = eventoRepository.save(evento)
        log.info("Evento {} atualizado.", id)
        return salvo.toResponse()
    }

    /** Só o organizador dono do evento pode excluí-lo. */
    fun excluir(id: Long, solicitante: UsuarioAutenticado) {
        val evento = eventoRepository.findById(id)
            .orElseThrow {
                NoSuchElementException("Evento com ID $id não encontrado.")
            }
        exigirDono(evento, solicitante)

        eventoRepository.delete(evento)
        log.info("Evento {} excluído.", id)
    }

    private fun exigirDono(evento: Evento, solicitante: UsuarioAutenticado) {
        if (!solicitante.ehOrganizadorDe(evento.organizador.id)) {
            log.warn("Acesso negado: usuário {} tentou alterar o evento {}.", solicitante.email, evento.id)
            throw AcessoNegadoException("Você só pode alterar ou excluir os eventos que você organiza.")
        }
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
        capacidade = capacidade,
        organizadorId = organizador.id
    )
}