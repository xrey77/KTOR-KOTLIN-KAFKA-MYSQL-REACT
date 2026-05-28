package com.kotlin.model

import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

data class UserRow(
    val id: Int,
    val firstname: String,
    val lastname: String,
    val email: String,
    val mobile: String,
    val username: String,
    val password: String,
    val isActive: Boolean,
    val isBlocked: Boolean,  
    val mailtoken: Int,
    val userpic: String,
    val secret: String,
    val qrcodeurl: String,
    val roleId: Int,   
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

object Roles : Table("roles") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50)
    override val primaryKey = PrimaryKey(id)
}

object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val firstname = varchar("firstname", 100)
    val lastname = varchar("lastname", 100)
    val email = varchar("email", 255).uniqueIndex()
    val mobile = varchar("mobile", 20)
    val username = varchar("username", 50, collate = "utf8mb4_bin").uniqueIndex()    
    val password = varchar("password", 255)
    val isActive = bool("isactive").default(true)
    val isBlocked = bool("isblocked").default(false)
    val mailtoken = integer("mailtoken").default(0)
    val userpic = varchar("userpic", 255).default("pix.png")
    val secret = text("secret").nullable().default(null)
    val qrcodeurl = text("qrcodeurl").nullable().default(null)
    
    val role = integer("role_id").references(Roles.id)         

    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)


    override val primaryKey = PrimaryKey(id)
}
