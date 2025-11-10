package id.usecase.meetcat.data.network

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {

    // TODO: Change this to your actual backend URL when deploying
    // For localhost testing from Android emulator, use 10.0.2.2
    // For localhost testing from physical device, use your computer's IP address
    private const val BASE_URL = "http://10.0.2.2:3210"

    fun create(tokenProvider: () -> String?): HttpClient {
        return HttpClient(OkHttp) {
            // JSON Configuration
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }

            // Logging
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("HttpClient", message)
                    }
                }
                level = LogLevel.ALL
            }

            // Default Request Configuration
            defaultRequest {
                url(BASE_URL)
                contentType(ContentType.Application.Json)

                // Add authorization header if token exists
                tokenProvider()?.let { token ->
                    header("Authorization", "Bearer $token")
                }
            }
        }
    }
}
