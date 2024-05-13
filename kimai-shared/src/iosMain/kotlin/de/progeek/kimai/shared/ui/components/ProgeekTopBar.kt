package de.progeek.kimai.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun KimaiTopAppBar(
    onBackClick: () -> Unit
) {
    Surface(
        shadowElevation = 8.dp,
        modifier = Modifier.background(Color.Transparent).padding(bottom = 24.dp)
    ) {
        CenterAlignedTopAppBar(
            colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            title = {
                KimaiLogo(
                    modifier = Modifier.size(140.dp, (140 / 4).dp)
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )
    }
}