package id.usecase.meetcat.presentation.screen.settings.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.usecase.meetcat.ui.theme.MeetCatTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AccountSettingsScreen(
    viewModel: AccountSettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is AccountSettingsUiEffect.NavigateBack -> {
                    onNavigateBack()
                }

                is AccountSettingsUiEffect.ShowChangePasswordDialog -> {
                    showChangePasswordDialog = true
                }

                is AccountSettingsUiEffect.ShowDeleteAccountDialog -> {
                    showDeleteAccountDialog = true
                }

                is AccountSettingsUiEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }

                is AccountSettingsUiEffect.ShowSuccess -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    AccountSettingsContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        showChangePasswordDialog = showChangePasswordDialog,
        onDismissChangePasswordDialog = { showChangePasswordDialog = false },
        showDeleteAccountDialog = showDeleteAccountDialog,
        onDismissDeleteAccountDialog = { showDeleteAccountDialog = false },
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountSettingsContent(
    uiState: AccountSettingsUiState,
    onEvent: (AccountSettingsUiEvent) -> Unit,
    showChangePasswordDialog: Boolean,
    onDismissChangePasswordDialog: () -> Unit,
    showDeleteAccountDialog: Boolean,
    onDismissDeleteAccountDialog: () -> Unit,
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
                        text = "Account Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(AccountSettingsUiEvent.NavigateBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
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
                    .verticalScroll(scrollState)
            ) {
                // Account Information
                SectionHeader(text = "Account Information")
                InfoItem(label = "Email", value = uiState.email)
                HorizontalDivider()
                InfoItem(label = "Username", value = "@${uiState.username}")
                HorizontalDivider()
                InfoItem(label = "Display Name", value = uiState.displayName)
                HorizontalDivider()

                Spacer(modifier = Modifier.height(16.dp))

                // Security
                SectionHeader(text = "Security")
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "Change Password",
                    onClick = { onEvent(AccountSettingsUiEvent.ChangePassword) }
                )
                HorizontalDivider()

                Spacer(modifier = Modifier.height(16.dp))

                // Danger Zone
                SectionHeader(text = "Danger Zone")
                SettingsItem(
                    icon = Icons.Default.Delete,
                    title = "Delete Account",
                    isDestructive = true,
                    onClick = { onEvent(AccountSettingsUiEvent.DeleteAccount) }
                )
                HorizontalDivider()
            }
        }
    }

    // Change Password Dialog
    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = onDismissChangePasswordDialog,
            title = { Text("Change Password") },
            text = {
                Text(
                    "Password change functionality requires backend integration.\n\n" +
                    "In a production app, this would:\n" +
                    "• Verify current password\n" +
                    "• Validate new password strength\n" +
                    "• Update password in secure backend"
                )
            },
            confirmButton = {
                TextButton(onClick = onDismissChangePasswordDialog) {
                    Text("OK")
                }
            }
        )
    }

    // Delete Account Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = onDismissDeleteAccountDialog,
            title = { Text("Delete Account") },
            text = {
                Text(
                    "Are you sure you want to delete your account?\n\n" +
                    "This action cannot be undone. All your posts, replies, and data will be permanently deleted."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDismissDeleteAccountDialog()
                        // Note: Actual implementation would call backend API
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeleteAccountDialog) {
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
private fun InfoItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
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
            tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountSettingsScreenPreview() {
    MeetCatTheme {
        AccountSettingsContent(
            uiState = AccountSettingsUiState(
                email = "test@meetcat.com",
                username = "testuser",
                displayName = "Test User"
            ),
            onEvent = {},
            showChangePasswordDialog = false,
            onDismissChangePasswordDialog = {},
            showDeleteAccountDialog = false,
            onDismissDeleteAccountDialog = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
