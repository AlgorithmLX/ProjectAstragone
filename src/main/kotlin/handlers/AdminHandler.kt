package com.algorithmlx.astragone.handlers

import com.algorithmlx.astragone.utils.AdminInitServiceUtil
import com.algorithmlx.astragone.utils.UserRepositoryUtil
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Message

class AdminHandler(
    private val adminService: AdminInitServiceUtil,
    private val userRepository: UserRepositoryUtil
) {
    fun handle(bot: Bot, message: Message) {
        val user = message.from ?: return
        val text = message.text?.removePrefix("/register-superadmin")?.trim() ?: return

        if (!adminService.isKeyActive()) return
        if (!adminService.isKeyValid(text)) return

        val send = bot.sendMessage(ChatId.fromId(message.chat.id), "Инициализация административных прав...").get()

        println("YES, I FOUND U, MY CREATOR!")
        println("Nice to meet you, ${user.firstName} ${user.lastName} [${user.username}] (${user.id})!")
        userRepository.grantOwner(user.id.toString())
        adminService.invalidate()

        bot.editMessageText(
            chatId = ChatId.fromId(message.chat.id),
            messageId = send.messageId,
            text = "Административные права инициализированы. Вам выдан полный функционал бота, ${user.firstName}"
        )
    }
}