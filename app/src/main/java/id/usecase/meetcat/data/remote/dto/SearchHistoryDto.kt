package id.usecase.meetcat.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SaveSearchQueryRequest(
    val query: String
)
