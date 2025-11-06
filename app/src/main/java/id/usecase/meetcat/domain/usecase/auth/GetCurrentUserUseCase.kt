package id.usecase.meetcat.domain.usecase.auth

import id.usecase.meetcat.domain.model.AuthUser
import id.usecase.meetcat.domain.repository.AuthRepository

class GetCurrentUserUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): AuthUser? {
        return authRepository.getCurrentUser()
    }
}
