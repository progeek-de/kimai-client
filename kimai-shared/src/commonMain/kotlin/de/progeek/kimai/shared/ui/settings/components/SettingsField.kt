package de.progeek.kimai.shared.ui.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Reusable settings field component that provides consistent label + content layout
 */
@Composable
fun SettingsField(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier.padding(start = 16.dp, top = 12.dp).fillMaxWidth()
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        content()
    }
}
