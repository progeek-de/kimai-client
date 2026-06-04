package de.progeek.kimai.shared.ui.login

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import de.progeek.kimai.shared.testutils.TestData
import de.progeek.kimai.shared.testutils.TestKoinModule
import de.progeek.kimai.shared.testutils.TestTheme
import de.progeek.kimai.shared.testutils.createTestComponentContext
import de.progeek.kimai.shared.testutils.createTestDispatchers
import de.progeek.kimai.shared.testutils.createTestStoreFactory
import dev.icerock.moko.resources.desc.StringDesc
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Locale

@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class LoginScreenTest {

    private lateinit var outputCallback: (LoginComponent.Output) -> Unit
    private var outputReceived: LoginComponent.Output? = null

    @Before
    fun setUp() {
        outputCallback = { output -> outputReceived = output }
        // Pin locale to English so stringResource() assertions are deterministic.
        StringDesc.localeType = StringDesc.LocaleType.Custom("en")
        Locale.setDefault(Locale.ENGLISH)
        TestKoinModule.startTestKoin()
    }

    @After
    fun tearDown() {
        TestKoinModule.stopTestKoin()
        outputReceived = null
        StringDesc.localeType = StringDesc.LocaleType.Custom("en")
        Locale.setDefault(Locale.ENGLISH)
    }

    private fun createLoginComponent(): LoginComponent {
        return LoginComponent(
            componentContext = createTestComponentContext(),
            storeFactory = createTestStoreFactory(),
            dispatchers = createTestDispatchers(),
            output = outputCallback
        )
    }

    @Test
    fun `login screen displays email and password fields`() = runComposeUiTest {
        val component = createLoginComponent()

        setContent {
            TestTheme {
                LoginScreen(component)
            }
        }

        // Verify email field exists (by checking for the label text)
        onNodeWithText("E-Mail", substring = true, ignoreCase = true).assertExists()
        // Verify password field exists
        onNodeWithText("Password", substring = true, ignoreCase = true).assertExists()
    }

    @Test
    fun `login screen displays login button`() = runComposeUiTest {
        val component = createLoginComponent()

        setContent {
            TestTheme {
                LoginScreen(component)
            }
        }

        // Verify login button exists
        onNodeWithText("LOGIN", ignoreCase = true).assertExists()
    }

    @Test
    fun `login button is disabled when fields are empty`() = runComposeUiTest {
        val component = createLoginComponent()

        setContent {
            TestTheme {
                LoginScreen(component)
            }
        }

        // Login button should be disabled when email and password are empty
        onNodeWithText("LOGIN", ignoreCase = true).assertIsNotEnabled()
    }

    @Test
    fun `login button is enabled when valid email and password are entered`() = runComposeUiTest {
        val component = createLoginComponent()

        setContent {
            TestTheme {
                LoginScreen(component)
            }
        }

        // Enter valid email
        onNodeWithText("E-Mail", substring = true, ignoreCase = true).performTextInput("test@example.com")
        // Enter password
        onNodeWithText("Password", substring = true, ignoreCase = true).performTextInput("password123")

        // Login button should be enabled
        onNodeWithText("LOGIN", ignoreCase = true).assertIsEnabled()
    }

    @Test
    fun `login button remains disabled with invalid email format`() = runComposeUiTest {
        val component = createLoginComponent()

        setContent {
            TestTheme {
                LoginScreen(component)
            }
        }

        // Enter invalid email (no @ sign)
        onNodeWithText("E-Mail", substring = true, ignoreCase = true).performTextInput("invalidemail")
        // Enter password
        onNodeWithText("Password", substring = true, ignoreCase = true).performTextInput("password123")

        // Login button should remain disabled due to invalid email
        onNodeWithText("LOGIN", ignoreCase = true).assertIsNotEnabled()
    }

    @Test
    fun `login screen displays server info`() = runComposeUiTest {
        val component = createLoginComponent()

        setContent {
            TestTheme {
                LoginScreen(component)
            }
        }

        // Verify server info text exists
        onNodeWithText("Server:", substring = true).assertExists()
    }

    @Test
    fun `login screen displays version info`() = runComposeUiTest {
        val component = createLoginComponent()

        setContent {
            TestTheme {
                LoginScreen(component)
            }
        }

        // The version should be displayed in the footer
        // Note: The actual version comes from BuildKonfig.KIMAI_VER
        onNodeWithText("Version", substring = true, ignoreCase = true).assertExists()
    }

    @Test
    fun `login screen renders without errors`() = runComposeUiTest {
        val component = createLoginComponent()

        setContent {
            TestTheme {
                LoginScreen(component)
            }
        }

        // Verify essential UI elements are rendered
        onNodeWithTag("login_button").assertExists()
        onNodeWithTag("email_input_field").assertExists()
        onNodeWithTag("password_input_field").assertExists()
    }

    @Test
    fun `clicking login button triggers login intent`() = runComposeUiTest {
        val authRepository = TestKoinModule.createMockAuthRepository(loginSuccess = true)
        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(authRepository = authRepository)

        val component = createLoginComponent()

        setContent {
            TestTheme {
                LoginScreen(component)
            }
        }

        // Enter valid credentials
        onNodeWithText("E-Mail", substring = true, ignoreCase = true).performTextInput("test@example.com")
        onNodeWithText("Password", substring = true, ignoreCase = true).performTextInput("password123")

        // Click login button
        onNodeWithText("LOGIN", ignoreCase = true).performClick()

        // Wait for async operations
        waitForIdle()

        // Verify login was attempted (AuthRepository.login was called)
        coVerify(timeout = 2000) { authRepository.login(any(), any(), any()) }
    }

    @Test
    fun `invalid email shows error message`() = runComposeUiTest {
        val component = createLoginComponent()

        setContent {
            TestTheme {
                LoginScreen(component)
            }
        }

        // Enter invalid email format
        onNodeWithText("E-Mail", substring = true, ignoreCase = true).performTextInput("notanemail")

        // Should show invalid email error
        onNodeWithTag("email_error_text").assertExists()
    }

    @Test
    fun `login screen shows loading indicator when logging in`() = runComposeUiTest {
        // Create a component where login takes some time
        val authRepository = TestKoinModule.createMockAuthRepository(loginSuccess = true)
        coEvery { authRepository.login(any(), any(), any()) } coAnswers {
            kotlinx.coroutines.delay(1000)
            TestData.validCredentials
        }
        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(authRepository = authRepository)

        val component = createLoginComponent()

        setContent {
            TestTheme {
                LoginScreen(component)
            }
        }

        // Enter credentials
        onNodeWithText("E-Mail", substring = true, ignoreCase = true).performTextInput("test@example.com")
        onNodeWithText("Password", substring = true, ignoreCase = true).performTextInput("password123")

        // Click login
        onNodeWithText("LOGIN", ignoreCase = true).performClick()

        // Verify loading indicator is shown
        onNodeWithTag("login_progress_indicator").assertExists()
    }

    @Test
    fun `server button opens change server dialog`() = runComposeUiTest {
        val component = createLoginComponent()

        setContent {
            TestTheme {
                LoginScreen(component)
            }
        }

        onNodeWithText("kimai.cloud", substring = true).performClick()

        // Dialog should appear with Host field
        onNodeWithText("Host", ignoreCase = true).assertExists()
    }

    @Test
    fun `cancel button in server dialog closes dialog`() = runComposeUiTest {
        val component = createLoginComponent()

        setContent {
            TestTheme {
                LoginScreen(component)
            }
        }

        // Open server dialog
        onNodeWithText("kimai.cloud", substring = true).performClick()

        // Wait for dialog
        waitForIdle()

        // Click cancel
        onNodeWithText("Cancel", ignoreCase = true).performClick()

        // Dialog should be closed - Host field should not exist
        waitForIdle()
        onNodeWithText("Host", ignoreCase = true).assertDoesNotExist()
    }

    @Test
    fun `failed login shows invalid login error text`() = runComposeUiTest {
        val authRepository = TestKoinModule.createMockAuthRepository(loginSuccess = false)
        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(authRepository = authRepository)

        val component = createLoginComponent()

        setContent {
            TestTheme {
                LoginScreen(component)
            }
        }

        // Drive the login intent directly so the failure path is exercised reliably
        // (clicking would also work, but driving avoids relying on the enabled-state timing).
        component.onLoginClick("test@example.com", "wrongpassword")

        waitForIdle()

        // The error branch (state.isError) should render the invalid login message.
        onNodeWithTag("login_error_text").assertExists()
        onNodeWithText("Invalid login or password").assertExists()
    }

    @Test
    fun `successful login does not show error text`() = runComposeUiTest {
        val authRepository = TestKoinModule.createMockAuthRepository(loginSuccess = true)
        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(authRepository = authRepository)

        val component = createLoginComponent()

        setContent {
            TestTheme {
                LoginScreen(component)
            }
        }

        component.onLoginClick("test@example.com", "password123")

        waitForIdle()

        // No error message on success.
        onNodeWithTag("login_error_text").assertDoesNotExist()
    }

    @Test
    fun `password is masked by default and toggle reveals it`() = runComposeUiTest {
        val component = createLoginComponent()

        setContent {
            TestTheme {
                LoginScreen(component)
            }
        }

        // Default: visibility toggle offers to "Show password".
        onNode(hasContentDescription("Show password")).assertExists()
        onNode(hasContentDescription("Hide password")).assertDoesNotExist()

        // Click the trailing visibility toggle.
        onNode(hasContentDescription("Show password")).performClick()
        waitForIdle()

        // Now it offers to "Hide password" (password is revealed).
        onNode(hasContentDescription("Hide password")).assertExists()
        onNode(hasContentDescription("Show password")).assertDoesNotExist()
    }

    @Test
    fun `changing base url updates host button label`() = runComposeUiTest {
        val component = createLoginComponent()

        setContent {
            TestTheme {
                LoginScreen(component)
            }
        }

        // Drive a base-url change directly; the host button strips the scheme.
        component.changedBaseUrl("https://my.custom-host.example")

        waitForIdle()

        onNodeWithText("my.custom-host.example", substring = true).assertExists()
    }

    @Test
    fun `server dialog ok button is disabled and shows error for invalid url`() = runComposeUiTest {
        val component = createLoginComponent()

        setContent {
            TestTheme {
                LoginScreen(component)
            }
        }

        // Open the change-server dialog.
        onNodeWithText("kimai.cloud", substring = true).performClick()
        waitForIdle()

        // Enter an invalid URL into the host field; OK should be disabled and the
        // invalid-url supporting text should show.
        onNodeWithTag("dialog_host_input").performTextInput("not a url")
        waitForIdle()

        onNodeWithText("OK").assertIsNotEnabled()
        onNodeWithText("Invalid URL", substring = true).assertExists()
    }
}
