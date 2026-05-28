package com.kotlin.repository

import org.jetbrains.exposed.sql.*
import com.kotlin.model.RegisterModel
import com.kotlin.model.LoginModel
import com.kotlin.model.UserModel
import com.kotlin.model.ActivateMfaModel
import com.kotlin.model.User
import com.kotlin.model.MfaUserModel
import com.kotlin.model.UploadModel
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import com.kotlin.model.RoleModel
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object UserTable : Table("users") {
    val id = integer("id").autoIncrement()
    val firstname = varchar("firstname", 50)
    val lastname = varchar("lastname", 50)
    val email = varchar("email", 100).uniqueIndex()
    val mobile = varchar("mobile", 20)
    val username = varchar("username", 50).uniqueIndex()
    val password = varchar("password", 100)
    val isActive = bool("isactive").default(true)
    val isBlocked = bool("isblocked").default(false)
    val mailtoken = integer("mailtoken").default(0)
    val userpic = varchar("userpic", 244)
    val secret = text("secret")
    val qrcodeurl = text("qrcodeurl")
    val role_id = integer("role_id")

    override val primaryKey = PrimaryKey(id)
}

object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val secret = text("secret").nullable()
    val qrcodeurl = text("qrcodeurl").nullable()
    override val primaryKey = PrimaryKey(id)
}

object UserProfilepic : Table("users") {
    val id = integer("id").autoIncrement()
    val userpic = varchar("userpic", 244)
    override val primaryKey = PrimaryKey(id)
}


object RoleTable : Table("roles") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50)

    override val primaryKey = PrimaryKey(id)
}


interface UserRepository {
    suspend fun findByUsername(username: String): RegisterModel?
    suspend fun findLoginUsername(username: String): LoginModel?
    suspend fun findByEmail(email: String): RegisterModel?
    suspend fun findRoleByName(roleName: String): RoleModel    
    suspend fun save(user: RegisterModel): RegisterModel
    suspend fun findUserById(idno: Int): UserModel?
    suspend fun activateMfa(id: Int, twofactorenabled: Boolean, secret: String, qrcodeurl: String): UserModel?
    suspend fun uploadUpdateProfilepic(id: Int, userpic: String): UserModel?
}

class UserRepositoryImpl : UserRepository {

    private fun rowToUser(row: ResultRow): RegisterModel {
        return RegisterModel(
            id = row[UserTable.id],
            firstname = row[UserTable.firstname],
            lastname = row[UserTable.lastname],
            email = row[UserTable.email],
            mobile = row[UserTable.mobile],
            username = row[UserTable.username],
            password = row[UserTable.password],
            role_id = row[UserTable.role_id]
        )
    }

    private fun loginToUser(row: ResultRow): LoginModel {
        return LoginModel(
            id = row[UserTable.id],
            firstname = row[UserTable.firstname],
            lastname = row[UserTable.lastname],
            email = row[UserTable.email],
            mobile = row[UserTable.mobile],
            username = row[UserTable.username],
            password = row[UserTable.password],
            isActive = row[UserTable.isActive],
            isBlocked = row[UserTable.isBlocked],
            mailtoken = row[UserTable.mailtoken],
            userpic = row[UserTable.userpic],
            secret = row[UserTable.secret],
            qrcodeurl = row[UserTable.qrcodeurl]
        )
    }


    private fun userIdToUser(row: ResultRow): UserModel {
        return UserModel(
            id = row[UserTable.id],
            firstname = row[UserTable.firstname],
            lastname = row[UserTable.lastname],
            email = row[UserTable.email],
            mobile = row[UserTable.mobile],
            username = row[UserTable.username],
            isActive = row[UserTable.isActive],
            isBlocked = row[UserTable.isBlocked],
            mailtoken = row[UserTable.mailtoken],
            userpic = row[UserTable.userpic],
            qrcodeurl = row[UserTable.qrcodeurl]
        )
    }

    private fun mfaUser(row: ResultRow): MfaUserModel {
        return MfaUserModel(
            id = row[UserTable.id],
            username = row[UserTable.username],
            secret = row[UserTable.secret],
            qrcodeurl = row[UserTable.qrcodeurl]
        )
    }


    suspend fun updateProfile(id: Int, fname: String, lname: String, mobile: String): UserModel = newSuspendedTransaction {
        println("Inputs: $fname, $lname, $mobile") 
        val updatedRows = UserTable.update({ UserTable.id eq id }) {
            it[firstname] = fname
            it[lastname] = lname
            it[UserTable.mobile] = mobile
        }
        
        findUserById(id) ?: throw IllegalStateException("User lost after update")
    }        

    suspend fun changePassword(id: Int, pword: String): UserModel = newSuspendedTransaction {
        val updatedRows = UserTable.update({ UserTable.id eq id }) {
            it[password] = pword
        }
        
        findUserById(id) ?: throw IllegalStateException("User lost after update")
    }        

    override suspend fun activateMfa(id: Int, twofactorenabled: Boolean, secret: String, qrcodeurl: String): UserModel = newSuspendedTransaction {
        if (twofactorenabled) {
            val updatedRows = Users.update({ Users.id eq id }) {
                it[Users.secret] = secret
                it[Users.qrcodeurl] = qrcodeurl
            }
        } else {
            val updatedRows = Users.update({ Users.id eq id }) {
                it[Users.secret] = null
                it[Users.qrcodeurl] = null
            }
        }
        
        findUserById(id) ?: throw IllegalStateException("User lost after update")
    }        

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction { block() }

    override suspend fun findByUsername(username: String): RegisterModel? {
        return newSuspendedTransaction {
            UserTable.selectAll()
                .where { UserTable.username eq username }
                .map { rowToUser(it) }
                .singleOrNull()
        }
    }

    override suspend fun findLoginUsername(username: String): LoginModel? {
        return newSuspendedTransaction {
            UserTable.selectAll()
                .where { UserTable.username eq username }
                .map { loginToUser(it) }
                .singleOrNull()
        }
    }

    override suspend fun findUserById(idno: Int): UserModel? {
        return newSuspendedTransaction {
            UserTable.selectAll()
                .where { UserTable.id eq idno }
                .map { userIdToUser(it) }
                .singleOrNull()
        }
    }


    suspend fun findMfaUserId(idno: Int): MfaUserModel? {
        return newSuspendedTransaction {
            UserTable.selectAll()
                .where { UserTable.id eq idno }
                .map { mfaUser(it) }
                .singleOrNull()
        }
    }





    override suspend fun findByEmail(email: String): RegisterModel? {
        return newSuspendedTransaction {
            UserTable.selectAll()
                .where { UserTable.email eq email }
                .map { rowToUser(it) }
                .singleOrNull()
        }
    }

    override suspend fun findRoleByName(roleName: String): RoleModel {
        return newSuspendedTransaction {
            RoleTable.selectAll()
                .where { RoleTable.name eq roleName }
                .map { 
                    RoleModel(
                        id = it[RoleTable.id],
                        name = it[RoleTable.name]
                    )
                }
                .single()
        }
    }

    override suspend fun save(user: RegisterModel): RegisterModel = dbQuery {
        val insertStatement = UserTable.insert {
            it[firstname] = user.firstname
            it[lastname] = user.lastname
            it[email] = user.email
            it[mobile] = user.mobile ?: ""
            it[username] = user.username
            it[password] = user.password
            it[role_id] = user.role_id
        }
        
        user.copy(id = insertStatement[UserTable.id])
    }

    override suspend fun uploadUpdateProfilepic(id: Int, userpic: String): UserModel = dbQuery {
        UserProfilepic.update({ UserProfilepic.id eq id }) {
            it[UserProfilepic.userpic] = userpic
        }

        findUserById(id) ?: throw IllegalStateException("User lost after update")
    }


}
