package id.usecase.meetcat.domain.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import id.usecase.meetcat.domain.model.FeedItem
import id.usecase.meetcat.domain.usecase.post.GetExploreFeedUseCase

/**
 * PagingSource for Explore feed with infinite scroll support
 *
 * Uses 0-based page indexing for consistency with repository layer.
 * Properly detects end of pagination when items.size < expected page size.
 */
class ExplorePagingSource(
    private val getExploreFeedUseCase: GetExploreFeedUseCase
) : PagingSource<Int, FeedItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FeedItem> {
        return try {
            val page = params.key ?: INITIAL_PAGE

            val result = getExploreFeedUseCase(page = page)

            result.fold(
                onSuccess = { items ->
                    LoadResult.Page(
                        data = items,
                        prevKey = if (page == INITIAL_PAGE) null else page - 1,
                        // nextKey is null if we've reached the end (fewer items than page size)
                        // or if items is empty
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
        // Try to find the page key of the closest item to the current scroll position
        // This helps maintain scroll position after refresh
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    companion object {
        private const val INITIAL_PAGE = 0 // 0-based indexing
    }
}
