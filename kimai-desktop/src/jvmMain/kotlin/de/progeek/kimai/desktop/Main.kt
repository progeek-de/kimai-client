package de.progeek.kimai.desktop

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.mvikotlin.core.utils.setMainThreadId
import com.arkivanov.mvikotlin.logging.store.LoggingStoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.arkivanov.mvikotlin.timetravel.store.TimeTravelStoreFactory
import de.progeek.kimai.shared.SharedRes
import de.progeek.kimai.shared.core.di.initKoin
import de.progeek.kimai.shared.kimaiDispatchers
import de.progeek.kimai.shared.ui.ContentView
import de.progeek.kimai.shared.ui.root.RootComponent
import dev.icerock.moko.resources.compose.painterResource
import java.awt.Dimension
import javax.swing.SwingUtilities

fun main() {
    initKoin()

    val root = invokeOnAwtSync {
        val storeFactory = when(BuildKonfig.DEBUG) {
            true -> LoggingStoreFactory(TimeTravelStoreFactory())
            else -> DefaultStoreFactory()
        }

        setMainThreadId(Thread.currentThread().id)
        val lifecycle = LifecycleRegistry()
        val rootComponent = RootComponent(
            DefaultComponentContext(lifecycle = lifecycle),
            storeFactory,
            kimaiDispatchers
        )
        lifecycle.resume()
        rootComponent
    }

    application {
        val windowState = rememberWindowState(height = 700.dp)
        var visibleInTray by remember { mutableStateOf(true) }
        val density = LocalDensity.current
        // Window icon is set at startup - uses default Kimai branding
        // The branding setting affects the in-app logo, not the window/tray icons
        val icon = painterResource(SharedRes.images.kimai_icon_orange).toAwtImage(
            density,
            LayoutDirection.Ltr,
            Size(128f, 128f)
        )

        fun notMinimized() {
            windowState.isMinimized  = false
            visibleInTray = true
        }

        fun putBottonClose(){
            visibleInTray = false
        }

        fun shouldExit(){
            exitApplication()
        }

        Window(
            onCloseRequest = :: putBottonClose,
            state = windowState,
            title = "Kimai",
            visible = visibleInTray,
            ) {
            SideEffect {
                // fix for taskbar icon resolution
                window.iconImage = icon
            }
            ContentView(root)
            TrayIcon(::notMinimized, ::shouldExit)
            window.minimumSize = Dimension(380,620)
        }
    }
}

fun <T> invokeOnAwtSync(block: () -> T): T {
    var result: T? = null
    SwingUtilities.invokeAndWait { result = block() }

    @Suppress("UNCHECKED_CAST")
    return result as T
}