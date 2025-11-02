package id.usecase.meetcat.domain.usecase.post

import id.usecase.meetcat.domain.repository.PostRepository

class LoveReplyUseCase(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(replyId: String): Result<Unit> {
        return postRepository.loveReply(replyId)
    }
}
