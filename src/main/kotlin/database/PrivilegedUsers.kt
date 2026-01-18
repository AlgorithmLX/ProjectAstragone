package com.algorithmlx.astragone.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.TextColumnType
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

data class PrivilegedUsers(val userId: String, val privileges: List<String>) {
    companion object {
        @JvmStatic
        fun getById(db: Database, userId: String) = transaction(db) {
            PrivilegedUsersDatabase.select(PrivilegedUsersDatabase.userId)
                .where { PrivilegedUsersDatabase.userId eq userId }
                .map { PrivilegedUsers(it[PrivilegedUsersDatabase.userId], it[PrivilegedUsersDatabase.privileges]) }
                .singleOrNull()
        }

        @JvmStatic
        fun isExists(db: Database, userId: String): Boolean = this.getById(db, userId) != null
    }
}

object PrivilegedUsersDatabase: Table() {
    val userId = varchar("user_id", 255).uniqueIndex()
    val privileges = array("privileges", TextColumnType())

    // if isReplace == false -> append
    @JvmStatic
    fun save(db: Database, userId: String, privileges: List<String>, isReplace: Boolean = false) = transaction(db) {
        if (!PrivilegedUsers.isExists(db, userId))
            PrivilegedUsersDatabase.insert {
                it[this.userId] = userId
                it[this.privileges] = privileges
            }
        else {
            PrivilegedUsersDatabase.update({ PrivilegedUsersDatabase.userId eq userId }) {
                if (isReplace) it[this.privileges] = privileges
                else {
                    PrivilegedUsers.getById(db, userId)?.let { let ->
                        val current = let.privileges.toMutableList()
                        privileges.forEach { fe ->
                            if (current.contains(fe)) return@forEach
                            current += fe
                        }

                        it[this.privileges] = current
                    }
                }
            }
        }
    }

    @JvmStatic
    fun delete(db: Database, userId: String) = transaction(db) { PrivilegedUsersDatabase.deleteWhere { this.userId eq userId } }
}
