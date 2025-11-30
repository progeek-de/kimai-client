package de.progeek.kimai.shared.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.progeek.kimai.shared.ui.components.KimaiTopAppBar
import de.progeek.kimai.shared.ui.settings.components.*

/**
 * Main settings screen displaying user profile, preferences, and app information
 */
@Composable
fun SettingsContent(component: SettingsComponent) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        KimaiTopAppBar(onBackClick = {
            component.onOutput()
        })
        UserProfileSection(component)
        DefaultProjectSection(component)
        JiraIntegrationSection(component)
        ThemeSection(component)
        LanguageSection(component)
    }
    VersionInfo()
}
