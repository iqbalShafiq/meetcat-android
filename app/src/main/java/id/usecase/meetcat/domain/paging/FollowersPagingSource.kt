package id.usecase.meetcat.domain.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import id.usecase.meetcat.domain.model.User
import id.usecase.meetcat.domain.usecase.user.GetFollowersUseCase

/**
 * PagingSource for user's followers with infinite scroll support
 *
 * Uses 0-based page indexing for consistency with repository layer.
 * Properly detects end of pagination when items.size < expected page size.
 */
class FollowersPagingSource(
    private val userId: String,
    private val getFollowersUseCase: GetFollowersUseCase
) : PagingSource<Int, User>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, User> {
        return try {
            val page = params.key ?: INITIAL_PAGE

            val result = getFollowersUseCase(userId = userId, page = page)

            result.fold(
                onSuccess = { items ->
                    LoadResult.Page(
                        data = items,
                        prevKey = if (page == INITIAL_PAGE) null else page - 1,
                        nextKey = if (items.size < params.loadSize) null else page + 1
                    )
                },
                onFailure = { error ->
                    LoadResult.Error(error)
                }
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, User>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    companion object {
        private const val INITIAL_PAGE = 0
    }
}
