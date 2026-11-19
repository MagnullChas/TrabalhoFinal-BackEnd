package br.pucpr.authserver.model

import jakarta.persistence.*

@Entity
@Table(name = "organizadores")
class Organizador(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, length = 120)
    var nome: String,

    @Column(nullable = false, unique = true, length = 150)
    var email: String,

    // Hash BCrypt da senha (nunca a senha em texto puro)
    @Column(nullable = false, length = 100)
    var senha: String,

    @Column(length = 20)
    var telefone: String? = null
)
