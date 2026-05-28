package com.kotlin.model

import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.math.BigDecimal

data class SaleRow(
    val id: Int,             
    val salesamount: BigDecimal,
    val salesdate: java.time.LocalDateTime
)

object Sales : Table("sales") {
    val id = integer("id").autoIncrement()
    val salesamount = decimal("salesamount", 10, 2)
    val salesdate = datetime("salesdate")

    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)    
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
    
    override val primaryKey = PrimaryKey(id, name = "PK_Sales_Id")
}
