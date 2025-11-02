package id.usecase.meetcat.domain.usecase.post

import id.usecase.meetcat.domain.repository.PostRepository

class LovePostUseCase(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(postId: String): Result<Unit> {
        return postRepository.lovePost(postId)
    }
}
