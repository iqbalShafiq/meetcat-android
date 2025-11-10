package id.usecase.meetcat.data.remote.datasource

import id.usecase.meetcat.data.remote.dto.ApiResponse
import id.usecase.meetcat.data.remote.dto.AuthUserDto
import id.usecase.meetcat.data.remote.dto.LoginRequest
import id.usecase.meetcat.data.remote.dto.MessageResponse
import id.usecase.meetcat.data.remote.dto.RegisterRequest
import id.usecase.meetcat.data.remote.dto.ResetPasswordRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class AuthRemoteDataSource(
    private val httpClient: HttpClient
) {

    suspend fun login(email: String, password: String): ApiResponse<AuthUserDto> {
        return httpClient.post("/auth/login") {
            setBody(LoginRequest(email, password))
        }.body()
    }

    suspend fun register(
        email: String,
        username: String,
        displayName: String,
        password: String
    ): ApiResponse<AuthUserDto> {
        return httpClient.post("/auth/register") {
            setBody(RegisterRequest(email, username, displayName, password))
        }.body()
    }

    suspend fun resetPassword(email: String): ApiResponse<MessageResponse> {
        return httpClient.post("/auth/reset-password") {
            setBody(ResetPasswordRequest(email))
        }.body()
    }

    suspend fun getCurrentUser(): ApiResponse<AuthUserDto> {
        return httpClient.get("/auth/current-user").body()
    }

    suspend fun logout(): ApiResponse<Unit> {
        return httpClient.post("/auth/logout").body()
    }
}
