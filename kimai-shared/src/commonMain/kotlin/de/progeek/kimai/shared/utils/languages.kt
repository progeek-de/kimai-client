package de.progeek.kimai.shared.utils

import de.progeek.kimai.shared.SharedRes
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc

data class Language(val name: StringResource, val languageCode: String)

fun getLanguages() = arrayOf(Language(SharedRes.strings.english, "en"), Language(SharedRes.strings.german, "de"))