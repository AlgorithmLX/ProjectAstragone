package com.algorithmlx.astragone.utils

import io.lettuce.core.RedisClient
import io.lettuce.core.api.sync.RedisCommands

fun <T> useRedis(redis: RedisClient, block: (commands: RedisCommands<String, String>) -> T): T = redis.connect().use { connection ->
    val commands = connection.sync()
    block(commands)
}