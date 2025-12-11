package de.progeek.kimai.shared.ui.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
    var passwordVisible by remember { mutableStateOf(false) }

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
                onValueChange = { email = it; isEmailValid = isValidEmail(it) },
                label = { Text(stringResource(SharedRes.strings.email)) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).focusRequester(emailFocusRequester).testTag("email_input_field"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { passwordFocusRequester.requestFocus() })
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(SharedRes.strings.password)) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).focusRequester(passwordFocusRequester).testTag("password_input_field"),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (isEmailValid && email.isNotEmpty() && password.isNotEmpty()) {
                        component.onLoginClick(email, password)
                    }

                }),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            )

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Button(
                    onClick = { component.onLoginClick(email, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                        .testTag("login_button"),
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
                    CircularProgressIndicator(modifier = Modifier.size(32.dp).testTag("login_progress_indicator"))
                }

                if (state.isError) {
                    Text(stringResource(SharedRes.strings.invalid_login), color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("login_error_text"))
                }

                if (!isEmailValid) {
                    Text(stringResource(SharedRes.strings.invalid_email), color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("email_error_text"))
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
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.testTag("host_button")
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
                        modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("dialog_host_input"),
                        value = host,
                        onValueChange = { host = it },
                        label = { Text("Host") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { onChange(host) })
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.padding(vertical = 16.dp))

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