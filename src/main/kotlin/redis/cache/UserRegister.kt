package com.algorithmlx.astragone.redis.cache

import com.algorithmlx.astragone.redis.RedisCache
import io.lettuce.core.api.sync.RedisCommands

class UserRegister(override val commands: RedisCommands<String, String>) : RedisCache<UserRegister.Data> {
    private val String.userIdKey get() = "user_registration:$this:user_id"
    private val String.userNameKey get() = "user_registration:$this:user_name"

    override fun invalidate(key: String) {
        commands.del(key.userIdKey)
        commands.del(key.userNameKey)
    }

    override fun get(
        key: String,
        orDefault: () -> Data
    ): Data {
        val userIdK = key.userIdKey
        val userNameK = key.userNameKey

        val userId = commands[userIdK]
        val userName = commands[userNameK]

        if (userId == null) return orDefault()
        return Data(userId, userName)
    }

    override fun set(key: String, value: Data) {
        val userId = key.userIdKey
        val userName = key.userNameKey
        commands.set(userId, userId)
        if (value.userName != null)
            commands.set(userName, value.userName)
    }

    data class Data(val userId: String, val userName: String?)
}
