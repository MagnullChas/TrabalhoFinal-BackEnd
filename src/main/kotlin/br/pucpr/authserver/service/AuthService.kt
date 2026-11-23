package br.pucpr.authserver.service

import br.pucpr.authserver.dto.LoginRequest
import br.pucpr.authserver.dto.TokenResponse
import br.pucpr.authserver.exception.NaoAutorizadoException
import br.pucpr.authserver.repository.OrganizadorRepository
import br.pucpr.authserver.repository.ParticipanteRepository
import br.pucpr.authserver.security.JwtService
import br.pucpr.authserver.security.Perfil
import br.pucpr.authserver.security.UsuarioAutenticado
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val organizadorRepository: OrganizadorRepository,
    private val participanteRepository: ParticipanteRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {

    private val log = LoggerFactory.getLogger(AuthService::class.java)

    fun loginOrganizador(request: LoginRequest): TokenResponse {
        val email = request.email.trim().lowercase()

        val organizador = organizadorRepository.findByEmail(email)
            ?.takeIf { passwordEncoder.matches(request.senha, it.senha) }
            ?: run {
                log.warn("Falha de login de organizador para o e-mail {}.", email)
                throw NaoAutorizadoException(CREDENCIAIS_INVALIDAS)
            }

        log.info("Login de organizador realizado: id {}.", organizador.id)
        val usuario = UsuarioAutenticado(
            perfil = Perfil.ORGANIZADOR,
            email = organizador.email,
            organizadorId = organizador.id
        )
        return TokenResponse(
            token = jwtService.gerarToken(usuario),
            perfil = Perfil.ORGANIZADOR,
            organizadorId = organizador.id,
            expiraEmSegundos = jwtService.expiracaoEmSegundos
        )
    }

    fun loginParticipante(request: LoginRequest): TokenResponse {
        val email = request.email.trim().lowercase()

        val participante = participanteRepository.findByEmail(email)
            ?.takeIf { passwordEncoder.matches(request.senha, it.senha) }
            ?: run {
                log.warn("Falha de login de participante para o e-mail {}.", email)
                throw NaoAutorizadoException(CREDENCIAIS_INVALIDAS)
            }

        log.info("Login de participante realizado: id {}.", participante.id)
        val usuario = UsuarioAutenticado(
            perfil = Perfil.PARTICIPANTE,
            email = participante.email,
            participanteId = participante.id
        )
        return TokenResponse(
            token = jwtService.gerarToken(usuario),
            perfil = Perfil.PARTICIPANTE,
            participanteId = participante.id,
            expiraEmSegundos = jwtService.expiracaoEmSegundos
        )
    }

    private companion object {
        const val CREDENCIAIS_INVALIDAS = "E-mail ou senha inválidos."
    }
}
