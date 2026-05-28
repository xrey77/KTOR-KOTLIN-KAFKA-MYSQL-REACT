package com.kotlin.data

import com.kotlin.model.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        val driverClassName = "com.mysql.cj.jdbc.Driver"
        val jdbcUrl = "jdbc:mysql://127.0.0.1:3306/ktor_kotlin"
        
        val database = Database.connect(createHikariDataSource(jdbcUrl, driverClassName))
        
        transaction(database) {
            SchemaUtils.create(Products, Sales, Users, Roles, Categories)            
        }
    }

    private fun createHikariDataSource(url: String, driver: String) = HikariDataSource(
        HikariConfig().apply {
            driverClassName = driver
            jdbcUrl = url
            username = "rey"
            password = "rey"
            maximumPoolSize = 3
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
    )
}
