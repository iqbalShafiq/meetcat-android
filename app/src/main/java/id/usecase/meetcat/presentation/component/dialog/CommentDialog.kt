package id.usecase.meetcat.presentation.component.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.usecase.meetcat.domain.model.Post
import id.usecase.meetcat.domain.model.Reply
import id.usecase.meetcat.presentation.component.user.AvatarSize
import id.usecase.meetcat.presentation.component.user.UserInfo
import id.usecase.meetcat.presentation.preview.PreviewData
import id.usecase.meetcat.ui.theme.MeetCatTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentDialog(
    post: Post?,
    reply: Reply?,
    onDismiss: () -> Unit,
    onCommentSubmit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var commentText by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Text(
                text = if (post != null) "Comment on Post" else "Comment on Reply",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Original Content Preview
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    when {
                        post != null -> {
                            UserInfo(
                                user = post.user,
                                avatarSize = AvatarSize.Small,
                                onUserClick = {}
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = post.caption,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        reply != null -> {
                            UserInfo(
                                user = reply.user,
                                avatarSize = AvatarSize.Small,
                                onUserClick = {}
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = reply.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Comment Text Field
            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = { Text("Write your comment...") },
                maxLines = 5,
                textStyle = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                            onDismiss()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            scope.launch {
                                sheetState.hide()
                                onCommentSubmit(commentText)
                            }
                        }
                    },
                    enabled = commentText.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Post Comment")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview
@Composable
private fun CommentDialogPostPreview() {
    MeetCatTheme {
        CommentDialog(
            post = PreviewData.mockPost,
            reply = null,
            onDismiss = {},
            onCommentSubmit = {}
        )
    }
}

@Preview
@Composable
private fun CommentDialogReplyPreview() {
    MeetCatTheme {
        CommentDialog(
            post = null,
            reply = PreviewData.mockReply,
            onDismiss = {},
            onCommentSubmit = {}
        )
    }
}
