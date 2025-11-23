package de.progeek.kimai.shared.ui.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.progeek.kimai.shared.BuildKonfig
import de.progeek.kimai.shared.SharedRes
import dev.icerock.moko.resources.compose.stringResource

/**
 * Displays the app version in the bottom-right corner
 */
@Composable
fun VersionInfo() {
    val projectVersion = BuildKonfig.KIMAI_VER

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = stringResource(SharedRes.strings.version, projectVersion),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}