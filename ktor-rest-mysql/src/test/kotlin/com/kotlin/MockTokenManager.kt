package com.kotlin

class MockTokenManager : AuthTokenManager {    
    override fun generateToken(username: String): String {
        return "mocked-jwt-token-for-$username"
    }
    
    override fun verifyToken(token: String): Boolean {
        return token.startsWith("mocked-jwt-token-for-")
    }
}
