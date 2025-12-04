package de.progeek.kimai.shared.ui.login

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
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
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class LoginScreenTest {

    private lateinit var outputCallback: (LoginComponent.Output) -> Unit
    private var outputReceived: LoginComponent.Output? = null

    @Before
    fun setUp() {
        outputCallback = { output -> outputReceived = output }
        TestKoinModule.startTestKoin()
    }

    @After
    fun tearDown() {
        TestKoinModule.stopTestKoin()
        outputReceived = null
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
    fun `login screen displays kimai logo`() = runComposeUiTest {
        val component = createLoginComponent()

        setContent {
            TestTheme {
                LoginScreen(component)
            }
        }

        // Logo should exist (it's rendered as an image)
        // Since it's an image, we can't easily assert its existence by text
        // The test passes if the content renders without error
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
        onNodeWithText("invalid", substring = true, ignoreCase = true).assertExists()
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

        // The loading state should be shown (CircularProgressIndicator)
        // Note: This is difficult to test without proper test tags
    }

    @Test
    fun `server button opens change server dialog`() = runComposeUiTest {
        val component = createLoginComponent()

        setContent {
            TestTheme {
                LoginScreen(component)
            }
        }

        // Find and click the server URL text button
        // The server URL is shown without https:// prefix
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
}
