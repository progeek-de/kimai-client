package de.progeek.kimai.shared.utils

import java.awt.Desktop
import java.net.URI

actual fun browseUrl(url: String) {
    val desktop = Desktop.getDesktop()
    desktop.browse(URI.create(url))
}