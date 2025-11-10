package id.usecase.meetcat.presentation.common

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Global Snackbar controller for showing messages across the app
 */
object SnackbarController {

    private val _snackbarEvents = Channel<SnackbarEvent>(Channel.BUFFERED)
    val snackbarEvents = _snackbarEvents.receiveAsFlow()

    suspend fun showMessage(
        message: String,
        actionLabel: String? = null,
        onActionClick: (() -> Unit)? = null
    ) {
        _snackbarEvents.send(
            SnackbarEvent.Message(
                message = message,
                actionLabel = actionLabel,
                onActionClick = onActionClick
            )
        )
    }

    suspend fun showError(
        message: String,
        actionLabel: String = "Retry",
        onRetry: (() -> Unit)? = null
    ) {
        _snackbarEvents.send(
            SnackbarEvent.Error(
                message = message,
                actionLabel = actionLabel,
                onRetry = onRetry
            )
        )
    }
}

sealed class SnackbarEvent {
    data class Message(
        val message: String,
        val actionLabel: String? = null,
        val onActionClick: (() -> Unit)? = null
    ) : SnackbarEvent()

    data class Error(
        val message: String,
        val actionLabel: String = "Retry",
        val onRetry: (() -> Unit)? = null
    ) : SnackbarEvent()
}
