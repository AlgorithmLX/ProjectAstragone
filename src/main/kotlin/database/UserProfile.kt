package com.algorithmlx.astragone.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

data class UserProfile(
    val telegramId: String,
    val userName: String
) {
    companion object {
        @JvmStatic
        fun getById(db: Database, userId: String) = transaction(db) {
            UserProfileDatabase.select(UserProfileDatabase.telegramId)
                .where { UserProfileDatabase.telegramId eq userId }
                .map { UserProfile(it[UserProfileDatabase.telegramId], it[UserProfileDatabase.userName]) }
                .singleOrNull()
        }

        @JvmStatic
        fun isExists(db: Database, userId: String): Boolean = this.getById(db, userId) != null
    }
}

object UserProfileDatabase: Table() {
    val telegramId = varchar("user_id", 255).uniqueIndex()
    val userName = text("username")

    @JvmStatic
    fun save(db: Database, userId: String, userName: String) = transaction(db) {
        if (UserProfile.isExists(db, userId)) UserProfileDatabase.update({ telegramId eq userId }) {
            it[this.telegramId] = userId
            it[this.userName] = userName
        } else UserProfileDatabase.insert {
            it[this.userName] = userName
        }
    }

    @JvmStatic
    fun deleteById(db: Database, userId: String) = transaction(db) { UserProfileDatabase.deleteWhere { this.telegramId eq userId } }
}
