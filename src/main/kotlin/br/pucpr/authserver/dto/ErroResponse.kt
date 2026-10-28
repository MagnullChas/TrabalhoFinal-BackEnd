package br.pucpr.authserver.dto

data class ErroResponse(
    val status: Int,
    val erro: String,
    val mensagem: String
)