package com.algorithmlx.astragone.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.toJavaInstant

@Serializable
data class RemnaWrapper(val response: RemnaResponse)

@Serializable
data class CreateUserRequest(
    val username: String,
    val status: String,
    val expireAt: String
)

@Serializable
data class RemnaResponse(
    val uuid: String,
    val id: Int,
    val username: String,
    val expireAt: String,
    val telegramId: Int? = null,
    val email: String? = null,
    val description: String? = null,
    val tag: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val subscriptionUrl: String,
)

class RemnaClient(private val baseUrl: String, private val apiToken: String, private val authKey: String) {
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }

        install(Logging) {
            level = LogLevel.ALL
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun createSubscription(
        username: String,
        days: Int,
        onSuccess: RemnaResponse.() -> Unit,
        onFailure: HttpResponse.() -> Unit = {},
        onFatal: (Exception) -> Unit = {}
    ) = try {
        val expiration = Clock.System.now() + days.days

        val response = client.post("$baseUrl/api/users${authKey}") {
            header("Authorization", "Bearer $apiToken")
            contentType(ContentType.Application.Json)
            setBody(CreateUserRequest(
                username = username,
                expireAt = expiration.toString().substringBefore(".") + "Z",
                status = "ACTIVE"
            ))
        }

        if (response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK) {
            val wrapper = response.body<RemnaWrapper>()
            onSuccess(wrapper.response)
        }
        else {
            val error = response.bodyAsText()
            println("Server Error: ${response.status} - $error")
            onFailure(response)

        }
    } catch (e: Exception) {
        onFatal(e)
        e.printStackTrace()
    }
}
