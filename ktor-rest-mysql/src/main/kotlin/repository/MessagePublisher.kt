package com.kotlin.repository

interface MessagePublisher {
    suspend fun publish(topic: String, message: String)
}
