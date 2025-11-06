package id.usecase.meetcat.presentation.preview

import id.usecase.meetcat.domain.model.Comment
import id.usecase.meetcat.domain.model.Location
import id.usecase.meetcat.domain.model.MediaItem
import id.usecase.meetcat.domain.model.Post
import id.usecase.meetcat.domain.model.Reply
import id.usecase.meetcat.domain.model.User

object PreviewData {
    val mockUser = User(
        id = "1",
        username = "cat_lover_123",
        displayName = "Cat Lover",
        bio = "Love all cats 🐱",
        profileImageUrl = "https://picsum.photos/200?random=1",
        followersCount = 1234,
        followingCount = 567,
        postsCount = 89,
        isFollowing = false,
        createdAt = System.currentTimeMillis() - 86400000
    )

    val mockUser2 = User(
        id = "2",
        username = "meow_master",
        displayName = "Meow Master",
        bio = "Professional cat photographer",
        profileImageUrl = "https://picsum.photos/200?random=2",
        followersCount = 5678,
        followingCount = 234,
        postsCount = 456,
        isFollowing = true,
        createdAt = System.currentTimeMillis() - 172800000
    )

    val mockLocation = Location(
        latitude = -6.2088,
        longitude = 106.8456,
        address = "Jakarta, Indonesia",
        name = "Jakarta"
    )

    val mockMediaImage = MediaItem.Image(
        url = "https://picsum.photos/800/600?random=10",
        thumbnailUrl = "https://picsum.photos/200/150?random=10",
        width = 800,
        height = 600
    )

    val mockMediaVideo = MediaItem.Video(
        url = "https://picsum.photos/800/600?random=11",
        thumbnailUrl = "https://picsum.photos/200/150?random=11",
        duration = 15000,
        width = 800,
        height = 600
    )

    val mockPost = Post(
        id = "post1",
        userId = "1",
        user = mockUser,
        caption = "Found this adorable cat sleeping in the sun! 🐱☀️",
        mediaItems = listOf(mockMediaImage),
        location = mockLocation,
        lovesCount = 234,
        commentsCount = 45,
        repliesCount = 12,
        isLoved = false,
        createdAt = System.currentTimeMillis() - 3600000
    )

    val mockPostLoved = mockPost.copy(
        isLoved = true,
        lovesCount = 235
    )

    val mockPostWithVideo = mockPost.copy(
        id = "post2",
        caption = "My cat just learned a new trick! Watch this amazing jump! 🎪",
        mediaItems = listOf(mockMediaVideo),
        location = null
    )

    val mockPostWithMultipleMedia = mockPost.copy(
        id = "post3",
        caption = "Beautiful cat photos from today! 📸",
        mediaItems = listOf(mockMediaImage, mockMediaImage, mockMediaVideo)
    )

    val mockReply = Reply(
        id = "reply1",
        originalPostId = "post1",
        originalPost = mockPost,
        userId = "2",
        user = mockUser2,
        text = "This is so cute! I also saw a cat like this yesterday 😍",
        mediaItems = listOf(mockMediaImage),
        location = mockLocation,
        lovesCount = 45,
        commentsCount = 12,
        isLoved = false,
        createdAt = System.currentTimeMillis() - 5400000
    )

    val mockReplyTextOnly = mockReply.copy(
        id = "reply2",
        text = "Wow! That's incredible! How did you train your cat to do that?",
        mediaItems = null,
        location = null
    )

    val mockComment = Comment(
        id = "comment1",
        postId = "post1",
        userId = "2",
        user = mockUser2,
        text = "This is such a great photo! 😍",
        lovesCount = 12,
        isLoved = false,
        createdAt = System.currentTimeMillis() - 1800000
    )

    val mockCommentLoved = mockComment.copy(
        id = "comment2",
        text = "Absolutely adorable! Where did you take this?",
        lovesCount = 25,
        isLoved = true
    )
}
