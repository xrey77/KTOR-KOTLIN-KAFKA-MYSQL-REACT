package com.kotlin.repository

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import com.kotlin.model.ProductModel
import com.kotlin.model.PagedResponse

import com.kotlin.model.Categories
import com.kotlin.model.Products
import com.kotlin.model.CategoryWithProducts
import com.kotlin.model.ProductDetail
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction


object ProductTable : Table("products") {
    val id = integer("id").autoIncrement()
    val descriptions = varchar("descriptions", 100).uniqueIndex()
    val qty = integer("qty")
    val unit = varchar("unit", 255)
    val costprice = double("costprice")
    val sellprice = double("sellprice")
    val saleprice = double("saleprice")
    val productpicture = varchar("productpicture", 255)    
    val alertstocks = integer("alertstocks")    
    val criticalstocks = integer("criticalstocks")
    val categoryId = integer("category_id").references(Categories.id)         

    override val primaryKey = PrimaryKey(id)
}


object ProdTable : Table("products") {
    val id = integer("id").autoIncrement()
    val descriptions = varchar("descriptions", 100).uniqueIndex()
    val qty = integer("qty")
    val unit = varchar("unit", 255)
    val costprice = double("costprice")
    val sellprice = double("sellprice")

    override val primaryKey = PrimaryKey(id)
}

object Categories : Table("categories") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50)
    override val primaryKey = PrimaryKey(id, name = "PK_Categories_Id")
}




interface ProductRepository {
    suspend fun findByDescriptions(descriptions: String): ProductModel?
    suspend fun findProducts(page: Int): PagedResponse<ProductModel>
    suspend fun searchProducts(page: Int, descriptions: String): PagedResponse<ProductModel>
    suspend fun getCategoriesWithProducts(): List<CategoryWithProducts>
}


class ProductRepositoryImpl : ProductRepository {

    private fun rowToProduct(row: ResultRow): ProductModel {
        return ProductModel(
            id = row[ProductTable.id],
            descriptions = row[ProductTable.descriptions],
            qty = row[ProductTable.qty],
            unit = row[ProductTable.unit],
            costprice = row[ProductTable.costprice],
            sellprice = row[ProductTable.sellprice],
            productpicture = row[ProductTable.productpicture],
        )
    }

    override suspend fun findByDescriptions(descriptions: String): ProductModel? = newSuspendedTransaction {
        ProductTable
            .selectAll()
            .where { ProductTable.descriptions like "%$descriptions%" }
            .map { rowToProduct(it) }
            .singleOrNull()
    }

    override suspend fun findProducts(page: Int): PagedResponse<ProductModel> {
        val pageSize = 5
        val calculatedOffset = ((page - 1) * pageSize).toLong()

        return newSuspendedTransaction {
            val totalRecords = ProductTable.selectAll().count() 
            val totalPages = kotlin.math.ceil(totalRecords.toDouble() / pageSize).toInt()

            val data = ProductTable.selectAll()
                .limit(pageSize, offset = calculatedOffset)
                .map { rowToProduct(it) }            

            PagedResponse(
                page = page,
                totalPages = totalPages,
                totalRecords = totalRecords,
                products = data
            )
        }
    }



    override suspend fun searchProducts(page: Int, descriptions: String): PagedResponse<ProductModel> {
        val pageSize = 5
        val calculatedOffset = ((page - 1) * pageSize).toLong()

        return newSuspendedTransaction {
            val query = ProductTable.selectAll().where { 
                ProductTable.descriptions like concat(stringLiteral("%"), stringLiteral(descriptions), stringLiteral("%")) 
            }

            val totalRecords = query.count() 
            val totalPages = kotlin.math.ceil(totalRecords.toDouble() / pageSize).toInt()

            val data = query.copy()
                .limit(pageSize, offset = calculatedOffset)
                .map { rowToProduct(it) }       

            PagedResponse(
                page = page,
                totalPages = totalPages,
                totalRecords = totalRecords,
                products = data
            )
        }
    }


    override suspend fun getCategoriesWithProducts(): List<CategoryWithProducts> = transaction {
        val rows = (Categories innerJoin Products)
            .selectAll()
            .where { Categories.id eq Products.categoryId }
            .map { row ->
                row[Categories.name] to ProductDetail(
                    id = row[Products.id],
                    descriptions = row[Products.descriptions],
                    qty = row[Products.qty],
                    unit = row[Products.unit],
                    costprice = row[Products.costprice].toDouble(),
                    sellprice = row[Products.sellprice].toDouble()
                )
            }

        rows.groupBy({ it.first }, { it.second })
            .map { (categoryName, productsList) ->
                CategoryWithProducts(
                    categoryName = categoryName,
                    products = productsList
                )
            }
    }


}
