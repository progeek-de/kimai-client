@file:OptIn(ExperimentalTestApi::class)

package de.progeek.kimai.shared.ui.components.loading

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import de.progeek.kimai.shared.testutils.TestTheme
import org.junit.Test

class LoadingContentTest {

    @Test
    fun `loading content renders without error`() = runComposeUiTest {
        setContent {
            TestTheme {
                Box(modifier = Modifier.size(200.dp)) {
                    LoadingContent()
                }
            }
        }

        waitForIdle()

        // The progress indicator hosts a root node that must exist after composition.
        onRoot().assertExists()
    }
}
