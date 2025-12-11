package de.progeek.kimai.shared.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import de.progeek.kimai.shared.ui.root.RootComponent
import de.progeek.kimai.shared.ui.root.RootContent
import de.progeek.kimai.shared.ui.theme.AppTheme

@Composable
fun ContentView(component: RootComponent) {
    val state by component.state.collectAsState()

    // Use language as key to force recomposition when language changes
    key(state.languageCode) {
        AppTheme(state.theme, state.branding) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                RootContent(component)
            }
        }
    }
}
