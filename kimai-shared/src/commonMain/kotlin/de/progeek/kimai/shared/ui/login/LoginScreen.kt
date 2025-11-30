package de.progeek.kimai.shared.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.progeek.kimai.shared.ui.components.KimaiLogo
import de.progeek.kimai.shared.utils.rememberImeState

val LoginComponentLocal = compositionLocalOf<LoginComponent> {
    error("No LoginComponent provided")
}

@Composable
fun LoginScreen(component: LoginComponent) {
    CompositionLocalProvider(LoginComponentLocal provides component) {
        LoginContent()
    }
}

@Composable
private fun LoginContent() {
    val imeState = rememberImeState()
    val scrollState = rememberScrollState()

    LaunchedEffect(key1 = imeState.value) {
        if (imeState.value == true) {
            scrollState.scrollTo(scrollState.maxValue)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        KimaiLogo(modifier = Modifier.size(125.dp, 125.dp))
        LoginCard()
        Footer()
    }
}
