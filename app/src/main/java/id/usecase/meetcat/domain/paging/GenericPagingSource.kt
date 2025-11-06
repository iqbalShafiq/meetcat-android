package id.usecase.meetcat.domain.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState

/**
 * Generic PagingSource that can be used with any data type and pagination function.
 * This provides a flexible way to implement pagination for any list without creating
 * separate PagingSource classes for each use case.
 *
 * @param T The type of data being paginated
 * @param loadFunction A suspend function that loads a page of data given a page number
 */
class GenericPagingSource<T : Any>(
    private val loadFunction: suspend (page: Int) -> Result<List<T>>
) : PagingSource<Int, T>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        return try {
            val page = params.key ?: INITIAL_PAGE

            val result = loadFunction(page)

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

    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    companion object {
        private const val INITIAL_PAGE = 1
    }
}
