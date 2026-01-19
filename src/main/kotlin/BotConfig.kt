package com.algorithmlx.astragone

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