package de.progeek.kimai.desktop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Tray
import de.progeek.kimai.shared.SharedRes
import de.progeek.kimai.shared.core.repositories.timesheet.TimesheetRepository
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private class PaddedPainter(
    private val inner: Painter,
    private val scale: Float,
) : Painter() {
    override val intrinsicSize: Size get() = inner.intrinsicSize

    override fun DrawScope.onDraw() {
        val w = size.width * scale
        val h = size.height * scale
        val dx = (size.width - w) / 2f
        val dy = (size.height - h) / 2f
        translate(dx, dy) {
            with(inner) { draw(Size(w, h)) }
        }
    }
}

@Composable
fun ApplicationScope.TrayIcon(onShow: () -> Unit, shouldExit: () -> Unit) {
    val exit = stringResource(SharedRes.strings.exit)
    val showApp = stringResource(SharedRes.strings.show)

    val timesheetRepository = koinInject<TimesheetRepository>()
    val scope = rememberCoroutineScope()

    val runningTimesheet by timesheetRepository.getRunningTimesheetStream()
        .collectAsState(initial = null)

    val iconRes = if (runningTimesheet == null) {
        SharedRes.images.kimai_icon_white
    } else {
        SharedRes.images.kimai_icon_orange
    }
    val basePainter = painterResource(iconRes)
    val icon = remember(basePainter) { PaddedPainter(basePainter, scale = 0.65f) }

    Tray(
        icon = icon,
        tooltip = "Kimai",
        onAction = onShow,
        menu = {
            val running = runningTimesheet
            if (running != null && running.id != null && running.project != null && running.activity != null) {
                Item("Stop", onClick = {
                    scope.launch {
                        timesheetRepository.stopTimesheet(running.id!!)
                    }
                })
            }
            Item(showApp, onClick = onShow)
            Separator()
            Item(exit, onClick = shouldExit)
        }
    )
}
