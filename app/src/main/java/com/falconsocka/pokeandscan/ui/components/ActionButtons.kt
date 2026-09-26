package com.falconsocka.pokeandscan.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.falconsocka.pokeandscan.ui.theme.AppDimensions
import com.falconsocka.pokeandscan.ui.theme.AppShapes
import com.falconsocka.pokeandscan.ui.theme.focusOutline

/** Primary action with the app-wide height, corner, and keyboard-focus treatment. */
@Composable
fun PrimaryActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = AppDimensions.primaryActionMinHeight)
            .focusOutline(),
        enabled = enabled && !loading,
        shape = AppShapes.control,
        content = { ActionButtonContent(loading, content) }
    )
}

/** Secondary outlined action with a consistent 48 dp minimum touch target. */
@Composable
fun SecondaryActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = AppDimensions.minimumTouchTarget)
            .focusOutline(),
        enabled = enabled && !loading,
        shape = AppShapes.control,
        content = { ActionButtonContent(loading, content) }
    )
}

/** Quiet text action with a consistent 48 dp minimum touch target and focus treatment. */
@Composable
fun QuietActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = AppDimensions.minimumTouchTarget)
            .focusOutline(),
        enabled = enabled && !loading,
        shape = AppShapes.control,
        content = { ActionButtonContent(loading, content) }
    )
}

/** Quiet destructive action for an explicit confirmation step. */
@Composable
fun DestructiveActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = AppDimensions.minimumTouchTarget)
            .focusOutline(),
        enabled = enabled && !loading,
        shape = AppShapes.control,
        colors = ButtonDefaults.textButtonColors(contentColor = androidx.compose.material3.MaterialTheme.colorScheme.error),
        content = { ActionButtonContent(loading, content) }
    )
}

@Composable
private fun RowScope.ActionButtonContent(loading: Boolean, content: @Composable RowScope.() -> Unit) {
    if (loading) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            color = LocalContentColor.current,
            strokeWidth = 2.dp
        )
        Spacer(Modifier.width(8.dp))
    }
    content()
}
