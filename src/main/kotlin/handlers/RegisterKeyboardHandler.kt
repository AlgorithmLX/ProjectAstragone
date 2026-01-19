package com.algorithmlx.astragone.handlers

import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.User
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton

open class RegisterKeyboardHandler {
    protected val nameValidator = Regex("^[a-zA-Z0-9_-]+$")

    protected fun getKeyboard(user: User): KeyboardReplyMarkup {
        val buttons = mutableListOf<List<KeyboardButton>>()
        val buttonName = if (user.lastName != null) {
            val firstName = user.firstName
            val lastName = user.lastName!!
            var validName = ""
            if (nameValidator.matches(firstName)) validName += firstName

            if (nameValidator.matches(lastName)) validName += " $lastName"

            validName.replace(" ", "_")
        } else ""

        val firstPair = mutableListOf<KeyboardButton>()
        val lastPair = mutableListOf<KeyboardButton>()

        if (nameValidator.matches(user.firstName))
            firstPair += KeyboardButton(user.firstName)

        if (buttonName.isNotEmpty())
            firstPair += KeyboardButton(buttonName)

        user.username?.let {
            if (!nameValidator.matches(it)) return@let

            if (firstPair.size <= 1) firstPair += KeyboardButton(it)
            else lastPair += KeyboardButton(it)
        }

        if (firstPair.isNotEmpty())
            buttons += firstPair

        if (lastPair.isNotEmpty())
            buttons += lastPair

        return KeyboardReplyMarkup(buttons, resizeKeyboard = true, oneTimeKeyboard = true)
    }
}