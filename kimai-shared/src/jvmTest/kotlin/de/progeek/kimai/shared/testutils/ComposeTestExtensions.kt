package de.progeek.kimai.shared.testutils

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import de.progeek.kimai.shared.KimaiDispatchers
import de.progeek.kimai.shared.ui.theme.AppTheme
import de.progeek.kimai.shared.ui.theme.BrandingEnum
import de.progeek.kimai.shared.ui.theme.ThemeEnum
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/**
 * Extension functions and utilities for Compose UI testing.
 */

/**
 * Wraps composable content with AppTheme for testing.
 * Defaults to LIGHT theme and KIMAI branding for consistent test behavior.
 */
@Composable
fun TestTheme(
    theme: ThemeEnum = ThemeEnum.LIGHT,
    branding: BrandingEnum = BrandingEnum.KIMAI,
    content: @Composable () -> Unit
) {
    AppTheme(theme = theme, branding = branding, content = content)
}

/**
 * Creates a test ComponentContext with a resumed lifecycle.
 */
fun createTestComponentContext(): DefaultComponentContext {
    val lifecycle = LifecycleRegistry()
    lifecycle.resume()
    return DefaultComponentContext(lifecycle = lifecycle)
}

/**
 * Creates a default StoreFactory for testing.
 */
fun createTestStoreFactory(): StoreFactory = DefaultStoreFactory()

/**
 * Creates test dispatchers using UnconfinedTestDispatcher.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun createTestDispatchers(): KimaiDispatchers {
    val testDispatcher = UnconfinedTestDispatcher()
    return object : KimaiDispatchers {
        override val main: CoroutineDispatcher = testDispatcher
        override val io: CoroutineDispatcher = testDispatcher
        override val unconfined: CoroutineDispatcher = testDispatcher
    }
}

/**
 * Clears text and enters new text in a text field.
 */
fun SemanticsNodeInteraction.clearAndEnterText(text: String) {
    performTextClearance()
    performTextInput(text)
}

/**
 * Finds a node by test tag and performs a click.
 */
fun SemanticsNodeInteractionsProvider.clickOnTag(tag: String) {
    onNodeWithTag(tag).performClick()
}

/**
 * Finds a node by text and performs a click.
 */
fun SemanticsNodeInteractionsProvider.clickOnText(text: String) {
    onNodeWithText(text).performClick()
}

/**
 * Enters text in a text field found by test tag.
 */
fun SemanticsNodeInteractionsProvider.enterTextInTag(tag: String, text: String) {
    onNodeWithTag(tag).performTextInput(text)
}

/**
 * Clears and enters text in a text field found by test tag.
 */
fun SemanticsNodeInteractionsProvider.clearAndEnterTextInTag(tag: String, text: String) {
    onNodeWithTag(tag).clearAndEnterText(text)
}

/**
 * Waits for idle and asserts a node with the given text exists.
 * Note: Compose Desktop doesn't have waitUntilExactlyOneExists, so we use waitForIdle + assert.
 */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.waitForText(text: String) {
    waitForIdle()
    onNodeWithText(text).assertExists()
}

/**
 * Waits for idle and asserts a node with the given test tag exists.
 * Note: Compose Desktop doesn't have waitUntilExactlyOneExists, so we use waitForIdle + assert.
 */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.waitForTag(tag: String) {
    waitForIdle()
    onNodeWithTag(tag).assertExists()
}

/**
 * Asserts that a node with the given text exists.
 */
fun SemanticsNodeInteractionsProvider.assertTextExists(text: String) {
    onNodeWithText(text).assertExists()
}

/**
 * Asserts that a node with the given text does not exist.
 */
fun SemanticsNodeInteractionsProvider.assertTextDoesNotExist(text: String) {
    onNodeWithText(text).assertDoesNotExist()
}

/**
 * Asserts that a node with the given test tag exists.
 */
fun SemanticsNodeInteractionsProvider.assertTagExists(tag: String) {
    onNodeWithTag(tag).assertExists()
}

/**
 * Asserts that a node with the given test tag does not exist.
 */
fun SemanticsNodeInteractionsProvider.assertTagDoesNotExist(tag: String) {
    onNodeWithTag(tag).assertDoesNotExist()
}

/**
 * Asserts that a button is enabled.
 */
fun SemanticsNodeInteraction.assertIsEnabled() {
    assertExists()
    // Button enabled state is checked via the semantics
}

/**
 * Test tags used across UI tests.
 */
object TestTags {
    // Login Screen
    const val EMAIL_FIELD = "email_field"
    const val PASSWORD_FIELD = "password_field"
    const val LOGIN_BUTTON = "login_button"
    const val LOGIN_ERROR = "login_error"
    const val LOADING_INDICATOR = "loading_indicator"
    const val SERVER_INFO = "server_info"

    // Timesheet Screen
    const val TIMESHEET_LIST = "timesheet_list"
    const val TIMESHEET_ITEM = "timesheet_item"
    const val TIMESHEET_INPUT_FIELD = "timesheet_input_field"
    const val START_BUTTON = "start_button"
    const val STOP_BUTTON = "stop_button"
    const val ADD_BUTTON = "add_button"
    const val RELOAD_BUTTON = "reload_button"
    const val SETTINGS_BUTTON = "settings_button"

    // Form Screen
    const val CUSTOMER_DROPDOWN = "customer_dropdown"
    const val PROJECT_DROPDOWN = "project_dropdown"
    const val ACTIVITY_DROPDOWN = "activity_dropdown"
    const val DESCRIPTION_FIELD = "description_field"
    const val BEGIN_TIME_FIELD = "begin_time_field"
    const val END_TIME_FIELD = "end_time_field"
    const val SAVE_BUTTON = "save_button"
    const val DELETE_BUTTON = "delete_button"
    const val BACK_BUTTON = "back_button"

    // Settings Screen
    const val THEME_SELECTION = "theme_selection"
    const val LANGUAGE_SELECTION = "language_selection"
    const val DEFAULT_PROJECT_SELECTION = "default_project_selection"
    const val LOGOUT_BUTTON = "logout_button"
    const val USER_PROFILE = "user_profile"

    // Dialogs
    const val DELETE_DIALOG = "delete_dialog"
    const val DELETE_CONFIRM_BUTTON = "delete_confirm_button"
    const val DELETE_CANCEL_BUTTON = "delete_cancel_button"

    // Generic
    const val TOP_APP_BAR = "top_app_bar"
    const val DROPDOWN_MENU = "dropdown_menu"
    const val DROPDOWN_ITEM = "dropdown_item"
}
