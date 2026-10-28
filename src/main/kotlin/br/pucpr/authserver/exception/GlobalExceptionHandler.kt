package br.pucpr.authserver.exception

import br.pucpr.authserver.dto.ErroResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException::class)
    fun tratarRecursoNaoEncontrado(
        ex: NoSuchElementException
    ): ResponseEntity<ErroResponse> {
        val resposta = ErroResponse(
            status = HttpStatus.NOT_FOUND.value(),
            erro = "Not Found",
            mensagem = ex.message ?: "Recurso não encontrado."
        )

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resposta)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun tratarArgumentoInvalido(
        ex: IllegalArgumentException
    ): ResponseEntity<ErroResponse> {
        val resposta = ErroResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            erro = "Bad Request",
            mensagem = ex.message ?: "Parâmetros inválidos."
        )

        return ResponseEntity.badRequest().body(resposta)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun tratarErroDeValidacao(
        ex: MethodArgumentNotValidException
    ): ResponseEntity<ErroResponse> {
        val mensagem = ex.bindingResult.fieldErrors
            .joinToString("; ") { erro ->
                "${erro.field}: ${erro.defaultMessage ?: "valor inválido"}"
            }

        val resposta = ErroResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            erro = "Bad Request",
            mensagem = mensagem
        )

        return ResponseEntity.badRequest().body(resposta)
    }

    @ExceptionHandler(NaoAutorizadoException::class)
    fun tratarNaoAutorizado(
        ex: NaoAutorizadoException
    ): ResponseEntity<ErroResponse> {
        val resposta = ErroResponse(
            status = HttpStatus.UNAUTHORIZED.value(),
            erro = "Unauthorized",
            mensagem = ex.message ?: "Não autorizado."
        )

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resposta)
    }

    @ExceptionHandler(ConflitoException::class)
    fun tratarConflito(
        ex: ConflitoException
    ): ResponseEntity<ErroResponse> {
        val resposta = ErroResponse(
            status = HttpStatus.CONFLICT.value(),
            erro = "Conflict",
            mensagem = ex.message ?: "Conflito de dados."
        )

        return ResponseEntity.status(HttpStatus.CONFLICT).body(resposta)
    }
}
