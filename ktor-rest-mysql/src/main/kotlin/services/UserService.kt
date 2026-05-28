package com.kotlin.services


import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

import org.mindrot.jbcrypt.BCrypt
import com.kotlin.utils.PasswordHasher
import com.kotlin.model.dto.UserRegistrationDto
import com.kotlin.model.dto.UserLoginDto
import com.kotlin.model.RegisterModel
import com.kotlin.model.LoginModel
import com.kotlin.model.UserModel
import com.kotlin.model.UploadPicModel
import com.kotlin.model.ActivateMfaModel
import com.kotlin.repository.UserRepositoryImpl
import com.kotlin.repository.MessagePublisher
import com.kotlin.services.TotpService
import com.kotlin.services.KafkaService
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import java.io.File

class UserService(private val userRepository: UserRepositoryImpl) {

    private val kafkaService = KafkaService()
    private val totService =  TotpService()
    private val client = HttpClient(CIO)

    init {
        kafkaService.initProducer() 
    }


    suspend fun createUser(request: UserRegistrationDto): RegisterModel {

        require(request.email.contains("@")) { "Invalid email format" }
        require(request.password.length >= 3) { "Password must be at least 3 characters" }

        val existingEmail = userRepository.findByEmail(request.email)
        if (existingEmail != null) {
            throw IllegalArgumentException("Email Address has already been already taken.")
        }

        val existingUser = userRepository.findByUsername(request.username)
        if (existingUser != null) {
            throw IllegalArgumentException("Username has already been already taken.")
        }

        val hashedPassword = PasswordHasher.hash(request.password)
        val rolename = userRepository.findRoleByName("ROLE_USER")
        
        val newUser = RegisterModel(
            id = 0,
            firstname = request.firstname,
            lastname = request.lastname,
            email = request.email,
            mobile = request.mobile ?: "",
            username = request.username,
            password = hashedPassword,
            role_id = rolename.id
        )

        val savedUser = userRepository.save(newUser)         
        val messagePayload = Json.encodeToString(savedUser)

        kafkaService.sendMessage(
            topic = "central-topic",
            key = savedUser.id.toString(),
            value = messagePayload
        )

        return savedUser
    }


    suspend fun userAccount(request: UserLoginDto): LoginModel {
        require(request.password.length >= 3) { "Password must be at least 3 characters" }

        val existingUser = userRepository.findLoginUsername(request.username)
        if (existingUser != null) {

            val isPasswordValid = PasswordHasher.checkPassword(request.password, existingUser.password)
            if (!isPasswordValid) {
                throw IllegalArgumentException("Invalid password, please try again.")
            }

            val loginModel = LoginModel(
                id = existingUser.id,
                firstname = existingUser.firstname,
                lastname = existingUser.lastname,
                email = existingUser.email,
                mobile = existingUser.mobile,
                username = existingUser.username,
                password = "",
                isActive = existingUser.isActive,
                isBlocked = existingUser.isBlocked,
                mailtoken = existingUser.mailtoken,
                userpic = existingUser.userpic,
                secret = existingUser.secret,
                qrcodeurl = existingUser.qrcodeurl
            )

            val messagePayload = Json.encodeToString(loginModel)

            kafkaService.sendMessage(
                topic = "central-topic",
                key = loginModel.id.toString(),
                value = messagePayload
            )


            return loginModel

        } else {
            throw IllegalArgumentException("User not found, please register now.")
        }

    }


    suspend fun getUserData(id: Int): UserModel {
        val existingUser = userRepository.findUserById(id)
        if (existingUser != null) {

            val userModel = UserModel(
                id = existingUser.id,
                firstname = existingUser.firstname,
                lastname = existingUser.lastname,
                email = existingUser.email,
                mobile = existingUser.mobile,
                username = existingUser.username,
                isActive = existingUser.isActive,
                isBlocked = existingUser.isBlocked,
                mailtoken = existingUser.mailtoken,
                userpic = existingUser.userpic,
                qrcodeurl = existingUser.qrcodeurl
            )

            return userModel

        } else {
            throw IllegalArgumentException("User not found, please register now.")
        }
    }    

    suspend fun upateUserProfile(id: Int, fname: String, lname: String, mobile: String): String {
        val updatedRows = userRepository.findUserById(id)
        if (updatedRows == null) {
            throw IllegalArgumentException("User not found, please register now.")
        } 
        userRepository.updateProfile(id, fname, lname, mobile)
        return "You have update your profile successfully."        
        
    }    


    suspend fun updatePassword(id: Int, pword: String): String {
        val updatedRows = userRepository.findUserById(id)
        if (updatedRows == null) {
            throw IllegalArgumentException("User not found, please register now.")
        } 

        val hashedPwd = PasswordHasher.hash(pword)

        userRepository.changePassword(id, hashedPwd)
        return "You have changed your password successfully."        
        
    }    


    suspend fun activateMfa(id: Int, twofactorenabled: Boolean): ActivateMfaModel {
        val userData = userRepository.findUserById(id)
        if (userData == null) {
            throw IllegalArgumentException("User not found, please register now.")
        } 


        if (twofactorenabled) {
            // println("Mfa Enabled......")            
            val secret = totService.generateSecret()
            val b64qrcode = totService.getQrCodeUrl(secret, userData.email, "Arab Bank")
            userRepository.activateMfa(id, twofactorenabled, secret, b64qrcode)
            val result = ActivateMfaModel(message="Multi-Factor Authenticator has been enabled.",qrcodeurl=b64qrcode)
            return result
        } else {
            // println("Mfa Disabled......")
            val secret = ""
            val qrcodeurl = ""
            userRepository.activateMfa(id, twofactorenabled, secret, qrcodeurl)
            val result = ActivateMfaModel(message="Multi-Factor Authenticator has been disabled.",qrcodeurl=null)
            return result
        }
    }    

    suspend fun verifyTotp(id: Int, otp: String): String {
        val checkUserid = userRepository.findMfaUserId(id)
        if (checkUserid == null) {
            throw IllegalArgumentException("User not found, please register now.")
        } 
        val res = totService.verifyOtp(checkUserid.secret, otp)
        if (res) {
            return "Ok"
        } else {
            throw IllegalArgumentException("Invalid OTP code, please try again.")
        }
    }

    suspend fun uploadProfilepic(id: Int, userpic: String): UploadPicModel {
       var updatepic = userRepository.uploadUpdateProfilepic(id, userpic)
       return UploadPicModel(
            message="You have change your profile picture successfully.",
            userpic=userpic
       )

    }



}
