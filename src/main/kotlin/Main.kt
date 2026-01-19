package com.algorithmlx.astragone

import com.algorithmlx.astragone.handlers.AdminHandler
import com.algorithmlx.astragone.handlers.RegisterFlowHandler
import com.algorithmlx.astragone.handlers.StartHandler
import com.algorithmlx.astragone.utils.AdminInitServiceUtil
import com.algorithmlx.astragone.utils.RegisterCacheUtil
import com.algorithmlx.astragone.utils.UserRepositoryUtil
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.extensions.filters.Filter
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
fun main() {
    val dbFactory = DatabaseFactory(BotConfig)
    dbFactory.connect()

    val redisFactory = RedisFactory(BotConfig)
    redisFactory.connect()

    val userRepository = UserRepositoryUtil(dbFactory.database)
    val registerCache = RegisterCacheUtil(redisFactory)

    val adminService = AdminInitServiceUtil(userRepository)
    adminService.tryInit()

    val adminHandler = AdminHandler(adminService, userRepository)
    val startHandler = StartHandler(userRepository, registerCache)
    val registerHandler = RegisterFlowHandler(userRepository, registerCache)

    val bot = bot {
        token = BotConfig.botToken

        dispatch {
            command("regiser-superadmin") { adminHandler.handle(bot, message) }
            command("start") { startHandler.handle(bot, message) }
            callbackQuery("register_continue") { registerHandler.handleContinue(bot, callbackQuery) }
            callbackQuery("register_back") { registerHandler.handleBack(bot, callbackQuery) }

            message(Filter.Text) {
                if (message.chat.type == "private" && !message.from!!.isBot)
                    registerHandler.handle(bot, message)
            }
        }
    }

    bot.startPolling()
}
