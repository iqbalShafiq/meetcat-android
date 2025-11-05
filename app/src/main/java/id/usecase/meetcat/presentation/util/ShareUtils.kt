package id.usecase.meetcat.presentation.util

import android.content.Context
import android.content.Intent
import id.usecase.meetcat.domain.model.Post
import id.usecase.meetcat.domain.model.Reply

/**
 * Share a post using Android's native share sheet
 */
fun sharePost(context: Context, post: Post) {
    val shareText = buildString {
        append("${post.user.displayName} (@${post.user.username}):\n")
        append(post.caption)
        append("\n\n")
        append("Shared from MeetCat")
    }

    shareContent(context, shareText, "Share Post")
}

/**
 * Share a reply using Android's native share sheet
 */
fun shareReply(context: Context, reply: Reply) {
    val shareText = buildString {
        append("${reply.user.displayName} (@${reply.user.username}) replied:\n")
        append(reply.text)
        append("\n\n")
        append("In reply to ${reply.originalPost.user.displayName}:\n")
        append(reply.originalPost.caption)
        append("\n\n")
        append("Shared from MeetCat")
    }

    shareContent(context, shareText, "Share Reply")
}

/**
 * Generic function to share text content
 */
private fun shareContent(context: Context, text: String, title: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, title)
    context.startActivity(shareIntent)
}
