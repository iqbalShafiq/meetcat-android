package id.usecase.meetcat.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorDto? = null
)

@Serializable
data class ErrorDto(
    val code: String,
    val message: String
)

@Serializable
data class PaginatedResponse<T>(
    val data: List<T>,
    val pagination: PaginationDto
)

@Serializable
data class PaginationDto(
    val page: Int,
    val pageSize: Int,
    val hasMore: Boolean
)
