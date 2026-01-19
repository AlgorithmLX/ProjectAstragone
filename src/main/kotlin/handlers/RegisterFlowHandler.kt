package com.algorithmlx.astragone.handlers

import com.algorithmlx.astragone.utils.RegisterCacheUtil
import com.algorithmlx.astragone.utils.UserRepositoryUtil
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.CallbackQuery
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton

class RegisterFlowHandler(
    private val userRepo: UserRepositoryUtil,
    private val registerCache: RegisterCacheUtil
): RegisterKeyboardHandler() {
    fun handle(bot: Bot, message: Message) {
        val user = message.from ?: return
        val chatId = ChatId.fromId(message.chat.id)
        val text = message.text ?: return

        if (userRepo.exists(user.id.toString())) return

        val cachedData = registerCache.getData(chatId.id.toString())
        if (cachedData.userId.isEmpty()) return

        if (!nameValidator.matches(text)) {
            bot.sendMessage(
                chatId,
                """
                    ⚠️ **Недопустимый формат имени.**
                    
                    Имя пользователя может содержать **только**:
                    - Латинские буквы (A-Z, a-z)
                    - Цифры (0-9)
                    - Символы: _ (нижнее подчеркивание) и - (дефис)
                    
                    Пробелы и кириллица запрещены. Попробуйте снова:
                """.trimIndent()
            )

            return
        }

        registerCache.saveState(chatId.id.toString(), cachedData.userId, text)
        bot.sendMessage(
            chatId,
            """
                ${text}, вы уверены в своем выборе?
                После подтверждения имя пользователя нельзя будет изменить.
            """.trimIndent(),
            replyMarkup = InlineKeyboardMarkup.create(listOf(listOf(
                InlineKeyboardButton.CallbackData("✅ Да, я уверен!", "register_continue"),
                InlineKeyboardButton.CallbackData("❌ Нет, я хочу сменить имя.", "register_back")
            )))
        )
    }

    fun handleContinue(bot: Bot, query: CallbackQuery) {
        val chatId = query.message?.chat?.id?.let { ChatId.fromId(it) } ?: return
        val messageId = query.message!!.messageId

        val data = registerCache.getData(chatId.id.toString())
        val name = data.userName

        if (name == null) {
            bot.sendMessage(
                chatId,
                """
                       Произошла ошибка во время получения вашего имени. Такое может произойти, в случае технических работ или отказе сервера данных.
                       Просто повторите попытку еще раз, либо обратитесь в техническую поддержку.
                       
                       Введите ваше имя повторно:
                       """.trimIndent(),
                replyMarkup = this.getKeyboard(query.from)
            )
            return
        }

        bot.editMessageText(
            chatId,
            messageId,
            text = """
            🟣 Project Astragone
                    
            $name, добро пожаловать!
            Выберите действие из меню ниже.
            """.trimIndent(),
            replyMarkup = InlineKeyboardMarkup.create(listOf(listOf(
                InlineKeyboardButton.CallbackData("\uD83D\uDCBC Профиль", "profile"),
                InlineKeyboardButton.CallbackData("🆘 Помощь", "support")
            )))
        )
    }

    fun handleBack(bot: Bot, query: CallbackQuery) {
        val message = query.message

        message?.let {
            val chatId = ChatId.fromId(it.chat.id)
            bot.deleteMessage(chatId, it.messageId)
            bot.sendMessage(chatId, "\uD83D\uDCE9 Введите имя пользователя или выберите из списка:", replyMarkup = this.getKeyboard(query.from))
        }

    }
}