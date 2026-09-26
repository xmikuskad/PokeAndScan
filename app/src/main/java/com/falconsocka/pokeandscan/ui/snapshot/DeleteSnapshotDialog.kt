package com.falconsocka.pokeandscan.ui.snapshot

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.falconsocka.pokeandscan.R
import com.falconsocka.pokeandscan.ui.components.DestructiveActionButton
import com.falconsocka.pokeandscan.ui.components.QuietActionButton
import com.falconsocka.pokeandscan.ui.theme.AppSpacing

/** Explicit destructive confirmation. The caller owns deletion and error state. */
@Composable
internal fun DeleteSnapshotDialog(
    snapshotName: String,
    isDeleting: Boolean,
    operationError: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isDeleting) onDismiss() },
        title = { Text(stringResource(R.string.snapshot_delete_confirmation_title, snapshotName)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.snapshot_delete_confirmation_body))
                if (operationError) {
                    Text(
                        stringResource(R.string.snapshot_operation_error),
                        modifier = Modifier.padding(top = AppSpacing.medium),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            DestructiveActionButton(
                onClick = onConfirm,
                enabled = !isDeleting,
                loading = isDeleting
            ) {
                Text(stringResource(if (isDeleting) R.string.snapshot_deleting else R.string.snapshot_delete_action))
            }
        },
        dismissButton = {
            QuietActionButton(onClick = onDismiss, enabled = !isDeleting) {
                Text(stringResource(R.string.action_back))
            }
        }
    )
}
