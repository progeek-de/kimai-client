package de.progeek.kimai.shared.ui.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.progeek.kimai.shared.SharedRes
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun LoginCard() {
    val component = LoginComponentLocal.current
    val state by component.state.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var isEmailValid by remember { mutableStateOf(true) }

    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }

    Card(
        modifier = Modifier.padding(32.dp).shadow(
            elevation = 4.dp,
            shape = MaterialTheme.shapes.small
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)

    ) {
        Column(modifier = Modifier.width(400.dp).padding(30.dp, 50.dp)) {
            OutlinedTextField(
                value = email,
                onValueChange = {
                    if (it.contains('\t')) {
                        passwordFocusRequester.requestFocus()
                    } else if (it.contains('\n')) {
                        if (isEmailValid && email.isNotEmpty() && password.isNotEmpty()) {
                            component.onLoginClick(email, password)
                        }
                    } else {
                        email = it
                        isEmailValid = isValidEmail(it)
                    }
                },
                label = { Text(stringResource(SharedRes.strings.email)) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).focusRequester(emailFocusRequester)
            )

            OutlinedTextField(
                value = password,
                onValueChange = {
                    if (it.contains('\t')) {
                        emailFocusRequester.requestFocus()
                    } else if (it.contains('\n')) {
                        if (isEmailValid && email.isNotEmpty() && password.isNotEmpty()) {
                            component.onLoginClick(email, password)
                        }
                    } else {
                        password = it
                    }
                },
                label = { Text(stringResource(SharedRes.strings.password)) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).focusRequester(passwordFocusRequester),
                visualTransformation = PasswordVisualTransformation()
            )

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Button(
                    onClick = { component.onLoginClick(email, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    shape = MaterialTheme.shapes.small,
                    enabled = isEmailValid && email.isNotBlank() && password.isNotBlank()
                ) {
                    Text(stringResource(SharedRes.strings.login).uppercase())
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }

                if (state.isError) {
                    Text(stringResource(SharedRes.strings.invalid_login), color = MaterialTheme.colorScheme.error)
                }

                if (!isEmailValid) {
                    Text(stringResource(SharedRes.strings.invalid_email), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

fun isValidEmail(email: String): Boolean {
    val emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}".toRegex()
    return emailPattern.matches(email) || email.isEmpty()
}

@Composable
fun Footer() {
    val component = LoginComponentLocal.current
    val state by component.state.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(4.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ServerInfo()
            Text(
                text = stringResource(SharedRes.strings.version, state.version),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun ServerInfo() {
    val component = LoginComponentLocal.current
    val state by component.state.collectAsState()

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Server:",
                    style = MaterialTheme.typography.bodyMedium
                )
                HostButton()
            }
        }
    }
}

@Composable
fun HostButton() {
    val component = LoginComponentLocal.current
    val state by component.state.collectAsState()
    var dialogOpen by remember { mutableStateOf(false) }

    TextButton(
        onClick = { dialogOpen = true },
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            state.baseUrl
                .replace("https://", "")
                .replace("http://", "")
        )
        Icon(
            imageVector = Icons.Rounded.ExpandMore,
            contentDescription = "open",
            modifier = Modifier.size(24.dp)
        )
    }

    if (dialogOpen) {
        ChangeBaseUrlDialog(
            baseUrl = state.baseUrl,
            onDismiss = { dialogOpen = false }
        ) {
            component.changedBaseUrl(it)
            dialogOpen = false
        }
    }
}

@Composable
fun ChangeBaseUrlDialog(
    baseUrl: String,
    onDismiss: () -> Unit,
    onChange: (baseUrl: String) -> Unit
) {
    var host by remember { mutableStateOf(baseUrl) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.widthIn(max = 390.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.background,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        value = host,
                        onValueChange = { host = it },
                        label = { Text("Host") }
                    )
                }

                Divider(color = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.padding(vertical = 16.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            stringResource(SharedRes.strings.cancel),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Button(
                        modifier = Modifier.padding(start = 8.dp),
                        shape = MaterialTheme.shapes.small,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        onClick = { onChange(host) }
                    ) {
                        Text(
                            stringResource(SharedRes.strings.ok),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
