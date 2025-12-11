package de.progeek.kimai.shared.ui.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.progeek.kimai.shared.ui.settings.SettingsComponent
import de.progeek.kimai.shared.utils.getLanguages

/**
 * Displays the logged-in user's email with a profile icon and language flags
 */
@Composable
fun UserProfileSection(component: SettingsComponent) {
    val state by component.state.collectAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
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

        Spacer(modifier = Modifier.weight(1f))

        LanguageFlags(
            selectedLanguageCode = state.language.languageCode,
            onLanguageSelected = { language -> component.onLanguageChange(language) }
        )
    }
}

/**
 * Displays language selection as flag icons
 */
@Composable
private fun LanguageFlags(
    selectedLanguageCode: String,
    onLanguageSelected: (de.progeek.kimai.shared.utils.Language) -> Unit
) {
    val languages = getLanguages()

    Row(verticalAlignment = Alignment.CenterVertically) {
        languages.forEach { language ->
            val languageLabel = language.languageCode.uppercase()
            val isSelected = language.languageCode == selectedLanguageCode
            val alpha = if (isSelected) 1f else 0.4f

            Text(
                text = languageLabel,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier
                    .clickable { onLanguageSelected(language) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = alpha)
            )

            if (language != languages.last()) {
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}