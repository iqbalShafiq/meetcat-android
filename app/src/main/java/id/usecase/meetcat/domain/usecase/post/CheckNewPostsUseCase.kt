package id.usecase.meetcat.domain.usecase.post

import id.usecase.meetcat.domain.repository.PostRepository

/**
 * Use case to check if there are new posts available in the explore feed
 * compared to a given timestamp
 */
class CheckNewPostsUseCase(
    private val postRepository: PostRepository
) {
    /**
     * Check if there are new posts since the given timestamp
     * @param currentLatestTimestamp The timestamp of the currently visible latest post
     * @return Result<Boolean> true if there are newer posts, false otherwise
     */
    suspend operator fun invoke(currentLatestTimestamp: Long): Result<Boolean> {
        return postRepository.getLatestPostTimestamp()
            .map { serverLatestTimestamp ->
                serverLatestTimestamp > currentLatestTimestamp
            }
    }
}
