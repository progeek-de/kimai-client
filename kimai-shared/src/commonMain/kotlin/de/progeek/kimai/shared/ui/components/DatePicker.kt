package de.progeek.kimai.shared.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.progeek.kimai.shared.SharedRes
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.datetime.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DatePicker(
    date: Instant,
    onDateChange: (Instant) -> Unit
) {
    var openDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = date.toEpochMilliseconds()
    )

    val dateFormatter = DatePickerFormatter(
        yearSelectionSkeleton = "MMMM YYYY",
        selectedDateSkeleton = "dd.MM.YYYY",
        selectedDateDescriptionSkeleton = "dd.MMMM YYYY"
    )

    DatePickerDialog(
        onDismissRequest = {
            onDateChange(date)
        },
        colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        confirmButton = {
            // confirmButton doesn't work atm (MaterialTheme3 bug)
        }
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    when (datePickerState.displayMode) {
                        DisplayMode.Picker -> stringResource(SharedRes.strings.pickDate)
                        DisplayMode.Input -> stringResource(SharedRes.strings.inputDate)
                        else -> {
                            ""
                        }
                    },
                    modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp)
                )
            },
            modifier = Modifier.weight(1f),
            dateFormatter = dateFormatter
        )

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Button(onClick = {
                onDateChange(Instant.fromEpochMilliseconds(datePickerState.selectedDateMillis ?: 0))
                openDatePicker = false
            }) {
                Text(stringResource(SharedRes.strings.ok))
            }
        }
    }
}
