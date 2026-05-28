// File: src/test/kotlin/com/kotlin/ApplicationTest.kt
package com.kotlin

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
// import com.kotlin.AuthTokenManager.Companion.AuthTokenManagerKey

class ApplicationTest {

    @Test
    fun testRootEndpoint() = testApplication {
        // environment {
        //     developmentMode = false
        // }
    
        application {
            configureSecurity()
            
            // attributes.put(AuthTokenManagerKey, MockTokenManager() as AuthTokenManager)                 
            attributes.put(AuthTokenManager.AuthTokenManagerKey, MockTokenManager() as AuthTokenManager)                        
            configureRouting() 
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
    }
}
