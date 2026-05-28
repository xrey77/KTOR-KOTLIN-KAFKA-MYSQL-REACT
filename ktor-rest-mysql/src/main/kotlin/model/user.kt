package com.kotlin.model

import kotlinx.serialization.Serializable
import com.kotlin.model.Role

@Serializable
data class LoginResponse(
    val message: String,
    val user: UserResponse
)

@Serializable
data class UpdateResponse(
    val message: String
)


@Serializable
data class UserDataResponse(
    val message: String,
    val user: UserModel
)

@Serializable
data class UploadResponse(
    val message: String,
    val userpic: String
)

@Serializable
data class UploadModel(
    val userpic: String
)


@Serializable
data class ActivateMfaResponse(
    val message: String,
    val qrcodeurl: String?
)

@Serializable
data class ActivateMfaModel(
    val message: String,
    val qrcodeurl: String?
)


@Serializable
data class UploadPicModel(
    val message: String,
    val userpic: String
)


@Serializable
data class RegisterModel(
    val id: Int,
    val firstname: String,
    val lastname: String,
    val email: String,
    val mobile: String?,
    val username: String,
    val password: String,
    val role_id: Int
)

@Serializable
data class LoginModel(
    val id: Int,
    val firstname: String,
    val lastname: String,
    val email: String,
    val mobile: String,
    val username: String,
    val password: String,
    val isActive: Boolean,
    val isBlocked: Boolean,
    val mailtoken: Int,
    val userpic: String,
    val secret: String?, 
    val qrcodeurl: String?
)

@Serializable
data class UserResponse(
    val id: Int,
    val firstname: String,
    val lastname: String,
    val email: String,
    val mobile: String,
    val username: String,
    val isActive: Boolean,
    val isBlocked: Boolean,
    val mailtoken: Int,
    val userpic: String,
    val qrcodeurl: String?,
    val token: String?
)


@Serializable
data class UserModel(
    val id: Int,
    val firstname: String,
    val lastname: String,
    val email: String,
    val mobile: String,
    val username: String,
    val isActive: Boolean,
    val isBlocked: Boolean,
    val mailtoken: Int,
    val userpic: String,
    val qrcodeurl: String?
)


@Serializable
data class RoleModel(
    val id: Int,
    val name: String
)

@Serializable
data class MfaUserModel(
    val id: Int,
    val username: String,
    val secret: String, 
    val qrcodeurl: String, 
)


@Serializable
data class User(
    val id: Int,
    val firstname: String,
    val lastname: String,
    val email: String,
    val mobile: String,
    val username: String,
    val password: String,
    val isActive: Boolean,
    val isBlocked: Boolean,
    val mailtoken: Int,
    val userpic: String,
    val secret: String, 
    val qrcodeurl: String, 
    val createdAt: String, 
    val updatedAt: String,
    val role: Role
)
