package com.kotlin.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadDto(
    val fileName: String,
    val fileBytes: ByteArray
) {
    // Generated equals and hashCode are required when using ByteArray in data classes
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as UploadDto
        return fileBytes.contentEquals(other.fileBytes) && fileName == other.fileName
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + fileBytes.contentHashCode()
        return result
    }
}