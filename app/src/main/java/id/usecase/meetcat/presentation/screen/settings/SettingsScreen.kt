package id.usecase.meetcat.presentation.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.usecase.meetcat.ui.theme.MeetCatTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAccountSettings: () -> Unit,
    onNavigateToPrivacySettings: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is SettingsUiEffect.NavigateToAccountSettings -> {
                    onNavigateToAccountSettings()
                }

                is SettingsUiEffect.NavigateToPrivacySettings -> {
                    onNavigateToPrivacySettings()
                }

                is SettingsUiEffect.NavigateToAbout -> {
                    onNavigateToAbout()
                }

                is SettingsUiEffect.NavigateToLogin -> {
                    onNavigateToLogin()
                }

                is SettingsUiEffect.NavigateBack -> {
                    onNavigateBack()
                }

                is SettingsUiEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    SettingsContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(SettingsUiEvent.NavigateBack) }) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // Account Section
            SectionHeader(text = "Account")
            SettingsItem(
                icon = Icons.Default.Person,
                title = "Account Settings",
                subtitle = "Manage your account",
                onClick = { onEvent(SettingsUiEvent.NavigateToAccountSettings) }
            )
            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))

            // Privacy & Security Section
            SectionHeader(text = "Privacy & Security")
            SettingsItem(
                icon = Icons.Default.Lock,
                title = "Privacy Settings",
                subtitle = "Control your privacy",
                onClick = { onEvent(SettingsUiEvent.NavigateToPrivacySettings) }
            )
            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))

            // About Section
            SectionHeader(text = "About")
            SettingsItem(
                icon = Icons.Default.Info,
                title = "About MeetCat",
                subtitle = "Version 1.0.0",
                onClick = { onEvent(SettingsUiEvent.NavigateToAbout) }
            )
            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))

            // Logout Section
            SectionHeader(text = "Session")
            SettingsItem(
                icon = Icons.AutoMirrored.Filled.Logout,
                title = "Logout",
                subtitle = "Sign out from your account",
                onClick = { showLogoutDialog = true },
                isDestructive = true
            )
            HorizontalDivider()
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onEvent(SettingsUiEvent.Logout)
                    },
                    enabled = !uiState.isLoggingOut
                ) {
                    if (uiState.isLoggingOut) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text("Logout")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false },
                    enabled = !uiState.isLoggingOut
                ) {
                    Text("Cancel")
                }
            }
        )
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
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    MeetCatTheme {
        SettingsContent(
            uiState = SettingsUiState(),
            onEvent = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
