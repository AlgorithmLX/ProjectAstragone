package com.algorithmlx.astragone.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
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
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

@Serializable
data class RemnaWrapper<T>(val response: T)

@Serializable
data class CreateUserRequest(
    val username: String,
    val status: UserStatus,
    val expireAt: String,
    val activeInternalSquads: List<String>
) {
    @Serializable
    enum class UserStatus {
        ACTIVE, DISABLED, LIMITED, EXPIRED
    }
}

@Serializable
data class RemnaUserResponse(
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

@Serializable
data class RemnaInternalSquadsResponse(
    val total: Int,
    val internalSquads: List<InternalSquad>
) {
    @Serializable
    data class InternalSquad(
        val uuid: String,
        val name: String
    )
}

class RemnaClient(private val baseUrl: String, private val apiToken: String, private val authKey: String = "") {
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
        activeInternalSquads: List<String>,
        onSuccess: RemnaUserResponse.() -> Unit,
        onFailure: HttpResponse.() -> Unit = {},
        onFatal: (Exception) -> Unit = {}
    ) = try {
        val expiration = Clock.System.now() + days.days

        val response = response(HttpMethod.Post, "/api/users$authKey", CreateUserRequest(
            username = username,
            expireAt = expiration.toString().substringBefore(".") + "Z",
            status = CreateUserRequest.UserStatus.ACTIVE,
            activeInternalSquads = activeInternalSquads
        ))

        if (response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK) {
            val wrapper = response.body<RemnaWrapper<RemnaUserResponse>>()
            onSuccess(wrapper.response)
        } else {
            val error = response.bodyAsText()
            println("Server Error: ${response.status} - $error")
            onFailure(response)
        }
    } catch (e: Exception) {
        onFatal(e)
        e.printStackTrace()
    }

    suspend fun getInternalSquads(
        onSuccess: RemnaInternalSquadsResponse.() -> Unit,
        onFailure: HttpResponse.() -> Unit = {},
        onFatal: (Exception) -> Unit = {}
    ) = try {
        // no body response
        val response = response<Unit>(HttpMethod.Get, "/api/internal-squads")
        if (response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK) {
            val wrapper = response.body<RemnaWrapper<RemnaInternalSquadsResponse>>()
            onSuccess(wrapper.response)
        } else {
            val error = response.bodyAsText()
            println("Server Error: ${response.status} - $error")
            onFailure(response)
        }
    } catch (e: Exception) {
        onFatal(e)
        e.printStackTrace()
    }

    private suspend fun <T> response(httpMethod: HttpMethod, path: String, body: T? = null) = client.post("$baseUrl$path") {
        method = httpMethod
        header("Authorization", "Bearer $apiToken")
        contentType(ContentType.Application.Json)
        body?.let { setBody(it) }
    }
}
