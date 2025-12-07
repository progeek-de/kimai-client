package de.progeek.kimai.shared.ui.form.customer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.progeek.kimai.shared.SharedRes
import de.progeek.kimai.shared.ui.components.ItemDropDown
import de.progeek.kimai.shared.ui.form.customer.CustomerFieldStore.Intent.SelectedCustomer
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun CustomerField(
    component: CustomerFieldComponent
) {
    val state by component.state.collectAsState()

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, bottom = 6.dp)
    ) {
        Text(
            stringResource(SharedRes.strings.customer),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        ItemDropDown(
            state.customers.toTypedArray(),
            currentSelected = state.selectedCustomer,
            mapItemToString = { it.name },
            required = false,
            placeholder = stringResource(SharedRes.strings.select_customer)
        ) {
            component.onIntent(SelectedCustomer(it))
        }
    }
}
