package com.algorithmlx.astragone.redis

import io.lettuce.core.api.sync.RedisCommands

interface RedisCache<V> {
    val commands: RedisCommands<String, String>
    fun invalidate(key: String)
    fun get(key: String, orDefault: () -> V): V
    fun set(key: String, value: V)
}
