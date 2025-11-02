package id.usecase.meetcat.domain.usecase.post

import id.usecase.meetcat.domain.repository.PostRepository

class UnlovePostUseCase(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(postId: String): Result<Unit> {
        return postRepository.unlovePost(postId)
    }
}
