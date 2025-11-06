package id.usecase.meetcat.domain.usecase.auth

import id.usecase.meetcat.domain.model.AuthUser
import id.usecase.meetcat.domain.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<AuthUser> {
        return authRepository.login(email, password)
    }
}
