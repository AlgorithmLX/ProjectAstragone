package com.algorithmlx.astragone

import org.koin.core.context.startKoin

fun main() {
    startKoin { modules(appModule) }
    BotStarter.start()
}
