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
internal fun BeginTimeField(begin: LocalDateTime,
                            end: LocalDateTime,
                            snackbarHostState: SnackbarHostState,
                            onBeginChange: (LocalDateTime) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val startAfterEndText = stringResource(SharedRes.strings.start_after_end)
    val closeText = stringResource(SharedRes.strings.close)

    TextTimeField(
        time = begin,
        onChange = {
            onBeginChange(
                if (end.removeSeconds() < it) {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            startAfterEndText,
                            actionLabel = closeText,
                            duration = SnackbarDuration.Short
                        )
                    }

                    end
                } else
                    it
            )
        }
    )
}