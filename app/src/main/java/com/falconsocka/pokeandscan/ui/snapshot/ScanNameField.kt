package com.falconsocka.pokeandscan.ui.snapshot

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.falconsocka.pokeandscan.R
import com.falconsocka.pokeandscan.ui.theme.AppShapes
import com.falconsocka.pokeandscan.ui.theme.focusOutline

/** Scan-name text field. Callers own validation and decide whether blank input is allowed. */
@Composable
fun ScanNameField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth().focusOutline(AppShapes.control),
        label = { Text(label) },
        placeholder = placeholder?.let { value -> ({ Text(value) }) },
        supportingText = supportingText?.let { value -> ({ Text(value) }) },
        isError = isError,
        enabled = enabled,
        singleLine = true,
        shape = AppShapes.control
    )
}

@Composable
internal fun snapshotNameIssueMessage(issue: com.falconsocka.pokeandscan.SnapshotNameIssue?): String? = when (issue) {
    com.falconsocka.pokeandscan.SnapshotNameIssue.TOO_LONG -> stringResource(R.string.scan_name_too_long)
    com.falconsocka.pokeandscan.SnapshotNameIssue.UNSAFE_CHARACTERS -> stringResource(R.string.scan_name_unsafe_characters)
    null -> null
}
