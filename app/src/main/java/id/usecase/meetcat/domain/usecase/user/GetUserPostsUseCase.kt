package id.usecase.meetcat.domain.usecase.user

import id.usecase.meetcat.domain.model.Post
import id.usecase.meetcat.domain.repository.UserRepository

class GetUserPostsUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, page: Int = 0): Result<List<Post>> {
        return userRepository.getUserPosts(userId, page, PAGE_SIZE)
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}
