package de.progeek.kimai.desktop

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.painter.Painter
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
import java.awt.Toolkit
import javax.imageio.ImageIO
import javax.swing.SwingUtilities
import java.awt.Window as AwtWindow

// Reference to the main application window for activation from other instances
private var mainWindow: AwtWindow? = null

// Single instance manager
private var singleInstanceManager: SingleInstanceManager? = null

/**
 * Activates (brings to foreground) the main application window.
 * Called when another instance of the application tries to start.
 */
private fun activateWindow() {
    mainWindow?.let { window ->
        if (!window.isVisible) {
            window.isVisible = true
        }
        window.toFront()
        window.requestFocus()
    }
}

// Pre-load icon from resources for AWT (before Compose initialization)
private val appIcon by lazy {
    try {
        val iconStream = Thread.currentThread().contextClassLoader
            .getResourceAsStream("kimai_logo.png")
        iconStream?.let { ImageIO.read(it) }
    } catch (e: Exception) {
        null
    }
}

fun main() {
    // Set the application class name for Linux taskbar icon support
    // This MUST be done before ANY AWT/Swing initialization including Toolkit.getDefaultToolkit()
    System.setProperty("sun.awt.X11.XToolkit.awtAppClassName", "kimai")

    // Also try setting via Toolkit for older Java versions
    try {
        val toolkit = Toolkit.getDefaultToolkit()
        val awtAppClassNameField = toolkit.javaClass.getDeclaredField("awtAppClassName")
        awtAppClassNameField.isAccessible = true
        awtAppClassNameField.set(toolkit, "kimai")
    } catch (_: Exception) {
        // Ignore - not all JVMs support this
    }

    // Check if another instance is already running
    if (SingleInstanceManager.tryActivateExistingInstance()) {
        // Another instance is running and has been activated
        println("Another instance of Kimai is already running. Activating existing window.")
        return
    }

    // Start single instance server to listen for activation requests
    singleInstanceManager = SingleInstanceManager(onActivate = ::activateWindow)
    if (!singleInstanceManager!!.startServer()) {
        println("Warning: Could not start single instance server. Multiple instances may be possible.")
    }

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
        // Window icon is set at startup - uses default Kimai branding
        // The branding setting affects the in-app logo, not the window/tray icons
        // Icon must be passed directly to Window for Linux compatibility (CMP-7194)
        val icon: Painter = painterResource(SharedRes.images.kimai_logo)

        fun notMinimized() {
            windowState.isMinimized  = false
            visibleInTray = true
        }

        fun putBottonClose(){
            visibleInTray = false
        }

        fun shouldExit(){
            singleInstanceManager?.close()
            exitApplication()
        }

        Window(
            onCloseRequest = :: putBottonClose,
            state = windowState,
            title = "Kimai",
            visible = visibleInTray,
            icon = icon,
        ) {
            // Store window reference and set AWT icon for better Linux taskbar support
            LaunchedEffect(Unit) {
                mainWindow = window
                appIcon?.let { window.iconImage = it }
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
