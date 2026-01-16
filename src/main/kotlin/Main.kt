package com.algorithmlx.astragone

//8481878513:AAH2CU8iEhABAtK2fis7Lja4Ajzql5388Xk
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

val String.fromEnv: String get() = System.getenv(this)

fun main() {
    System.setProperty("jsse.enableSNIExtension", "false")

    // Environment
    val botToken = "BOT_TOKEN".fromEnv
    val dbHost = "POSTGRES_HOST".fromEnv
    val dbUser = "POSTGRES_USER".fromEnv
    val dbPass = "POSTGRES_PASSWORD".fromEnv
    val dbPath = "POSTGRES_DB".fromEnv
    val redisPassword = "REDIS_PASSWORD".fromEnv
    val redisUser = "REDIS_USER".fromEnv
    // End of environment

    val database = Database.connect(
        "jdbc:${dbHost}/$dbPath",
        driver = "org.postgresql.Driver",
        user = dbUser,
        password = dbPass
    )

    transaction(database) {
        SchemaUtils.create<Table>()
    }

    val redisClient = RedisClient.create(
        RedisURI.builder()
            .withAuthentication(redisUser, redisPassword)
            .build()
    )

    val bot = bot {
        token = botToken

        dispatch {
            command("start") {
                val chatId = message.chat.id
                bot.sendMessage(
                    chatId,
                    """
                        🟣 Project Astragone
                        
                        🌍 Добро пожаловать в сеть. Мы предоставляем защищенный туннель для доступа к информации.
                        
                        ❓ **Почему Astragone?**
                        Наше главное слово - приватность. Мы не храним выши данные. Они существуют только в момент передачи.
                        Стабильная скорость потока для стриминга и работы.
                    """.trimIndent(),
                    replyMarkup = InlineKeyboardMarkup.create(
                        listOf(
                            listOf(InlineKeyboardButton.CallbackData("\uD83D\uDCB3 Купить подписку", "buy_subscription")),
                            listOf(InlineKeyboardButton.CallbackData("🆘 Помощь", "contact_support"))
                        )
                    )
                )
            }
        }
    }

    bot.startPolling()
}
