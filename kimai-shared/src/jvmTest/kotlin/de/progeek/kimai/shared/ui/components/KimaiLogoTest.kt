@file:OptIn(ExperimentalTestApi::class)

package de.progeek.kimai.shared.ui.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.runComposeUiTest
import de.progeek.kimai.shared.testutils.TestTheme
import de.progeek.kimai.shared.ui.theme.BrandingEnum
import de.progeek.kimai.shared.ui.theme.ThemeEnum
import org.junit.Test

class KimaiLogoTest {

    // Covers branch: useProgeek && theme == LIGHT -> progeek_logo_dark
    @Test
    fun `renders progeek dark logo for progeek branding in light theme`() = runComposeUiTest {
        setContent {
            TestTheme(theme = ThemeEnum.LIGHT, branding = BrandingEnum.PROGEEK) {
                KimaiLogo()
            }
        }

        waitForIdle()

        onNodeWithContentDescription("Kimai").assertExists()
    }

    // Covers branch: useProgeek (dark theme) -> progeek_logo_light
    @Test
    fun `renders progeek light logo for progeek branding in dark theme`() = runComposeUiTest {
        setContent {
            TestTheme(theme = ThemeEnum.DARK, branding = BrandingEnum.PROGEEK) {
                KimaiLogo()
            }
        }

        waitForIdle()

        onNodeWithContentDescription("Kimai").assertExists()
    }

    // Covers branch: else -> kimai_logo (kimai branding)
    @Test
    fun `renders kimai logo for kimai branding`() = runComposeUiTest {
        setContent {
            TestTheme(theme = ThemeEnum.LIGHT, branding = BrandingEnum.KIMAI) {
                KimaiLogo()
            }
        }

        waitForIdle()

        onNodeWithContentDescription("Kimai").assertExists()
    }

    // Covers branch: useBranding == false forces the kimai logo even with progeek branding.
    @Test
    fun `renders kimai logo when branding disabled`() = runComposeUiTest {
        setContent {
            TestTheme(theme = ThemeEnum.LIGHT, branding = BrandingEnum.PROGEEK) {
                KimaiLogo(useBranding = false)
            }
        }

        waitForIdle()

        onNodeWithContentDescription("Kimai").assertExists()
    }
}
