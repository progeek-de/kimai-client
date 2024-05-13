package de.progeek.kimai.shared.ui.home.context

import com.arkivanov.decompose.ComponentContext
import de.progeek.kimai.shared.core.models.Credentials

class DefaultAuthComponentContext(
    componentContext: ComponentContext,
    override val credentials: Credentials,
    override val baseUrl: String
) : AuthComponentContext, ComponentContext by componentContext