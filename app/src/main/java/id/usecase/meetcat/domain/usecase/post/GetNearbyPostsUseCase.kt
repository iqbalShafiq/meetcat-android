package id.usecase.meetcat.domain.usecase.post

import id.usecase.meetcat.domain.model.Location
import id.usecase.meetcat.domain.model.Post
import id.usecase.meetcat.domain.repository.PostRepository

class GetNearbyPostsUseCase(
    private val postRepository: PostRepository
) {
    /**
     * Get posts near a specific location
     * @param location Center point for search
     * @param radiusKm Search radius in kilometers (default 10km)
     * @param limit Maximum number of posts to return (default 100)
     * @return Result containing list of Posts with location data
     */
    suspend operator fun invoke(
        location: Location,
        radiusKm: Double = DEFAULT_RADIUS_KM,
        limit: Int = DEFAULT_LIMIT
    ): Result<List<Post>> {
        return postRepository.getNearbyPosts(location, radiusKm, limit)
    }

    companion object {
        private const val DEFAULT_RADIUS_KM = 10.0
        private const val DEFAULT_LIMIT = 100
    }
}
