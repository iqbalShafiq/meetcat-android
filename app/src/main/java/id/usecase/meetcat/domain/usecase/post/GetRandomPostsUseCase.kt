package id.usecase.meetcat.domain.usecase.post

import id.usecase.meetcat.domain.model.Post
import id.usecase.meetcat.domain.repository.PostRepository

class GetRandomPostsUseCase(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(count: Int = DEFAULT_COUNT): Result<List<Post>> {
        return postRepository.getRandomPosts(count)
    }

    companion object {
        private const val DEFAULT_COUNT = 50
    }
}
