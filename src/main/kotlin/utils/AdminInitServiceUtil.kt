package com.algorithmlx.astragone.utils

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class AdminInitServiceUtil(private val userRepository: UserRepositoryUtil) {
    private var accessKey = ""

    @OptIn(ExperimentalUuidApi::class)
    fun tryInit() {
        if (userRepository.isNoPrivileged()) {
            accessKey = Uuid.random().toHexString()
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