@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package de.progeek.kimai.ios

import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.stop
import de.progeek.kimai.shared.ui.root.RootComponent
import de.progeek.kimai.shared.ui.root.RootContent
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectBase
import kotlinx.cinterop.autoreleasepool
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toCValues
import platform.Foundation.NSStringFromClass
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDelegateProtocol
import platform.UIKit.UIApplicationDelegateProtocolMeta
import platform.UIKit.UIApplicationMain
import platform.UIKit.UIResponder
import platform.UIKit.UIResponderMeta
import platform.UIKit.UIScreen
import platform.UIKit.UIWindow

fun main() {
    val args = emptyArray<String>()
    memScoped {
        val argc = args.size + 1
        val argv = (arrayOf("skikoApp") + args).map { it.cstr.ptr }.toCValues()
        autoreleasepool {
            UIApplicationMain(argc, argv, null, NSStringFromClass(SkikoAppDelegate))
        }
    }
}

class SkikoAppDelegate @ObjCObjectBase.OverrideInit constructor() : UIResponder(), UIApplicationDelegateProtocol {
    companion object : UIResponderMeta(), UIApplicationDelegateProtocolMeta

    private val lifecycle = LifecycleRegistry()

    private val root = RootComponent(
        componentContext = DefaultComponentContext(lifecycle = lifecycle),
    )

    private var _window: UIWindow? = null
    override fun window() = _window
    override fun setWindow(window: UIWindow?) {
        _window = window
    }

    override fun application(
        application: UIApplication,
        didFinishLaunchingWithOptions: Map<Any?, *>?,
    ): Boolean {
        window = UIWindow(frame = UIScreen.mainScreen.bounds)

        window!!.rootViewController = ComposeUIViewController {
            RootContent(component = root)
        }
        window!!.makeKeyAndVisible()
        return true
    }

    override fun applicationDidBecomeActive(application: UIApplication) {
        lifecycle.resume()
    }

    override fun applicationWillResignActive(application: UIApplication) {
        lifecycle.stop()
    }

    override fun applicationWillTerminate(application: UIApplication) {
        lifecycle.destroy()
    }
}
