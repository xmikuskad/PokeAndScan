package com.falconsocka.pokeandscan.ui.illustration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.falconsocka.pokeandscan.ui.theme.AppSpacing

/** Centered artwork and copy for the introductory part of an illustrated screen. */
@Composable
fun IllustratedIntro(
    illustrationRes: Int,
    body: String,
    modifier: Modifier = Modifier,
    title: String? = null
) {
    Column(
        modifier = modifier.widthIn(max = 440.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IllustrationArtwork(illustrationRes, height = 208.dp)
        Spacer(Modifier.height(AppSpacing.large))
        title?.let {
            Text(it, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Spacer(Modifier.height(AppSpacing.small))
        }
        Text(
            body,
            modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun IllustratedIntro(
    illustrationRes: Int,
    titleRes: Int,
    bodyRes: Int,
    modifier: Modifier = Modifier
) {
    IllustratedIntro(
        illustrationRes = illustrationRes,
        title = stringResource(titleRes),
        body = stringResource(bodyRes),
        modifier = modifier
    )
}
