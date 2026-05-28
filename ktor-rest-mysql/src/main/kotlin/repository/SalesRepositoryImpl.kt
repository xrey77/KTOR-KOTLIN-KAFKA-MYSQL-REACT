package com.kotlin.repository

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import com.kotlin.model.Sale
import com.kotlin.model.SaleModel

import org.jetbrains.exposed.sql.javatime.datetime

object SaleTable : Table("sales") {
    val id = integer("id").autoIncrement()
    val salesamount = double("salesamount")
    val salesdate = varchar("salesdate", 100)

    override val primaryKey = PrimaryKey(id)
}


interface SaleRepository {
    suspend fun findSales(): List<SaleModel>
}


class SalesRepositoryImpl : SaleRepository {

    private fun rowToSale(row: ResultRow): SaleModel {
        return SaleModel(
            // id = row[SaleTable.id],
            salesamount = row[SaleTable.salesamount],
            salesdate = row[SaleTable.salesdate],
        )
    }


    override suspend fun findSales(): List<SaleModel> = newSuspendedTransaction {
        SaleTable.selectAll().map { rowToSale(it) }
    }

}
