package de.progeek.kimai.shared.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize

@Composable
fun <T> ItemDropDown(
    items: Array<T>,
    currentSelected: T?,
    required: Boolean,
    placeholder: String,
    mapItemToString: @Composable (T) -> (String),
    onItemClick: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var dropDownSize by remember { mutableStateOf(Size.Zero) }
    val density = LocalDensity.current

    Surface(
        shadowElevation = 8.dp,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.onSecondary,
        border = BorderStroke(
            1.dp,
            if (required && currentSelected == null) MaterialTheme.colorScheme.error else Color.Transparent
        ),
        modifier = Modifier.onGloballyPositioned { dropDownSize = it.size.toSize() }
    ) {
        Row(
            modifier = Modifier.clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                if (currentSelected != null) {
                    Text(mapItemToString(currentSelected), color = MaterialTheme.colorScheme.surfaceTint)
                } else {
                    Text(
                        placeholder,
                        color = if (required) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceTint
                    )
                }
            }

            Column {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = if (required && currentSelected == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceTint
                    )
                }
            }

            DropdownMenu(expanded, onDismissRequest = {
                expanded = false
            }, modifier = Modifier.width(with(density) { dropDownSize.width.toDp() }).background(MaterialTheme.colorScheme.secondaryContainer)) {
                items.forEach {
                    DropdownMenuItem(onClick = {
                        expanded = false
                        onItemClick(it)
                    }, text = {
                            Text(mapItemToString(it), color = MaterialTheme.colorScheme.surfaceTint)
                        })
                }
            }
        }
    }
}
