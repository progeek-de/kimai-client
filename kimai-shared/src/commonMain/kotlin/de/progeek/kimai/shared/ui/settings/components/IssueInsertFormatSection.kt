package de.progeek.kimai.shared.ui.settings.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import de.progeek.kimai.shared.core.ticketsystem.models.IssueInsertFormat
import de.progeek.kimai.shared.ui.components.ItemDropDown
import de.progeek.kimai.shared.ui.settings.SettingsComponent

data class FormatOption(val name: String, val value: IssueInsertFormat)

/**
 * Section for selecting the issue insert format in timesheet descriptions
 */
@Composable
fun IssueInsertFormatSection(component: SettingsComponent) {
    val state by component.state.collectAsState()
    val formatOptions = arrayOf(
        FormatOption("Summary #KEY", IssueInsertFormat.SUMMARY_HASH_KEY),
        FormatOption("KEY: Summary", IssueInsertFormat.KEY_COLON_SUMMARY),
        FormatOption("#KEY only", IssueInsertFormat.KEY_ONLY)
    )

    SettingsField(label = "Issue Format") {
        ItemDropDown(
            formatOptions,
            formatOptions.find { it.value == state.issueInsertFormat },
            false,
            "Select format",
            mapItemToString = { it.name }
        ) {
            component.onIssueInsertFormatChange(it.value)
        }
    }
}
