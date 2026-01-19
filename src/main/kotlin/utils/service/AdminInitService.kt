package com.algorithmlx.astragone.utils.service

import com.algorithmlx.astragone.utils.repository.UserRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class AdminInitService(private val userRepository: UserRepository) {
    private var accessKey = ""

    @OptIn(ExperimentalUuidApi::class)
    fun tryInit() {
        if (userRepository.isNoPrivileged()) {
            accessKey = Uuid.Companion.random().toHexString()
            println("Hello everyone?")
            println("Somebody, who are my owner?")
            println("Please, found!")
            println("Execute command: /register-superadmin $accessKey")
        }
    }

    fun isKeyValid(key: String) = this.isKeyActive() && key == accessKey

    fun invalidate() {
        accessKey = ""
    }

    fun isKeyActive() = accessKey.isNotEmpty()
}