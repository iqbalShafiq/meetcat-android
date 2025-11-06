package id.usecase.meetcat.domain.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import id.usecase.meetcat.domain.model.FeedItem
import id.usecase.meetcat.domain.usecase.user.GetUserLovedItemsUseCase

/**
 * PagingSource for user's loved items with infinite scroll support
 *
 * Uses 0-based page indexing for consistency with repository layer.
 * Properly detects end of pagination when items.size < expected page size.
 */
class UserLovedItemsPagingSource(
    private val userId: String,
    private val getUserLovedItemsUseCase: GetUserLovedItemsUseCase
) : PagingSource<Int, FeedItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FeedItem> {
        return try {
            val page = params.key ?: INITIAL_PAGE

            val result = getUserLovedItemsUseCase(userId = userId, page = page)

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

    override fun getRefreshKey(state: PagingState<Int, FeedItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    companion object {
        private const val INITIAL_PAGE = 0
    }
}
