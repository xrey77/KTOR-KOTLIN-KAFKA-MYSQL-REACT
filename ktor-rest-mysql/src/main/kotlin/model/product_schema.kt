package com.kotlin.model

import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.math.BigDecimal
import java.time.LocalDateTime

data class ProductRow(
    val id: Int,
    val descriptions: String,
    val qty: Int,
    val unit: String,
    val costprice: BigDecimal,
    val sellprice: BigDecimal,
    val saleprice: BigDecimal,
    val productpicture: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val categoryId: Int
)

object Categories : Table("categories") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50)
    override val primaryKey = PrimaryKey(id, name = "PK_Categories_Id")
}

object Products : Table("products") {
    val id = integer("id").autoIncrement()
    val descriptions = varchar("descriptions", 100).uniqueIndex()
    val qty = integer("qty")
    val unit = varchar("unit", 255)
    val costprice = decimal("costprice", 10, 2)
    val sellprice = decimal("sellprice", 10, 2)
    val saleprice = decimal("saleprice", 10, 2)
    val productpicture = varchar("productpicture", 255)
    val alertstocks = integer("alertstocks")    
    val criticalstocks = integer("criticalstocks")
    val categoryId = integer("category_id").references(Categories.id)         

    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)    
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
    
    override val primaryKey = PrimaryKey(id, name = "PK_Products_Id")
}
