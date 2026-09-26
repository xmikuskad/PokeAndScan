package com.falconsocka.pokeandscan.ui.illustration

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Displays the complete selected illustration as decorative artwork. */
@Composable
fun IllustrationArtwork(
    illustrationRes: Int,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp
) {
    Box(
        modifier = modifier.fillMaxWidth().height(height),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(illustrationRes),
            contentDescription = null,
            modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth().fillMaxHeight(),
            contentScale = ContentScale.Fit
        )
    }
}
