package com.kotlin.services

import com.kotlin.model.Sale
import com.kotlin.model.SaleModel
import com.kotlin.repository.SalesRepositoryImpl

class SalesService(private val salesRepository: SalesRepositoryImpl) {

    suspend fun salesDataList(): List<SaleModel> {
        val response = salesRepository.findSales()
        if (response.isEmpty()) {
            throw IllegalArgumentException("Sales not found.")
        }
        return response
    }    
}
