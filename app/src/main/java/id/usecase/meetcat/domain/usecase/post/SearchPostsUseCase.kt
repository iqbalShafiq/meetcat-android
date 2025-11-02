package id.usecase.meetcat.domain.usecase.post

import id.usecase.meetcat.domain.model.Post
import id.usecase.meetcat.domain.repository.PostRepository

class SearchPostsUseCase(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(
        query: String,
        page: Int = 0
    ): Result<List<Post>> {
        if (query.isBlank()) {
            return Result.success(emptyList())
        }
        return postRepository.searchPosts(query, page, PAGE_SIZE)
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}
