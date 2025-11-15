package id.usecase.meetcat.presentation.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton manager to track feed state events across the app.
 * Used to trigger "new posts available" chip in Explore screen.
 */
class FeedStateManager {

    private val _shouldRefreshFeed = MutableStateFlow(false)
    val shouldRefreshFeed: StateFlow<Boolean> = _shouldRefreshFeed.asStateFlow()

    /**
     * Notify that a new post was created
     */
    fun notifyPostCreated() {
        _shouldRefreshFeed.value = true
    }

    /**
     * Notify that a post was updated
     */
    fun notifyPostUpdated() {
        _shouldRefreshFeed.value = true
    }

    /**
     * Notify that a new reply was created
     */
    fun notifyReplyCreated() {
        _shouldRefreshFeed.value = true
    }

    /**
     * Notify that a reply was updated
     */
    fun notifyReplyUpdated() {
        _shouldRefreshFeed.value = true
    }

    /**
     * Notify that user returned from detail screen
     */
    fun notifyReturnedFromDetail() {
        _shouldRefreshFeed.value = true
    }

    /**
     * Notify that new posts were detected via polling
     */
    fun notifyNewPostsDetected() {
        _shouldRefreshFeed.value = true
    }

    /**
     * Reset the refresh state (called after user refreshes the feed)
     */
    fun resetRefreshState() {
        _shouldRefreshFeed.value = false
    }
}
