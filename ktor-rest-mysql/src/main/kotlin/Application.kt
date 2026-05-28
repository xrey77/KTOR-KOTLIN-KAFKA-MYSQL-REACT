package com.kotlin

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import com.kotlin.data.DatabaseFactory
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import kotlinx.serialization.Serializable
import io.ktor.http.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.*
import io.ktor.util.AttributeKey
import com.kotlin.utils.TokenManager
import com.kotlin.configureRouting
import com.kotlin.services.KafkaService

val TokenManagerKey = AttributeKey<TokenManager>("TokenManagerKey")

fun main() {
    System.setProperty("java.awt.headless", "true")     
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {        
    val kafkaService = KafkaService()

    kafkaService.startConsumer("central-topic", "ktor-group", this) { key, value ->
        log.info("Kafka Consumer received: Key = $key, Value = $value")
    }

    monitor.subscribe(ApplicationStopping) {
        kafkaService.stop()
    }

    val tokenManager = TokenManager()     
    attributes.put(TokenManagerKey, tokenManager)    

    DatabaseFactory.init() 
    configureSerialization()
    configureExceptions()
    configureRouting()
}

fun Application.configureSerialization() {
   install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }    
}

fun Application.configureExceptions() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            val errorBody = ErrorResponse(cause.message ?: "Invalid request")
            call.respond(HttpStatusCode.BadRequest, errorBody)
        }
    }
}

fun Application.configureSecurity() {
    println("Hello, Terminal World!") 

    val manager = TokenManager()
    attributes.put(TokenManagerKey, manager)
    install(Authentication) {
        jwt("auth-jwt") {
            realm = "Access to 'protected' routes"
            verifier(manager.verifier) 
            validate { credential ->
                if (credential.payload.getClaim("username").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}

@Serializable
data class ErrorResponse(val message: String)