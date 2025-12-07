package de.progeek.kimai.shared.ui.form

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.progeek.kimai.shared.ui.components.KimaiTopAppBar
import de.progeek.kimai.shared.ui.form.components.Form
import de.progeek.kimai.shared.utils.rememberImeState

val FormComponentLocal = compositionLocalOf<FormComponent> {
    error("No EditComponent provided")
}

@Composable
fun FormScreen(component: FormComponent) {
    CompositionLocalProvider(FormComponentLocal provides component) {
        FormContent(component)
    }
}

@Composable
private fun FormContent(component: FormComponent) {
    val imeState = rememberImeState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = imeState.value) {
        if (imeState.value == true) {
            scrollState.scrollTo(scrollState.maxValue)
        }
    }
    Scaffold(
        topBar = {
            KimaiTopAppBar(onBackClick = {
                component.onOutput(FormComponent.Output.Close)
            })
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.BottomCenter)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.background).padding(padding)
        ) {
            Form(snackbarHostState)
        }
    }
}
