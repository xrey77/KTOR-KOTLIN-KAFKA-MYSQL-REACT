package com.kotlin.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import java.util.Date
import io.github.cdimascio.dotenv.dotenv

class TokenManager() {
    private val dotenv = dotenv()
    private val secret = dotenv["JWT_SECRET"] 
            ?: throw IllegalArgumentException("JWT_SECRET not found in .env")
            
    private val issuer = "http://127.0.0.1:8080"
    private val audience = "http://127.0.0.1:8080"
    private val VALIDITY_IN_MS = 36_000_000L

    private val algorithm = Algorithm.HMAC256(secret)

    val verifier: JWTVerifier = JWT.require(algorithm)
        .withAudience(audience)
        .withIssuer(issuer)
        .build()


    fun generateToken(username: String): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("username", username)
            .withExpiresAt(Date(System.currentTimeMillis() + VALIDITY_IN_MS))
            .sign(algorithm)
    }

    fun verifyToken(token: String): DecodedJWT? {
        return try {
            verifier.verify(token)
        } catch (e: Exception) {
            null // Returns null if token is expired or tampered with
        }
    }
}
