package id.usecase.meetcat.domain.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import id.usecase.meetcat.domain.model.FeedItem
import id.usecase.meetcat.domain.usecase.user.GetUserRepliesUseCase

/**
 * PagingSource for user's replies with infinite scroll support
 *
 * Uses 0-based page indexing for consistency with repository layer.
 * Properly detects end of pagination when items.size < expected page size.
 */
class UserRepliesPagingSource(
    private val userId: String,
    private val getUserRepliesUseCase: GetUserRepliesUseCase
) : PagingSource<Int, FeedItem.ReplyItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FeedItem.ReplyItem> {
        return try {
            val page = params.key ?: INITIAL_PAGE

            val result = getUserRepliesUseCase(userId = userId, page = page)

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

    override fun getRefreshKey(state: PagingState<Int, FeedItem.ReplyItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    companion object {
        private const val INITIAL_PAGE = 0
    }
}
