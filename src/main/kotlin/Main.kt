package com.algorithmlx.astragone

import com.algorithmlx.astragone.database.PrivilegedUsersDatabase
import com.algorithmlx.astragone.database.UserProfile
import com.algorithmlx.astragone.database.UserProfileDatabase
import com.algorithmlx.astragone.redis.cache.UserRegister
import com.algorithmlx.astragone.utils.useRedis
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import io.lettuce.core.ClientOptions
import io.lettuce.core.MaintNotificationsConfig
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

val String.fromEnv: String get() = System.getenv(this)

@OptIn(ExperimentalUuidApi::class)
fun main() {
    System.setProperty("jsse.enableSNIExtension", "false")

    // Environment
    val botToken = "BOT_TOKEN".fromEnv
    val dbHost = "POSTGRES_HOST".fromEnv
    val dbUser = "POSTGRES_USER".fromEnv
    val dbPass = "POSTGRES_PASSWORD".fromEnv
    val dbPath = "POSTGRES_DB".fromEnv
    val redisPassword = "REDIS_PASSWORD".fromEnv
    val redisHost = "REDIS_HOST".fromEnv
    // End of environment

    var redisHostName = if (redisHost.startsWith("redis://")) redisHost.removePrefix("redis://") else redisHost
    var redisPort = 6379
    if (redisHostName.contains(":")) {
        redisPort = redisHostName.split(":", limit = 2)[1].toIntOrNull() ?: redisPort
        redisHostName = redisHostName.split(":", limit = 2)[0]
    }

    val database = Database.connect(
        "jdbc:${dbHost}/$dbPath",
        driver = "org.postgresql.Driver",
        user = dbUser,
        password = dbPass
    )

    transaction(database) {
        SchemaUtils.create(
            PrivilegedUsersDatabase,
            UserProfileDatabase
        )
    }

    val redisClient = RedisClient.create(
        RedisURI.Builder.redis(redisHostName, redisPort)
            .withPassword(redisPassword.toCharArray())
            .build()
    ).apply {
        options = ClientOptions.builder().maintNotificationsConfig(MaintNotificationsConfig.disabled()).build()
    }
    val redisConnect = redisClient.connect()

    var accessKey = ""
    if (transaction(database) { PrivilegedUsersDatabase.selectAll().empty() }) {
        accessKey = Uuid.random().toHexString()
        println("Hello everyone?")
        println("Somebody, who are my owner?")
        println("Please, found!")
        println("Execute command: /registerconsolesuperadmin $accessKey")
    }

    val bot = bot {
        token = botToken

        dispatch {
            command("registerconsolesuperadmin") {
                val chatId = message.chat.id
                val user = message.from ?: return@command
                if (user.isBot) return@command
                if (message.chat.permissions != null) return@command
                if (accessKey.isEmpty()) return@command
                val text = this.message.text?.removePrefix("/registerconsolesuperadmin")?.trimStart() ?: return@command

                if (accessKey.isNotEmpty() && text != accessKey) return@command

                val sended = bot.sendMessage(chatId, "Инициализация административных прав...").first?.body()?.result

                println("YES, I FOUND U, MY CREATOR!")
                println("Nice to meet you, ${user.firstName} ${user.lastName} [${user.username}] (${user.id})!")

                PrivilegedUsersDatabase.save(
                    database,
                    user.id.toString(),
                    listOf("full_access", "add_new_admin")
                )

                sended?.messageId?.let {
                    bot.editMessageText(
                        chatId,
                        it,
                        text = "Административные права инициализированы. Вам выдан полный функционал бота, ${user.firstName}"
                    )
                }
                accessKey = ""
            }
            command("start") {
                val chatId = message.chat.id
                val user = message.from ?: return@command
                if (user.isBot) return@command
                if (message.chat.type != "private") {
                    bot.sendMessage(chatId, "Бота можно использовать только в личных сообщениях.")
                    return@command
                }

                if (!UserProfile.isExists(database, user.id.toString())) {
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

                    val keyboardButtons = mutableListOf<List<KeyboardButton>>()

                    keyboardButtons += if (user.lastName != null) {
                        listOf(KeyboardButton(user.firstName), KeyboardButton("${user.firstName} ${user.lastName}"))
                    } else listOf(KeyboardButton(user.firstName))

                    if (user.username != null) keyboardButtons += listOf(KeyboardButton(user.username!!))

                    bot.sendMessage(
                        chatId,
                        """
                            Для начала, представьтесь. Как вас зовут? 
                            Имеется ввиду не ваше имя, а имя вашего аккаунта, как будет называться в системе.
                            
                            Учтите, потом сменить имя не получится.
                            
                            Введите имя пользователя или выберите из списка:
                        """.trimIndent(),
                        replyMarkup = KeyboardReplyMarkup(
                            keyboardButtons,
                            resizeKeyboard = true,
                            oneTimeKeyboard = true
                        )
                    )

                    useRedis(redisClient) { commands ->
                        val cache = UserRegister(commands)
                        cache.set(chatId.toString(), UserRegister.Data(user.id.toString(), null)) // null because yes
                    }

                    return@command
                }

                val userProfile = UserProfile.getById(database, user.id.toString()) ?: return@command
                bot.sendMessage(
                    chatId,
                    """
                        ${userProfile.userName}, возвращением!
                        Что хотите сделать?
                    """.trimIndent(),
                    replyMarkup = InlineKeyboardMarkup.create(
                        listOf(
                            listOf(
                                InlineKeyboardButton.CallbackData("\uD83D\uDCB3 Купить подписку", "buy_subscription"),
                                InlineKeyboardButton.CallbackData("Профиль", "profile")
                            ),
                            listOf(InlineKeyboardButton.CallbackData("🆘 Помощь", "contact_support"))
                        )
                    )
                )
            }

            callbackQuery("register_continue") {
                val user = this.callbackQuery.message?.chat ?: return@callbackQuery
                val chatId = user.id
                val messageId = this.callbackQuery.message?.messageId ?: return@callbackQuery
                val userName = useRedis(redisClient) { commands ->
                    val cache = UserRegister(commands)
                    val data = cache.get(chatId.toString()) { UserRegister.Data("", null) }
                    val userName = data.userName
                    cache.invalidate(chatId.toString())
                    return@useRedis userName
                }

                if (userName == null) {
                    val keyboardButtons = mutableListOf<List<KeyboardButton>>()
                    keyboardButtons += if (user.lastName != null) {
                        listOf(KeyboardButton(user.firstName!!), KeyboardButton("${user.firstName} ${user.lastName}"))
                    } else listOf(KeyboardButton(user.firstName!!))

                    if (user.username != null) keyboardButtons += listOf(KeyboardButton(user.username!!))

                    useRedis(redisClient) { commands ->
                        val cache = UserRegister(commands)
                        val data = cache.get(chatId.toString()) { UserRegister.Data("", null) }
                        cache.set(chatId.toString(), UserRegister.Data(data.userId, null))
                    }

                    bot.deleteMessage(chatId, messageId)
                    bot.sendMessage(
                        chatId = chatId,
                        text = """
                            Произошла ошибка во время получения вашего имени. Такое может произойти, в случае технических работ или отказе сервера данных.
                            Просто повторите попытку еще раз, либо обратитесь в техническую поддержку.
                            
                            Введите ваше имя повторно:
                        """.trimIndent(),
                        replyMarkup = KeyboardReplyMarkup(
                            keyboardButtons,
                            resizeKeyboard = true,
                            oneTimeKeyboard = true
                        )
                    )

                    return@callbackQuery
                }

                UserProfileDatabase.save(database, user.id.toString(), userName)

                bot.sendMessage(
                    chatId,
                    """
                        ${userName}, добро пожаловать!
                        Что хотите сделать?
                    """.trimIndent(),
                    replyMarkup = InlineKeyboardMarkup.create(
                        listOf(
                            listOf(
                                InlineKeyboardButton.CallbackData("\uD83D\uDCB3 Купить подписку", "buy_subscription"),
                                InlineKeyboardButton.CallbackData("Профиль", "profile")
                            ),
                            listOf(InlineKeyboardButton.CallbackData("🆘 Помощь", "contact_support"))
                        )
                    )
                )
            }

            callbackQuery("register_back") {
                val chatId = this.callbackQuery.message?.chat?.id ?: return@callbackQuery
                val messageId = this.callbackQuery.message ?: return@callbackQuery
                val user = this.callbackQuery.message?.chat ?: return@callbackQuery
                val keyboardButtons = mutableListOf<List<KeyboardButton>>()

                keyboardButtons += if (user.lastName != null) {
                    listOf(KeyboardButton(user.firstName!!), KeyboardButton("${user.firstName} ${user.lastName}"))
                } else listOf(KeyboardButton(user.firstName!!))

                if (user.username != null) keyboardButtons += listOf(KeyboardButton(user.username!!))

                bot.deleteMessage(chatId, messageId.messageId)
                bot.sendMessage(
                    chatId,
                    """
                            📩 Введите имя пользователя или выберите из списка:
                        """.trimIndent(),
                    replyMarkup = KeyboardReplyMarkup(
                        keyboardButtons,
                        resizeKeyboard = true,
                        oneTimeKeyboard = true
                    )
                )
            }

            message {
                val chat = message.chat
                val chatId = chat.id
                val user = message.from ?: return@message
                if (chat.type != "private") return@message
                if (user.isBot) return@message
                if (UserProfile.isExists(database, user.id.toString())) return@message

                useRedis(redisClient) { commands ->
                    val cache = UserRegister(commands)
                    val data = cache.get(chatId.toString()) { UserRegister.Data("", null) }
                    if (data.userId.isEmpty()) return@useRedis

                    val text = message.text
                    if (text == null) {
                        val keyboardButtons = mutableListOf<List<KeyboardButton>>()

                        keyboardButtons += if (user.lastName != null) {
                            listOf(KeyboardButton(user.firstName), KeyboardButton("${user.firstName} ${user.lastName}"))
                        } else listOf(KeyboardButton(user.firstName))

                        if (user.username != null) keyboardButtons += listOf(KeyboardButton(user.username!!))

                        bot.sendMessage(
                            chatId,
                            """
                                📩 Введите имя пользователя или выберите из списка:
                            """.trimIndent(),
                            replyMarkup = KeyboardReplyMarkup(
                                keyboardButtons,
                                resizeKeyboard = true,
                                oneTimeKeyboard = true
                            )
                        )

                        return@useRedis
                    }

                    if (text.startsWith("/")) return@useRedis
                    cache.set(chatId.toString(), UserRegister.Data(data.userId, text))

                    bot.sendMessage(
                        chatId,
                        """
                            ${text}, вы уверены в своем выборе?
                            После подтверждения имя пользователя нельзя будет изменить.
                        """.trimIndent(),
                        replyMarkup = InlineKeyboardMarkup.create(
                            listOf(
                                listOf(
                                    InlineKeyboardButton.CallbackData("✅ Да, я уверен!", "register_continue"),
                                    InlineKeyboardButton.CallbackData("❌ Нет, я хочу сменить имя.", "register_back")
                                )
                            )
                        )
                    )
                }
            }
        }
    }

    bot.startPolling()
}
