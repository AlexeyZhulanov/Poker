package com.example.poker.server.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.poker.server.data.repository.UserRepository
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.util.UUID

fun Application.configureSecurity() {
    val config = environment.config
    val secret = config.property("jwt.secret").getString()
    val issuer = config.property("jwt.issuer").getString()
    val audience = config.property("jwt.audience").getString()
    val myRealm = config.property("jwt.realm").getString()

    val userRepository = UserRepository()

    install(Authentication) {
        jwt("auth-jwt") {
            realm = myRealm
            verifier(
                JWT.require(Algorithm.HMAC256(secret))
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()
            )
            validate { credential ->
                val userIdString = credential.payload.getClaim("userId").asString()
                if (userIdString != null) {
                    // Просто находим пользователя по ID и возвращаем его.
                    // Ktor сам сделает его Principal-ом для этого запроса.
                    userRepository.findById(UUID.fromString(userIdString))
                } else {
                    null
                }
            }
        }
    }
}
