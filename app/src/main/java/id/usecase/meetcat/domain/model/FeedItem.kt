package id.usecase.meetcat.domain.model

sealed class FeedItem {
    data class PostItem(val post: Post) : FeedItem()
    data class ReplyItem(val reply: Reply) : FeedItem()
}
