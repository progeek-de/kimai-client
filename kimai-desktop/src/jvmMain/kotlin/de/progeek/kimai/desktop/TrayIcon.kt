package de.progeek.kimai.desktop

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import de.progeek.kimai.shared.SharedRes
import de.progeek.kimai.shared.core.models.TimesheetForm
import de.progeek.kimai.shared.core.repositories.project.ProjectRepository
import de.progeek.kimai.shared.core.repositories.settings.SettingsRepository
import de.progeek.kimai.shared.core.repositories.timesheet.TimesheetRepository
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import dorkbox.systemTray.MenuItem
import dorkbox.systemTray.Separator
import dorkbox.systemTray.SystemTray
import kotlinx.coroutines.launch
import org.koin.compose.rememberKoinInject

@Composable
fun TrayIcon(onShow: () -> Unit, shouldExit: () -> Unit) {
    val exit = stringResource(SharedRes.strings.exit)
    val startTimer = stringResource(SharedRes.strings.start_timer)
    val showApp = stringResource(SharedRes.strings.show)

    val timesheetRepository = rememberKoinInject<TimesheetRepository>()
    val projectRepository = rememberKoinInject<ProjectRepository>()
    val settingsRepository = rememberKoinInject<SettingsRepository>()
    val scope = rememberCoroutineScope()

    val density = LocalDensity.current
    val icon = painterResource(SharedRes.images.progeek_icon).toAwtImage(
        density,
        LayoutDirection.Ltr,
        Size(128f, 128f)
    )
    val idleIcon =
        painterResource(SharedRes.images.progeek_icon_weiss).toAwtImage(
            density,
            LayoutDirection.Ltr,
            Size(128f, 128f)
        )

    val tray by remember { mutableStateOf(SystemTray.get()) }

    fun clearAll() {
        tray.menu.first?.let { tray.menu.remove(it) }
        tray.menu.first?.let { tray.menu.remove(it) }
        tray.menu.first?.let { tray.menu.remove(it) }
    }

    fun addShow() {
        tray.menu.add(MenuItem(showApp).apply {
            setCallback { onShow() }
        })
    }

    fun closeApp() {
        tray.menu.add(Separator())
        tray.menu.add(MenuItem(exit).apply {
            setCallback { shouldExit(); tray.shutdown() }
        })
    }

    fun stopTimer(timesheet: TimesheetForm) {
        scope.launch {
            timesheetRepository.stopTimesheet(timesheet.id!!)
        }
    }

    scope.launch {
        timesheetRepository.getRunningTimesheetStream().collect { timesheet ->
            clearAll()

            if (timesheet == null) {
                tray.setImage(idleIcon)
                //tray.menu.add(MenuItem(startTimer).apply {
                //    setCallback { startTimer() }
                //})
                addShow()
                closeApp()
            } else {
                clearAll()
                tray.setImage(icon)
                // all required values are set -> stoppable
                if (timesheet.id != null && timesheet.project != null && timesheet.activity != null) {
                    tray.menu.add(MenuItem("Stop").apply {
                        setCallback { stopTimer(timesheet) }
                    })
                    addShow()
                    closeApp()
                } else {
                    addShow()
                    closeApp()
                }
            }
        }
    }
}