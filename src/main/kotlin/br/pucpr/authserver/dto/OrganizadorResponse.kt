package br.pucpr.authserver.dto

data class OrganizadorResponse(
    val id: Long,
    val nome: String,
    val email: String,
    val telefone: String?
)
