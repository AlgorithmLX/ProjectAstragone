package com.algorithmlx.astragone

import io.lettuce.core.ClientOptions
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands

class RedisFactory(private val config: BotConfig) {
    private lateinit var client: RedisClient
    private lateinit var connection: StatefulRedisConnection<String, String>

    fun connect() {
        client = RedisClient.create(
            RedisURI.Builder.redis(config.redisHost, config.redisPort)
                .withPassword(config.redisPassword.toCharArray())
                .build()
        ).apply {
            options = ClientOptions.builder().build()
        }

        connection = client.connect()
    }

    fun <T> use(block: (commands: RedisCommands<String, String>) -> T): T = client.connect().use { connection ->
        val commands = connection.sync()
        block(commands)
    }

    fun shutdown() = client.shutdown()
}
