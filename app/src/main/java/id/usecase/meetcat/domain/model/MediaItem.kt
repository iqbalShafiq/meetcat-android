package id.usecase.meetcat.domain.model

sealed class MediaItem {
    abstract val url: String
    abstract val thumbnailUrl: String?

    data class Image(
        override val url: String,
        override val thumbnailUrl: String? = null,
        val width: Int,
        val height: Int
    ) : MediaItem()

    data class Video(
        override val url: String,
        override val thumbnailUrl: String?,
        val duration: Long,
        val width: Int,
        val height: Int
    ) : MediaItem()
}
