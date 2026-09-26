package com.falconsocka.pokeandscan.ui.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.falconsocka.pokeandscan.ui.theme.AppSpacing

@Composable
fun ScrollableScreenColumn(
    modifier: Modifier = Modifier,
    verticalPadding: Dp = AppSpacing.screen,
    maxContentWidth: Dp = 560.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .widthIn(max = maxContentWidth)
                .fillMaxSize()
                .align(Alignment.TopCenter)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.screen, vertical = verticalPadding),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(AppSpacing.large),
            content = content
        )
    }
}
