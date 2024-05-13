package de.progeek.kimai.shared.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import de.progeek.kimai.shared.SharedRes
import de.progeek.kimai.shared.ui.theme.ThemeEnum
import de.progeek.kimai.shared.ui.theme.ThemeLocal
import dev.icerock.moko.resources.compose.painterResource

@Composable
fun KimaiIcon(
    modifier: Modifier = Modifier
) {
    val theme = ThemeLocal.current
    val resource = when(theme) {
        ThemeEnum.LIGHT -> SharedRes.images.kimai_logo
        ThemeEnum.DARK -> SharedRes.images.kimai_logo
    }

    Image(
        painter = painterResource(resource),
        contentDescription = "Kimai",
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}