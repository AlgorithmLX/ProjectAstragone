package com.algorithmlx.astragone

import com.algorithmlx.astragone.database.PrivilegedUsersDatabase
import com.algorithmlx.astragone.database.UserProfileDatabase
import io.lettuce.core.api.sync.RedisCommands
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class DatabaseFactory(private val config: BotConfig) {
    lateinit var database: Database

    fun connect() {
        database = Database.connect(
            "jdbc:postgresql://${config.dbHost}/${config.dbPath}",
            driver = "org.postgresql.Driver",
            user = config.dbUser,
            password = config.dbPass
        )

        transaction(database) {
            SchemaUtils.create(PrivilegedUsersDatabase, UserProfileDatabase)
        }
    }
}
