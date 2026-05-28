package com.kotlin.services

import com.kotlin.model.ProductModel
import com.kotlin.repository.ProductRepositoryImpl
import com.kotlin.model.PagedResponse
import com.kotlin.model.CategoryWithProducts

class ProductService(private val productRepository: ProductRepositoryImpl) {

    suspend fun productDataList(page: Int): PagedResponse<ProductModel> {
        val response = productRepository.findProducts(page)
        if (response.products.isEmpty()) {
            throw IllegalArgumentException("Products not found.")
        }
        return response
    }    


    suspend fun productDataSearch(page: Int, descriptions: String): PagedResponse<ProductModel> {
        val response = productRepository.searchProducts(page, descriptions)
        if (response.products.isEmpty()) {
            throw IllegalArgumentException("Products not found.")
        }
        return response
    }    

    suspend fun getCategoriesWithProducts(): List<CategoryWithProducts> {
        return productRepository.getCategoriesWithProducts()
    }

}
