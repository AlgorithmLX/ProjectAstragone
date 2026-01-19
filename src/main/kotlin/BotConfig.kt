package com.algorithmlx.astragone

import com.algorithmlx.astragone.handlers.AdminHandler
import com.algorithmlx.astragone.handlers.RegisterHandler
import com.algorithmlx.astragone.handlers.StartHandler
import com.algorithmlx.astragone.utils.service.AdminInitService
import com.algorithmlx.astragone.utils.repository.RegisterRepository
import com.algorithmlx.astragone.utils.repository.UserRepository
import org.koin.dsl.module

val appModule = module {
    // Config
    single { BotConfig }

    // Factories
    single { DatabaseFactory(get()) }
    single { RedisFactory(get()) }

    // Database
    single { get<DatabaseFactory>().database }

    // Repositories
    single { UserRepository(get()) }
    single { RegisterRepository(get()) }

    // Services
    single { AdminInitService(get()) }

    // Handlers
    single { AdminHandler(get(), get()) }
    single { StartHandler(get(), get()) }
    single { RegisterHandler(get(), get()) }
}

object BotConfig {
    init {
        System.setProperty("jsse.enableSNIExtension", "false")
    }

    val String.fromEnv: String get() = System.getenv(this)
    // Environment
    // Bot Token
    val botToken = "BOT_TOKEN".fromEnv

    // Database
    val dbHost = "POSTGRES_HOST".fromEnv
    val dbUser = "POSTGRES_USER".fromEnv
    val dbPass = "POSTGRES_PASSWORD".fromEnv
    val dbPath = "POSTGRES_DB".fromEnv

    // Redis config
    private val rawRedisHost = "REDIS_HOST".fromEnv
    val redisPassword = "REDIS_PASSWORD".fromEnv
    // End of environment

    val redisHost: String
    val redisPort: Int

    init {
        var host = if (rawRedisHost.startsWith("redis://")) rawRedisHost.removePrefix("redis://") else rawRedisHost
        var port = 6379
        if (host.contains(":")) {
            val parts = host.split(":", limit = 2)
            host = parts[0]
            port = parts[1].toIntOrNull() ?: port
        }

        redisHost = host
        redisPort = port
    }
}