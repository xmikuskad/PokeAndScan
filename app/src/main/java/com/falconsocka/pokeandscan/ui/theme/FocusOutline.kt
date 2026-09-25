package com.falconsocka.pokeandscan.ui.theme

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

fun Modifier.focusOutline(shape: Shape = RoundedCornerShape(12.dp)): Modifier = composed {
    var isFocused by remember { mutableStateOf(false) }
    val color = LocalFocusOutlineColor.current
    onFocusChanged { isFocused = it.isFocused }
        .then(if (isFocused) Modifier.border(width = 2.dp, color = color, shape = shape) else Modifier)
}
