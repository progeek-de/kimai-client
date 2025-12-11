package de.progeek.kimai.shared.ui.theme

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Test suite for BrandingEnum.
 *
 * Tests the following:
 * 1. Enum has exactly two values
 * 2. valueOf works correctly for KIMAI
 * 3. valueOf works correctly for PROGEEK
 * 4. toString returns correct string representation
 * 5. Enum values are distinct
 */
class BrandingEnumTest {

    @Test
    fun `BrandingEnum has exactly two values`() {
        val values = BrandingEnum.entries
        assertEquals(2, values.size, "BrandingEnum should have exactly 2 values")
    }

    @Test
    fun `BrandingEnum contains KIMAI`() {
        val values = BrandingEnum.entries
        assertTrue(values.contains(BrandingEnum.KIMAI), "BrandingEnum should contain KIMAI")
    }

    @Test
    fun `BrandingEnum contains PROGEEK`() {
        val values = BrandingEnum.entries
        assertTrue(values.contains(BrandingEnum.PROGEEK), "BrandingEnum should contain PROGEEK")
    }

    @Test
    fun `valueOf KIMAI returns correct enum`() {
        val result = BrandingEnum.valueOf("KIMAI")
        assertEquals(BrandingEnum.KIMAI, result, "valueOf('KIMAI') should return BrandingEnum.KIMAI")
    }

    @Test
    fun `valueOf PROGEEK returns correct enum`() {
        val result = BrandingEnum.valueOf("PROGEEK")
        assertEquals(BrandingEnum.PROGEEK, result, "valueOf('PROGEEK') should return BrandingEnum.PROGEEK")
    }

    @Test
    fun `KIMAI toString returns KIMAI`() {
        assertEquals("KIMAI", BrandingEnum.KIMAI.toString(), "KIMAI.toString() should return 'KIMAI'")
    }

    @Test
    fun `PROGEEK toString returns PROGEEK`() {
        assertEquals("PROGEEK", BrandingEnum.PROGEEK.toString(), "PROGEEK.toString() should return 'PROGEEK'")
    }

    @Test
    fun `KIMAI and PROGEEK are distinct`() {
        assertNotEquals(BrandingEnum.KIMAI, BrandingEnum.PROGEEK, "KIMAI and PROGEEK should be distinct")
    }

    @Test
    fun `BrandingEnum ordinal values are correct`() {
        assertEquals(0, BrandingEnum.KIMAI.ordinal, "KIMAI should have ordinal 0")
        assertEquals(1, BrandingEnum.PROGEEK.ordinal, "PROGEEK should have ordinal 1")
    }

    @Test
    fun `BrandingEnum name property matches string representation`() {
        assertEquals("KIMAI", BrandingEnum.KIMAI.name, "KIMAI.name should be 'KIMAI'")
        assertEquals("PROGEEK", BrandingEnum.PROGEEK.name, "PROGEEK.name should be 'PROGEEK'")
    }

    @Test
    fun `BrandingEnum values can be iterated`() {
        val expected = listOf(BrandingEnum.KIMAI, BrandingEnum.PROGEEK)
        assertEquals(expected, BrandingEnum.entries.toList(), "Entries should match expected order")
    }
}
