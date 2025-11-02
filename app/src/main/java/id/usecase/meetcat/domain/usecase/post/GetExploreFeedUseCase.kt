package id.usecase.meetcat.domain.usecase.post

import id.usecase.meetcat.domain.model.FeedItem
import id.usecase.meetcat.domain.repository.PostRepository

class GetExploreFeedUseCase(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(page: Int = 0): Result<List<FeedItem>> {
        return postRepository.getExploreFeed(page, PAGE_SIZE)
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}
