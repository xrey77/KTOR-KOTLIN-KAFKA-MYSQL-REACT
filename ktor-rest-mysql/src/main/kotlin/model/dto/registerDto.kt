package com.kotlin.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserRegistrationDto(
    @SerialName("firstname") val firstname: String,
    @SerialName("lastname") val lastname: String,
    val email: String,
    val mobile: String? = null,
    val username: String,
    val password: String
)
