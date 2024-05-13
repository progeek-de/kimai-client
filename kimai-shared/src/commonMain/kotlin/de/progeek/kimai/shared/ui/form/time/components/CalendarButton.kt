package de.progeek.kimai.shared.ui.form.time.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.progeek.kimai.shared.SharedRes
import de.progeek.kimai.shared.ui.components.DatePicker
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CalendarButton(
    begin: LocalDateTime,
    end: LocalDateTime,
    onBeginChange: (LocalDateTime) -> Unit,
    onEndChange: (LocalDateTime) -> Unit,
) {
    var openDatePicker by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.wrapContentHeight(align = Alignment.CenterVertically)
    ) {
        IconButton(
            onClick = { openDatePicker = !openDatePicker }
        ) {
            Icon(
                tint = MaterialTheme.colorScheme.surfaceTint,
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = stringResource(SharedRes.strings.refresh)
            )
        }
    }

    if (openDatePicker) {
        DatePicker(begin.toInstant(TimeZone.currentSystemDefault())) {
            val newDate = it.toLocalDateTime(TimeZone.currentSystemDefault()).date

            onBeginChange(LocalDateTime(newDate, begin.time))
            onEndChange(LocalDateTime(newDate, end.time))

            openDatePicker = false
        }
    }
}