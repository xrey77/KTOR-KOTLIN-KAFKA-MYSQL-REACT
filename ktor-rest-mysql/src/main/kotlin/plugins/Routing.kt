package com.kotlin

import io.ktor.http.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.* 
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.kotlin.services.SalesService
import com.kotlin.repository.SalesRepositoryImpl
import com.kotlin.services.ProductService
import com.kotlin.services.UserService
import com.kotlin.repository.ProductRepositoryImpl
import com.kotlin.repository.UserRepositoryImpl
import com.kotlin.model.dto.UserRegistrationDto
import com.kotlin.model.dto.UserLoginDto
import com.kotlin.model.dto.UserProfileDto
import com.kotlin.model.dto.UserPasswordDto
import com.kotlin.model.dto.ActivateMfaDto
import com.kotlin.model.dto.OtpDto
import com.kotlin.model.dto.UploadDto
import com.kotlin.model.LoginResponse
import com.kotlin.model.UserDataResponse
import com.kotlin.model.UserResponse
import com.kotlin.model.UpdateResponse
import com.kotlin.model.UploadResponse
import com.kotlin.model.ActivateMfaResponse
import com.kotlin.model.UserModel
import com.kotlin.TokenManagerKey 

import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.request.receiveMultipart
import java.io.File
import io.ktor.utils.io.* 
import kotlinx.io.*



fun Application.configureRouting() {

    val tokenManager = attributes.getOrNull(TokenManagerKey) 
        ?: throw IllegalStateException("Security module was not initialized before routing!")

    val userRepository = UserRepositoryImpl()
    val userService = UserService(userRepository) 

    val productRepository = ProductRepositoryImpl()
    val productService = ProductService(productRepository)

    val salesRepository = SalesRepositoryImpl()
    val salesService = SalesService(salesRepository)

    routing {
        staticFiles("/static", File("users"))
        staticResources("/", "static", index = "index.html") 
        
        get("/") {
            call.respondText(
                this::class.java.classLoader.getResource("static/index.html")!!.readText(),
                ContentType.Text.Html
            )
        }


        post("/register") {
            try {
                val request = call.receive<UserRegistrationDto>()                         
                val createdUser = userService.createUser(request)
                
                call.respond(HttpStatusCode.Created, mapOf(
                    "status" to "You have registered successfully, please login now."
                ))            
            } catch (e: BadRequestException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to (e.localizedMessage ?: "Invalid JSON format")))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("message" to (e.localizedMessage ?: "Unknown error")))
            }                
        }

        post("/login") {
            try {
                val request = call.receive<UserLoginDto>()                         
                val loginUser = userService.userAccount(request)        

                val tokenid = tokenManager.generateToken(loginUser.username)

                val user = UserResponse(
                    id = loginUser.id,
                    firstname = loginUser.firstname,
                    lastname = loginUser.lastname,
                    email = loginUser.email,
                    mobile = loginUser.mobile,
                    username = loginUser.username,
                    isActive = loginUser.isActive,
                    isBlocked = loginUser.isBlocked,
                    mailtoken = loginUser.mailtoken,
                    userpic = loginUser.userpic,
                    qrcodeurl = loginUser.qrcodeurl,
                    token = tokenid
                )
                call.respond(HttpStatusCode.OK, LoginResponse("You have logged-in successfully, please wait.", user))
            } catch (e: BadRequestException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to (e.localizedMessage ?: "Invalid JSON format")))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("message" to (e.localizedMessage ?: "Unknown error")))
            }                

        }

        get("/api/getuserid/{id}") {
            try {

                val authHeader = call.request.headers["Authorization"]
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Unauthorized Access!"))
                    return@get
                }

                val token = authHeader.removePrefix("Bearer ")
                val username = tokenManager.verifyToken(token) 
                if (username == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Invalid or expired token"))
                    return@get
                }

                val userId = call.parameters["id"]?.toIntOrNull() 
                    ?: throw BadRequestException("Invalid or missing ID")
                val userData = userService.getUserData(userId)
                val user = UserModel(
                    id = userData.id,
                    firstname = userData.firstname,
                    lastname = userData.lastname,
                    email = userData.email,
                    mobile = userData.mobile,
                    username = userData.username,
                    isActive = userData.isActive,
                    isBlocked = userData.isBlocked,
                    mailtoken = userData.mailtoken,
                    userpic = userData.userpic,
                    qrcodeurl = userData.qrcodeurl
                )



                call.respond(HttpStatusCode.OK, UserDataResponse("User ID $userId found.", user))
            } catch (e: BadRequestException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to (e.localizedMessage ?: "Invalid JSON format")))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("message" to (e.localizedMessage ?: "Unknown error")))
            }                
        }
    


        patch("/api/updateprofile/{id}") {
            try {

                val authHeader = call.request.headers["Authorization"]
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Unauthorized Access!"))
                    return@patch
                }

                val token = authHeader.removePrefix("Bearer ")
                val username = tokenManager.verifyToken(token) 
                if (username == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Invalid or expired token"))
                    return@patch
                }

                val userId = call.parameters["id"]?.toIntOrNull() 
                    ?: throw BadRequestException("Invalid or missing ID")
                val request = call.receive<UserProfileDto>()
                
                val responseData = userService.upateUserProfile(userId, request.firstname, request.lastname, request.mobile)
                call.respond(HttpStatusCode.OK, UpdateResponse(responseData))

            } catch (e: BadRequestException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to (e.localizedMessage ?: "Invalid JSON format")))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("message" to (e.localizedMessage ?: "Unknown error")))
            }                
        }

        patch("/api/changepassword/{id}") {
            try {

                val authHeader = call.request.headers["Authorization"]
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Unauthorized Access!"))
                    return@patch
                }

                // 2. Strip "Bearer " to get the raw token string
                val token = authHeader.removePrefix("Bearer ")
                val username = tokenManager.verifyToken(token) 
                if (username == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Invalid or expired token"))
                    return@patch
                }

                val userId = call.parameters["id"]?.toIntOrNull() 
                    ?: throw BadRequestException("Invalid or missing ID")
                val request = call.receive<UserPasswordDto>()
                
                val responseData = userService.updatePassword(userId, request.password)
                call.respond(HttpStatusCode.OK, UpdateResponse(responseData))

            } catch (e: BadRequestException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to (e.localizedMessage ?: "Invalid JSON format")))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("message" to (e.localizedMessage ?: "Unknown error")))
            }                
        }


        patch("/api/activatemfa/{id}") {
            try {

                val authHeader = call.request.headers["Authorization"]
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Unauthorized Access!"))
                    return@patch
                }

                val token = authHeader.removePrefix("Bearer ")
                val username = tokenManager.verifyToken(token) 
                if (username == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Invalid or expired token"))
                    return@patch
                }



                val userId = call.parameters["id"]?.toIntOrNull() 
                    ?: throw BadRequestException("Invalid or missing ID")
                val request = call.receive<ActivateMfaDto>()

                val response = userService.activateMfa(userId, request.twofactorenabled)
                val result = ActivateMfaResponse(
                        message = response.message,
                        qrcodeurl = response.qrcodeurl
                    )

                call.respond(HttpStatusCode.OK, result)

            } catch (e: BadRequestException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to (e.localizedMessage ?: "Invalid JSON format")))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("message" to (e.localizedMessage ?: "Unknown error")))
            }                
        }


        patch("/api/verifyotp/{id}") {
            try {
                val authHeader = call.request.headers["Authorization"]
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Unauthorized Access!"))
                    return@patch
                }

                val token = authHeader.removePrefix("Bearer ")
                val username = tokenManager.verifyToken(token) 
                if (username == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Invalid or expired token"))
                    return@patch
                }

                val userId = call.parameters["id"]?.toIntOrNull() 
                    ?: throw BadRequestException("Invalid or missing ID")
                val request = call.receive<OtpDto>()
                val responseData = userService.verifyTotp(userId, request.otp)
                call.respond(HttpStatusCode.OK, ActivateMfaResponse("OTP code has been validated successfully.", responseData))
            } catch (e: BadRequestException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to (e.localizedMessage ?: "Invalid JSON format")))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("message" to (e.localizedMessage ?: "Unknown error")))
            }                
        }


        patch("/api/uploadprofilepic/{id}") {
            try {

                val authHeader = call.request.headers["Authorization"]
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Unauthorized Access!"))
                    return@patch
                }

                val token = authHeader.removePrefix("Bearer ")
                val username = tokenManager.verifyToken(token) 
                if (username == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Invalid or expired token"))
                    return@patch
                }



                val userId = call.parameters["id"]?.toIntOrNull() 
                    ?: throw BadRequestException("Invalid or missing ID")

                var fileName: String? = null
                var fileBytes: ByteArray? = null

                val multipart = call.receiveMultipart()

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            if (part.name == "userpic") {
                                fileName = part.originalFileName ?: "pix.png"
                                fileBytes = part.provider().readRemaining().readByteArray()                              
                            }
                        }       
                        else -> {}                                            
                    }
                    part.release()
                }

                if (fileName == null || fileBytes == null) {
                    throw BadRequestException("Missing file payload")
                }

                val uploadDir = File("src/main/resources/static/users").apply { mkdirs() }
                val ext = File(fileName).extension 
                val newFilename = "00${userId}.${ext}"
                val fileDescription = File(uploadDir, newFilename)
                fileDescription.writeBytes(fileBytes)

                val responseData = userService.uploadProfilepic(userId, newFilename)
                call.respond(HttpStatusCode.OK, mapOf("message" to "You have changed your profile picture successfully.", "userpic" to newFilename))


            } catch (e: BadRequestException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to (e.localizedMessage ?: "Invalid request")))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("message" to (e.localizedMessage ?: "Unknown error")))
            }
        }


        get("/api/productlist/{page}") {
            try {
                val page = call.parameters["page"]?.toIntOrNull() 
                    ?: throw BadRequestException("Invalid or missing Page")

                val products = productService.productDataList(page)
                call.respond(HttpStatusCode.OK, mapOf("products" to products))

            } catch (e: BadRequestException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to (e.localizedMessage ?: "Invalid request")))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("message" to (e.localizedMessage ?: "Unknown error")))
            }                
        }


        get("/api/productsearch/{page}/{keyword}") {
            try {
                val page = call.parameters["page"]?.toIntOrNull() 
                    ?: throw BadRequestException("Invalid or missing Page")

                val key = call.parameters["keyword"] ?: ""                
                val products = productService.productDataSearch(page, key)
                call.respond(HttpStatusCode.OK, mapOf("products" to products))

            } catch (e: BadRequestException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to (e.localizedMessage ?: "Invalid request")))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("message" to (e.localizedMessage ?: "Unknown error")))
            }                
        }


        get("/api/getsales") {
            try {

                val sales = salesService.salesDataList()
                call.respond(HttpStatusCode.OK, mapOf("sales" to sales))

            } catch (e: BadRequestException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to (e.localizedMessage ?: "Invalid request")))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("message" to (e.localizedMessage ?: "Unknown error")))
            }                
        }

        get("/api/getproductbycategory") {
            try {

                val bycategory = productService.getCategoriesWithProducts()
                call.respond(HttpStatusCode.OK, mapOf("products" to bycategory))

            } catch (e: BadRequestException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to (e.localizedMessage ?: "Invalid request")))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("message" to (e.localizedMessage ?: "Unknown error")))
            }                
        }


    }
}