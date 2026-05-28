// src/test/kotlin/ServerTest.kt
package com.kotlin

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ServerTest {
    @Test
    fun testRootEndpoint() = testApplication {        
        application {
            configureRouting() 
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
    }
}
