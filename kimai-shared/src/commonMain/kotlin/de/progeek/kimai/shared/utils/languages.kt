package de.progeek.kimai.shared.utils

import de.progeek.kimai.shared.SharedRes
import dev.icerock.moko.resources.StringResource

data class Language(val name: StringResource, val languageCode: String)

fun getLanguages() = arrayOf(Language(SharedRes.strings.english, "en"), Language(SharedRes.strings.german, "de"))
