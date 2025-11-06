package id.usecase.meetcat.domain.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import id.usecase.meetcat.domain.model.Post
import id.usecase.meetcat.domain.usecase.post.SearchPostsUseCase

/**
 * PagingSource for Search results with infinite scroll support
 */
class SearchPagingSource(
    private val query: String,
    private val searchPostsUseCase: SearchPostsUseCase
) : PagingSource<Int, Post>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Post> {
        return try {
            val page = params.key ?: INITIAL_PAGE

            val result = searchPostsUseCase(query = query, page = page)

            result.fold(
                onSuccess = { items ->
                    LoadResult.Page(
                        data = items,
                        prevKey = if (page == INITIAL_PAGE) null else page - 1,
                        nextKey = if (items.isEmpty()) null else page + 1
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

    override fun getRefreshKey(state: PagingState<Int, Post>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    companion object {
        private const val INITIAL_PAGE = 1
    }
}
