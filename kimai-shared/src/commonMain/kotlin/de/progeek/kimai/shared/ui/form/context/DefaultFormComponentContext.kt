package de.progeek.kimai.shared.ui.form.context

import com.arkivanov.decompose.ComponentContext
import de.progeek.kimai.shared.ui.timesheet.models.TimesheetFormParams

class DefaultFormComponentContext(
    componentContext: ComponentContext,
    override val formParams: TimesheetFormParams
) : FormComponentContext, ComponentContext by componentContext
