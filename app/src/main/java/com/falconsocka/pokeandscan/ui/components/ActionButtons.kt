package com.falconsocka.pokeandscan.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.falconsocka.pokeandscan.ui.theme.AppDimensions
import com.falconsocka.pokeandscan.ui.theme.AppShapes
import com.falconsocka.pokeandscan.ui.theme.focusOutline

/** Primary action with the app-wide height, corner, and keyboard-focus treatment. */
@Composable
fun PrimaryActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = AppDimensions.primaryActionMinHeight)
            .focusOutline(),
        enabled = enabled,
        shape = AppShapes.control,
        content = content
    )
}

/** Secondary outlined action with a consistent 48 dp minimum touch target. */
@Composable
fun SecondaryActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = AppDimensions.minimumTouchTarget)
            .focusOutline(),
        enabled = enabled,
        shape = AppShapes.control,
        content = content
    )
}

/** Quiet text action with a consistent 48 dp minimum touch target and focus treatment. */
@Composable
fun QuietActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = AppDimensions.minimumTouchTarget)
            .focusOutline(),
        enabled = enabled,
        shape = AppShapes.control,
        content = content
    )
}
