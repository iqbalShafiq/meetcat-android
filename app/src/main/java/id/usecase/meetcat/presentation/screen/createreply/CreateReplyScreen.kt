package id.usecase.meetcat.presentation.screen.createreply

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.usecase.meetcat.domain.model.Post
import id.usecase.meetcat.domain.model.User
import id.usecase.meetcat.ui.theme.MeetCatTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CreateReplyScreen(
    viewModel: CreateReplyViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is CreateReplyUiEffect.NavigateBack -> {
                    onNavigateBack()
                }

                is CreateReplyUiEffect.ShowMediaPicker -> {
                    // TODO: Show media picker
                }

                is CreateReplyUiEffect.ShowSuccess -> {
                    snackbarHostState.showSnackbar(effect.message)
                }

                is CreateReplyUiEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    CreateReplyContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateReplyContent(
    uiState: CreateReplyUiState,
    onEvent: (CreateReplyUiEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Reply",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onEvent(CreateReplyUiEvent.NavigateBack) },
                        enabled = !uiState.isPosting
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { onEvent(CreateReplyUiEvent.CreateReply) },
                        enabled = !uiState.isPosting
                    ) {
                        if (uiState.isPosting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Reply")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                ) {
                    // Original Post Preview
                    if (uiState.post != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row {
                                // User Avatar
                                Surface(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = uiState.post.user.displayName.first().uppercase(),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = uiState.post.user.displayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "@${uiState.post.user.username}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = uiState.post.caption,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Replying to @${uiState.post.user.username}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        HorizontalDivider()
                    }

                    // Reply TextField
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        TextField(
                            value = uiState.replyText,
                            onValueChange = { onEvent(CreateReplyUiEvent.TextChanged(it)) },
                            placeholder = { Text("Write your reply...") },
                            isError = uiState.textError != null,
                            supportingText = {
                                if (uiState.textError != null) {
                                    Text(uiState.textError)
                                } else {
                                    Text("${uiState.replyText.length}/280")
                                }
                            },
                            enabled = !uiState.isPosting,
                            minLines = 3,
                            maxLines = 8,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Media Preview
                        if (uiState.mediaUri != null) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "Selected image",
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                IconButton(
                                    onClick = { onEvent(CreateReplyUiEvent.RemoveMedia) },
                                    enabled = !uiState.isPosting,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove media"
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Add Media Button
                        OutlinedButton(
                            onClick = { onEvent(CreateReplyUiEvent.SelectMedia) },
                            enabled = !uiState.isPosting && uiState.mediaUri == null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Photo")
                        }
                    }
                }

                // Reply Button at bottom
                Button(
                    onClick = { onEvent(CreateReplyUiEvent.CreateReply) },
                    enabled = !uiState.isPosting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(48.dp)
                ) {
                    if (uiState.isPosting) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(20.dp)
                        )
                        Text("Posting...")
                    } else {
                        Text("Reply", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateReplyScreenPreview() {
    MeetCatTheme {
        CreateReplyContent(
            uiState = CreateReplyUiState(
                post = Post(
                    id = "1",
                    user = User(
                        id = "user1",
                        username = "catowner",
                        displayName = "Cat Owner",
                        profileImageUrl = null,
                        bio = null,
                        followersCount = 100,
                        followingCount = 50,
                        postsCount = 25,
                        isFollowing = false
                    ),
                    caption = "My cat is so cute! Look at those eyes!",
                    imageUrl = null,
                    lovesCount = 42,
                    repliesCount = 10,
                    sharesCount = 5,
                    isLoved = false,
                    createdAt = System.currentTimeMillis(),
                    latitude = null,
                    longitude = null
                ),
                replyText = "Aww, so adorable!"
            ),
            onEvent = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
