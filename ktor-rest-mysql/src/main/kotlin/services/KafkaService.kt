package com.kotlin.services

import kotlinx.coroutines.*
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import java.time.Duration
import java.util.Properties
import java.util.concurrent.atomic.AtomicBoolean

class KafkaService(
    private val bootstrapServers: String = "localhost:9092"
) {
    private var producer: KafkaProducer<String, String>? = null
    private val keepRunning = AtomicBoolean(true)
    private var consumerJob: Job? = null

    // 1. Thread-safe Consumer initialized inside the dedicated coroutine
    fun startConsumer(
        topic: String, 
        groupId: String, 
        scope: CoroutineScope, 
        onMessageReceived: (key: String?, value: String) -> Unit
    ) {
        val config = Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
            put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
        }

        keepRunning.set(true)

        // Force execution on a dedicated thread pool for blocking I/O
        consumerJob = scope.launch(Dispatchers.IO) {
            val consumer = KafkaConsumer<String, String>(config)
            try {
                consumer.subscribe(listOf(topic))
                
                while (keepRunning.get() && CoroutineScope(coroutineContext).isActive) {
                    val records = consumer.poll(Duration.ofMillis(100))
                    for (record in records) {
                        onMessageReceived(record.key(), record.value())
                    }
                }
            } catch (e: Exception) {
                // Handle or pass to a logger framework
                println("Kafka consumer encountered an error: ${e.message}")
            } finally {
                // Ensures consumer closes on the same thread it was used
                consumer.close()
            }
        }
    }

    // 2. Thread-safe Producer Initialization
    fun initProducer() {
        if (producer != null) return
        val config = Properties().apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        }
        producer = KafkaProducer<String, String>(config)
    }

    fun sendMessage(topic: String, key: String?, value: String) {
        val producerInstance = producer ?: throw IllegalStateException("Producer not initialized. Call initProducer() first.")
        val record = ProducerRecord(topic, key, value)
        producerInstance.send(record)
    }

    // 3. Graceful Shutdown Routine
    fun stop() {
        keepRunning.set(false)
        runBlocking {
            consumerJob?.join()
        }
        producer?.close()
        producer = null
    }
}
