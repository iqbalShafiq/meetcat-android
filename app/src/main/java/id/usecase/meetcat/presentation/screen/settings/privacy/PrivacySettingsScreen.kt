package id.usecase.meetcat.presentation.screen.settings.privacy

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.usecase.meetcat.ui.theme.MeetCatTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun PrivacySettingsScreen(
    viewModel: PrivacySettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is PrivacySettingsUiEffect.NavigateBack -> {
                    onNavigateBack()
                }

                is PrivacySettingsUiEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }

                is PrivacySettingsUiEffect.ShowSuccess -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    PrivacySettingsContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrivacySettingsContent(
    uiState: PrivacySettingsUiState,
    onEvent: (PrivacySettingsUiEvent) -> Unit,
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
                        text = "Privacy Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(PrivacySettingsUiEvent.NavigateBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                windowInsets = WindowInsets()
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
                    .verticalScroll(scrollState)
            ) {
                // Account Privacy
                SectionHeader(text = "Account Privacy")
                SwitchItem(
                    title = "Private Account",
                    subtitle = "Only approved followers can see your posts",
                    checked = uiState.isPrivateAccount,
                    onCheckedChange = {
                        onEvent(PrivacySettingsUiEvent.PrivateAccountToggled(it))
                    },
                    enabled = !uiState.isSaving
                )
                HorizontalDivider()

                Spacer(modifier = Modifier.height(16.dp))

                // Activity
                SectionHeader(text = "Activity")
                SwitchItem(
                    title = "Show Activity Status",
                    subtitle = "Let others see when you're active",
                    checked = uiState.showActivityStatus,
                    onCheckedChange = {
                        onEvent(PrivacySettingsUiEvent.ShowActivityStatusToggled(it))
                    },
                    enabled = !uiState.isSaving
                )
                HorizontalDivider()

                Spacer(modifier = Modifier.height(16.dp))

                // Interactions
                SectionHeader(text = "Interactions")
                SwitchItem(
                    title = "Allow Tagging",
                    subtitle = "Let others tag you in their posts",
                    checked = uiState.allowTagging,
                    onCheckedChange = {
                        onEvent(PrivacySettingsUiEvent.AllowTaggingToggled(it))
                    },
                    enabled = !uiState.isSaving
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun SwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PrivacySettingsScreenPreview() {
    MeetCatTheme {
        PrivacySettingsContent(
            uiState = PrivacySettingsUiState(
                isPrivateAccount = false,
                showActivityStatus = true,
                allowTagging = true
            ),
            onEvent = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
