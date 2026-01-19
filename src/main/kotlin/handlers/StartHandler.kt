package com.algorithmlx.astragone.handlers

import com.algorithmlx.astragone.utils.RegisterCacheUtil
import com.algorithmlx.astragone.utils.UserRepositoryUtil
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.User

class StartHandler(
    private val userRepository: UserRepositoryUtil,
    private val registerCache: RegisterCacheUtil
): RegisterKeyboardHandler() {
    fun handle(bot: Bot, message: Message) {
        val user = message.from ?: return
        val chatId = ChatId.fromId(message.chat.id)

        if (message.chat.type != "private") {
            bot.sendMessage(chatId, "Бота можно испольщовать только в личных сообщениях")
            return
        }

        if (userRepository.exists(user.id.toString())) showMainMenu(bot, chatId, user.id.toString())
        else startRegister(bot, chatId, user)
    }

    private fun showMainMenu(bot: Bot, chatId: ChatId, userId: String){
        val profile = userRepository.get(userId) ?: return
        bot.sendMessage(
            chatId,
            """
                🟣 Project Astragone
                
                ${profile.userName}, с возвращением!
                Выберите действие из меню ниже.
            """.trimIndent(),

        )
    }

    private fun startRegister(bot: Bot, chatId: ChatId, user: User) {
        bot.sendMessage(
            chatId,
            """
                Добро пожаловать в 🟣 Project Astragone, незнакомец.
                
                🌍 Мы предоставляем защищенный туннель для доступа к информации.
                
                ❓ Почему Astragone?
                Наше главное слово - приватность. Мы не храним выши данные. Они существуют только в момент передачи.
                Мы предоставляем стабильную скорость потока для стриминга и работы.
            """.trimIndent()
        )

        bot.sendMessage(
            chatId,
            """
                Для начала, представьтесь. Как вас зовут? 
                Имеется ввиду не ваше имя, а имя вашего аккаунта, как будет называться в системе.
                
                Учтите, потом сменить имя не получится.
                
                Введите имя пользователя или выберите из списка:
            """.trimIndent(),
            replyMarkup = this.getKeyboard(user)
        )

        registerCache.saveState(chatId.toString(), user.id.toString(), null)
    }
}