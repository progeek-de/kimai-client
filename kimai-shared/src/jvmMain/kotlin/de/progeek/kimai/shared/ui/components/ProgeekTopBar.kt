package de.progeek.kimai.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.progeek.kimai.shared.SharedRes
import dev.icerock.moko.resources.compose.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun KimaiTopAppBar(
    onBackClick: () -> Unit
) {
    Surface(
        shadowElevation = 4.dp,
        modifier = Modifier.background(Color.Transparent)
    ) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            title = {
                KimaiLogo(
                    modifier = Modifier.height(35.dp)
                )
            },
            navigationIcon = { },
            actions = {
                Button(
                    onClick = onBackClick,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(stringResource(SharedRes.strings.back), color = Color.White)
                }
            }
        )
    }
}
