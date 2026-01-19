package com.algorithmlx.astragone.utils.repository

import com.algorithmlx.astragone.database.PrivilegedUsersDatabase
import com.algorithmlx.astragone.database.UserProfile
import com.algorithmlx.astragone.database.UserProfileDatabase
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepository(private val db: Database) {
    fun exists(userId: String): Boolean = UserProfile.Companion.isExists(db, userId)

    fun get(userId: String): UserProfile? = UserProfile.Companion.getById(db, userId)

    fun save(userId: String, name: String) = UserProfileDatabase.save(db, userId, name)

    fun isNoPrivileged(): Boolean = transaction(db) {
        PrivilegedUsersDatabase.selectAll().empty()
    }

    fun grantOwner(userId: String) = transaction(db) {
        PrivilegedUsersDatabase.save(db, userId, listOf("full_access", "add_new_admin"))
    }

    fun grantPrivileges(userId: String, privileges: List<String>, isReplace: Boolean = false) = transaction(db) {
        PrivilegedUsersDatabase.save(db, userId, privileges, isReplace)
    }
}
