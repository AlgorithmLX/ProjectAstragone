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
    companion object: EasyGetter<UserProfile> {
        @JvmStatic
        override fun fromDatabase(db: Database): List<UserProfile> = transaction(db) {
            UserProfileDatabase.selectAll().map {
                val tgId = it[UserProfileDatabase.telegramId]
                val usName = it[UserProfileDatabase.userName]
                UserProfile(tgId, usName)
            }
        }

        @JvmStatic
        fun getById(db: Database, userId: String) = this.fromDatabase(db).firstOrNull { it.telegramId == userId }
    }
}

object UserProfileDatabase: Table() {
    val telegramId = varchar("user_id", 255).uniqueIndex()
    val userName = text("username")

    @JvmStatic
    fun save(db: Database, userId: String, userName: String) = transaction(db) {
        val exists = UserProfile.getById(db, userId) != null

        if (exists) UserProfileDatabase.update({ telegramId eq userId }) {
            it[this.telegramId] = userId
            it[this.userName] = userName
        } else UserProfileDatabase.insert {
            it[this.telegramId] = userId
            it[this.userName] = userName
        }
    }

    @JvmStatic
    fun deleteById(db: Database, userId: String) = transaction(db) { UserProfileDatabase.deleteWhere { this.telegramId eq userId } }
}
