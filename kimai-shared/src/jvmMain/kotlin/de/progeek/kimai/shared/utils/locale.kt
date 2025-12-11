package de.progeek.kimai.shared.utils

import java.util.*

actual fun setDefaultLocale(locale: String) {
    Locale.setDefault(Locale.of(locale, locale))
}
