package com.algorithmlx.astragone.utils.repository

import com.algorithmlx.astragone.RedisFactory
import com.algorithmlx.astragone.redis.cache.UserRegister

class RegisterRepository(private val redis: RedisFactory) {
    fun saveState(chatId: String, userId: String, name: String?) {
        redis.use { commands ->
            val cache = UserRegister(commands)
            cache.set(chatId, UserRegister.Data(userId, name))
        }
    }

    fun getData(chatId: String): UserRegister.Data = redis.use { commands ->
        UserRegister(commands).get(chatId) { UserRegister.Data("", null) }
    }

    fun clear(chatId: String) = redis.use { commands ->
        UserRegister(commands).invalidate(chatId)
    }
}
