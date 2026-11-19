package br.pucpr.authserver.security

/**
 * Quem está fazendo a requisição, extraído do JWT.
 * Os IDs são separados: organizadorId só existe para ORGANIZADOR
 * e participanteId só existe para PARTICIPANTE.
 */
data class UsuarioAutenticado(
    val perfil: Perfil,
    val email: String,
    val organizadorId: Long? = null,
    val participanteId: Long? = null
) {
    val ehOrganizador: Boolean get() = perfil == Perfil.ORGANIZADOR
    val ehParticipante: Boolean get() = perfil == Perfil.PARTICIPANTE

    fun ehOrganizadorDe(id: Long): Boolean = ehOrganizador && organizadorId == id
}
