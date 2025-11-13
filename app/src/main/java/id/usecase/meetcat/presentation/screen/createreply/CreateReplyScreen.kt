package id.usecase.meetcat.presentation.screen.createreply

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.floatingToolbarVerticalNestedScroll
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.HorizontalFloatingToolbar
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.usecase.meetcat.R
import id.usecase.meetcat.domain.model.Post
import id.usecase.meetcat.domain.model.User
import id.usecase.meetcat.ui.theme.MeetCatTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CreateReplyScreen(
    viewModel: CreateReplyViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToReplyDetail: (String) -> Unit,
    onNavigateToCamera: (() -> Unit)? = null,
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

                is CreateReplyUiEffect.NavigateToReplyDetail -> {
                    onNavigateToReplyDetail(effect.replyId)
                }

                is CreateReplyUiEffect.ShowMediaPicker -> {
                    // Navigate to camera screen
                    onNavigateToCamera?.invoke()
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CreateReplyContent(
    uiState: CreateReplyUiState,
    onEvent: (CreateReplyUiEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
        exitDirection = FloatingToolbarExitDirection.Bottom
    )

    var expanded by rememberSaveable { mutableStateOf(true) }
    val vibrantColors = FloatingToolbarDefaults.vibrantFloatingToolbarColors()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Reply",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                windowInsets = WindowInsets()
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            HorizontalFloatingToolbar(
                expanded = expanded,
                floatingActionButton = {
                    FloatingToolbarDefaults.VibrantFloatingActionButton(
                        onClick = {
                            if (!uiState.isPosting) {
                                onEvent(CreateReplyUiEvent.CreateReply)
                            }
                        }
                    ) {
                        if (uiState.isPosting) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Reply"
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = vibrantColors,
                modifier = Modifier
                    .zIndex(1f),
                content = {
                    // Back button
                    IconButton(
                        onClick = { onEvent(CreateReplyUiEvent.NavigateBack) },
                        enabled = !uiState.isPosting,
                        modifier = Modifier.focusProperties { canFocus = expanded }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }

                    // Add Photo button
                    IconButton(
                        onClick = { onEvent(CreateReplyUiEvent.SelectMedia) },
                        enabled = !uiState.isPosting && uiState.mediaUri == null,
                        modifier = Modifier.focusProperties { canFocus = expanded }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Add Photo"
                        )
                    }
                }
            )
        },
        floatingActionButtonPosition = FabPosition.Center
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .floatingToolbarVerticalNestedScroll(
                                expanded = expanded,
                                onExpand = { expanded = true },
                                onCollapse = { expanded = false }
                            )
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Original Post Preview
                        if (uiState.post != null) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
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
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                coil3.compose.AsyncImage(
                                    model = uiState.mediaUri,
                                    contentDescription = "Selected media",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    placeholder = painterResource(R.drawable.ic_placeholder_image),
                                    error = painterResource(R.drawable.ic_placeholder_image)
                                )

                                // Remove button
                                IconButton(
                                    onClick = { onEvent(CreateReplyUiEvent.RemoveMedia) },
                                    enabled = !uiState.isPosting,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .background(
                                            Color.Black.copy(alpha = 0.5f),
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove media",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
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
                    userId = "user1",
                    user = User(
                        id = "user1",
                        username = "catowner",
                        displayName = "Cat Owner",
                        bio = null,
                        profileImageUrl = null,
                        followersCount = 100,
                        followingCount = 50,
                        postsCount = 25,
                        isFollowing = false,
                        createdAt = System.currentTimeMillis()
                    ),
                    caption = "My cat is so cute! Look at those eyes!",
                    mediaItems = emptyList(),
                    location = null,
                    lovesCount = 42,
                    commentsCount = 0,
                    repliesCount = 10,
                    isLoved = false,
                    createdAt = System.currentTimeMillis()
                ),
                replyText = "Aww, so adorable!"
            ),
            onEvent = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
