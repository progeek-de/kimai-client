package de.progeek.kimai.shared.ui.root.store

import com.arkivanov.mvikotlin.core.store.Store
import de.progeek.kimai.shared.core.models.Credentials
import de.progeek.kimai.shared.ui.root.store.RootStore.*
import de.progeek.kimai.shared.ui.theme.BrandingEnum
import de.progeek.kimai.shared.ui.theme.ThemeEnum

interface RootStore : Store<Nothing, State, Nothing> {
    data class State(
        val credentials: Credentials?,
        val isLoading: Boolean,
        val theme: ThemeEnum,
        val branding: BrandingEnum = BrandingEnum.KIMAI
    )
}
