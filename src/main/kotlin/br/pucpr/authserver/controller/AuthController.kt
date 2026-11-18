package br.pucpr.authserver.controller

import br.pucpr.authserver.dto.LoginRequest
import br.pucpr.authserver.dto.TokenResponse
import br.pucpr.authserver.service.AuthService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/organizadores/login")
    fun loginOrganizador(@Valid @RequestBody request: LoginRequest): TokenResponse =
        authService.loginOrganizador(request)

    @PostMapping("/participantes/login")
    fun loginParticipante(@Valid @RequestBody request: LoginRequest): TokenResponse =
        authService.loginParticipante(request)
}
