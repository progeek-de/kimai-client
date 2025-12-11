package de.progeek.kimai.shared.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import de.progeek.kimai.shared.SharedRes
import de.progeek.kimai.shared.ui.theme.BrandingEnum
import de.progeek.kimai.shared.ui.theme.BrandingLocal
import de.progeek.kimai.shared.ui.theme.ThemeEnum
import de.progeek.kimai.shared.ui.theme.ThemeLocal
import dev.icerock.moko.resources.compose.painterResource

@Composable
fun KimaiIcon(
    modifier: Modifier = Modifier
) {
    val theme = ThemeLocal.current
    val branding = BrandingLocal.current
    val useProgeek = branding == BrandingEnum.PROGEEK

    val resource = when {
        useProgeek && theme == ThemeEnum.LIGHT -> SharedRes.images.progeek_dark
        useProgeek -> SharedRes.images.progeek_light
        theme == ThemeEnum.LIGHT -> SharedRes.images.kimai_icon_orange
        else -> SharedRes.images.kimai_icon_white
    }

    Image(
        painter = painterResource(resource),
        contentDescription = "Kimai",
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}