package com.falconsocka.pokeandscan.ui.snapshot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.falconsocka.pokeandscan.R
import com.falconsocka.pokeandscan.snapshotNameIssue
import com.falconsocka.pokeandscan.ui.components.InlineNotice
import com.falconsocka.pokeandscan.ui.components.NoticeTone
import com.falconsocka.pokeandscan.ui.components.QuietActionButton
import com.falconsocka.pokeandscan.ui.theme.AppSpacing

/** Draft-only rename form; the owner commits the value only after explicit Save. */
@Composable
internal fun RenameSnapshotDialog(
    name: String,
    onNameChange: (String) -> Unit,
    isSaving: Boolean,
    operationError: Boolean,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var hasSubmitted by remember { mutableStateOf(false) }
    var observedSaving by remember { mutableStateOf(false) }
    LaunchedEffect(isSaving, operationError, hasSubmitted) {
        if (hasSubmitted && isSaving) observedSaving = true
        if (hasSubmitted && observedSaving && !isSaving) {
            if (!operationError) onDismiss()
            hasSubmitted = false
            observedSaving = false
        }
    }
    val issue = snapshotNameIssue(name)
    val issueMessage = snapshotNameIssueMessage(issue)
    val blank = name.isBlank()
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text(stringResource(R.string.snapshot_rename_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                ScanNameField(
                    value = name,
                    onValueChange = onNameChange,
                    label = stringResource(R.string.snapshot_name_label),
                    supportingText = issueMessage ?: if (blank) stringResource(R.string.snapshot_name_required) else null,
                    isError = issue != null || blank,
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
                if (operationError) {
                    InlineNotice(
                        message = stringResource(R.string.snapshot_operation_error),
                        tone = NoticeTone.Error
                    )
                }
            }
        },
        confirmButton = {
            QuietActionButton(
                onClick = {
                    hasSubmitted = true
                    onSave(name)
                },
                enabled = !isSaving && !blank && issue == null,
                loading = isSaving
            ) {
                Text(stringResource(if (isSaving) R.string.snapshot_saving else R.string.snapshot_save_name_action))
            }
        },
        dismissButton = {
            QuietActionButton(onClick = onDismiss, enabled = !isSaving) {
                Text(stringResource(R.string.action_back))
            }
        }
    )
}
