package com.kotlin.model

import kotlinx.serialization.Serializable


@Serializable
data class SaleModel(
    val salesamount: Double,
    val salesdate: String
)


@Serializable
data class Sale(
    val id: Int,
    val salesamount: Double,
    val salesdate: String
)
