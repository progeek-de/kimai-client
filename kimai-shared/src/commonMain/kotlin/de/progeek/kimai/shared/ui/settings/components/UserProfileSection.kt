package de.progeek.kimai.shared.ui.settings.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.progeek.kimai.shared.ui.settings.SettingsComponent

/**
 * Displays the logged-in user's email with a profile icon
 */
@Composable
fun UserProfileSection(component: SettingsComponent) {
    val state by component.state.collectAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.PersonOutline,
            contentDescription = "PersonOutline",
            modifier = Modifier.size(46.dp).padding(end = 16.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )

        Text(
            text = state.email,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
