package id.usecase.meetcat.domain.usecase.user

import id.usecase.meetcat.domain.model.FeedItem
import id.usecase.meetcat.domain.repository.UserRepository

class GetUserRepliesUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, page: Int = 0): Result<List<FeedItem.ReplyItem>> {
        return userRepository.getUserReplies(userId, page, PAGE_SIZE)
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}
