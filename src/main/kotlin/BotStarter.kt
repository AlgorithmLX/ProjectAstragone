package com.algorithmlx.astragone

import com.algorithmlx.astragone.handlers.AdminHandler
import com.algorithmlx.astragone.handlers.RegisterHandler
import com.algorithmlx.astragone.handlers.StartHandler
import com.algorithmlx.astragone.utils.service.AdminInitService
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.extensions.filters.Filter
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object BotStarter: KoinComponent {
    private val config: BotConfig by inject()

    private val dbFactory: DatabaseFactory by inject()
    private val redisFactory: RedisFactory by inject()
    private val adminService: AdminInitService by inject()

    private val adminHandler: AdminHandler by inject()
    private val startHandler: StartHandler by inject()
    private val registerHandler: RegisterHandler by inject()

    fun start() {
        dbFactory.connect()
        redisFactory.connect()

        adminService.tryInit()

        val bot = bot {
            token = config.botToken

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
}
