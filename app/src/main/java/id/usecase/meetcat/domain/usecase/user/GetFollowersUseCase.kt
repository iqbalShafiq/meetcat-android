package id.usecase.meetcat.domain.usecase.user

import id.usecase.meetcat.domain.model.User
import id.usecase.meetcat.domain.repository.UserRepository

class GetFollowersUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, page: Int = 0): Result<List<User>> {
        return userRepository.getFollowers(userId, page, PAGE_SIZE)
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}
