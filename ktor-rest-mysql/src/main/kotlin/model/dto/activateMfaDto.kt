package com.kotlin.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActivateMfaDto(
    val twofactorenabled: Boolean,
)