package id.usecase.meetcat.domain.usecase.auth

import id.usecase.meetcat.domain.model.AuthUser
import id.usecase.meetcat.domain.repository.AuthRepository

class RegisterUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        username: String,
        displayName: String,
        password: String
    ): Result<AuthUser> {
        return authRepository.register(email, username, displayName, password)
    }
}
