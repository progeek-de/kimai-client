package de.progeek.kimai.shared.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.progeek.kimai.shared.ui.components.KimaiTopAppBar
import de.progeek.kimai.shared.ui.settings.components.*
import de.progeek.kimai.shared.ui.ticketsystem.settings.TicketSystemSettingsSection

/**
 * Main settings screen displaying user profile, preferences, and app information
 */
@Composable
fun SettingsContent(component: SettingsComponent) {
    Column(modifier = Modifier.fillMaxSize()) {
        KimaiTopAppBar(onBackClick = {
            component.onOutput()
        })
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            UserProfileSection(component)
            DefaultProjectSection(component)
            ThemeSection(component)
            LanguageSection(component)
            IssueInsertFormatSection(component)
            TicketSystemSettingsSection()
            VersionInfo()
        }
    }
}