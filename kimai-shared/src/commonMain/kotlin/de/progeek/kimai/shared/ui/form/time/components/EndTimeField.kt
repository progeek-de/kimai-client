package de.progeek.kimai.shared.ui.form.time.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import de.progeek.kimai.shared.SharedRes
import de.progeek.kimai.shared.ui.components.TextTimeField
import de.progeek.kimai.shared.utils.removeSeconds
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime

@Composable
internal fun EndTimeField(
    begin: LocalDateTime,
    end: LocalDateTime,
    snackbarHostState: SnackbarHostState,
    onEndChange: (LocalDateTime) -> Unit
) {
    val endBeforeStartText = stringResource(SharedRes.strings.end_before_start)
    val closeText = stringResource(SharedRes.strings.close)

    val scope = rememberCoroutineScope()

    TextTimeField(
        time = end,
        onChange = {
            onEndChange(
                if (begin.removeSeconds() > it) {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            endBeforeStartText,
                            actionLabel = closeText,
                            duration = SnackbarDuration.Short
                        )
                    }

                    begin
                } else {
                    it
                }
            )
        }
    )
}
