@file:OptIn(ExperimentalTestApi::class)

package de.progeek.kimai.shared.ui.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.runComposeUiTest
import de.progeek.kimai.shared.testutils.TestTheme
import de.progeek.kimai.shared.ui.theme.BrandingEnum
import de.progeek.kimai.shared.ui.theme.ThemeEnum
import org.junit.Test

class KimaiIconTest {

    // Covers branch: useProgeek && theme == LIGHT -> progeek_dark
    @Test
    fun `renders progeek dark icon for progeek branding in light theme`() = runComposeUiTest {
        setContent {
            TestTheme(theme = ThemeEnum.LIGHT, branding = BrandingEnum.PROGEEK) {
                KimaiIcon()
            }
        }

        waitForIdle()

        onNodeWithContentDescription("Kimai").assertExists()
    }

    // Covers branch: useProgeek (dark theme) -> progeek_light
    @Test
    fun `renders progeek light icon for progeek branding in dark theme`() = runComposeUiTest {
        setContent {
            TestTheme(theme = ThemeEnum.DARK, branding = BrandingEnum.PROGEEK) {
                KimaiIcon()
            }
        }

        waitForIdle()

        onNodeWithContentDescription("Kimai").assertExists()
    }

    // Covers branch: theme == LIGHT (kimai branding) -> kimai_icon_orange
    @Test
    fun `renders orange kimai icon for kimai branding in light theme`() = runComposeUiTest {
        setContent {
            TestTheme(theme = ThemeEnum.LIGHT, branding = BrandingEnum.KIMAI) {
                KimaiIcon()
            }
        }

        waitForIdle()

        onNodeWithContentDescription("Kimai").assertExists()
    }

    // Covers branch: else -> kimai_icon_white
    @Test
    fun `renders white kimai icon for kimai branding in dark theme`() = runComposeUiTest {
        setContent {
            TestTheme(theme = ThemeEnum.DARK, branding = BrandingEnum.KIMAI) {
                KimaiIcon()
            }
        }

        waitForIdle()

        onNodeWithContentDescription("Kimai").assertExists()
    }
}
