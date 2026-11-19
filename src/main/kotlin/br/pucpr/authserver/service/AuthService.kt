package br.pucpr.authserver.service

import br.pucpr.authserver.dto.LoginRequest
import br.pucpr.authserver.dto.TokenResponse
import br.pucpr.authserver.exception.NaoAutorizadoException
import br.pucpr.authserver.repository.OrganizadorRepository
import br.pucpr.authserver.repository.ParticipanteRepository
import br.pucpr.authserver.security.JwtService
import br.pucpr.authserver.security.Perfil
import br.pucpr.authserver.security.UsuarioAutenticado
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val organizadorRepository: OrganizadorRepository,
    private val participanteRepository: ParticipanteRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {

    fun loginOrganizador(request: LoginRequest): TokenResponse {
        val email = request.email.trim().lowercase()

        val organizador = organizadorRepository.findByEmail(email)
            ?.takeIf { passwordEncoder.matches(request.senha, it.senha) }
            ?: throw NaoAutorizadoException(CREDENCIAIS_INVALIDAS)

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

        // O mesmo e-mail pode ter inscrições em vários eventos: vale a primeira cuja senha confere
        val participante = participanteRepository.findAllByEmail(email)
            .firstOrNull { passwordEncoder.matches(request.senha, it.senha) }
            ?: throw NaoAutorizadoException(CREDENCIAIS_INVALIDAS)

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
