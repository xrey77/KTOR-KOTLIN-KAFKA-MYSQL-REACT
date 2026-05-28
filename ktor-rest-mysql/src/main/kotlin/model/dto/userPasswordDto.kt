package com.kotlin.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserPasswordDto(
    val password: String
)