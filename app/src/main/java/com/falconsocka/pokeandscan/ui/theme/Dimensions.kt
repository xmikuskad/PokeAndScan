package com.falconsocka.pokeandscan.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Shared layout rhythm. Use these tokens for repeated spacing; keep composition-specific structure local. */
object AppSpacing {
    val xSmall: Dp = 4.dp
    val small: Dp = 8.dp
    val medium: Dp = 12.dp
    val large: Dp = 16.dp
    val screen: Dp = 20.dp
    val xLarge: Dp = 24.dp
}

object AppDimensions {
    val primaryActionMinHeight: Dp = 52.dp
    val minimumTouchTarget: Dp = 48.dp
    val choiceRowMinHeight: Dp = 52.dp
    val settingsActionRowMinHeight: Dp = 56.dp
    val cardCornerRadius: Dp = 16.dp
    val controlCornerRadius: Dp = 12.dp
    val statusBadgeCornerRadius: Dp = 10.dp
}

object AppShapes {
    val card: Shape = RoundedCornerShape(AppDimensions.cardCornerRadius)
    val control: Shape = RoundedCornerShape(AppDimensions.controlCornerRadius)
}
