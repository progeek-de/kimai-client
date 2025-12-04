package de.progeek.kimai.shared.utils

import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Test suite for utility extension functions.
 *
 * Tests cover:
 * - Boolean extensions: ok(), not(), then()
 * - Nullable extensions: isNull(), notNull()
 * - Collection extensions: withEmpty()
 * - DateTime extensions: toLocalDatetime(), removeSeconds()
 * - String extensions: addCharAtIndex()
 */
class ExtensionsTest {

    // ============================================================
    // Boolean.ok() Tests
    // ============================================================

    @Test
    fun `ok executes action when Boolean is true`() {
        var executed = false

        val result = true.ok { executed = true }

        assertTrue(executed, "Action should be executed when Boolean is true")
        assertTrue(result, "ok() should return the original Boolean value")
    }

    @Test
    fun `ok does not execute action when Boolean is false`() {
        var executed = false

        val result = false.ok { executed = true }

        assertFalse(executed, "Action should not be executed when Boolean is false")
        assertFalse(result, "ok() should return the original Boolean value")
    }

    // ============================================================
    // Boolean.not() Tests
    // ============================================================

    @Test
    fun `not executes action when Boolean is false`() {
        var executed = false

        val result = false.not { executed = true }

        assertTrue(executed, "Action should be executed when Boolean is false")
        assertFalse(result, "not() should return the original Boolean value")
    }

    @Test
    fun `not does not execute action when Boolean is true`() {
        var executed = false

        val result = true.not { executed = true }

        assertFalse(executed, "Action should not be executed when Boolean is true")
        assertTrue(result, "not() should return the original Boolean value")
    }

    // ============================================================
    // Boolean.then() Tests
    // ============================================================

    @Test
    fun `then returns value when Boolean is true`() {
        val result = true.then("value")

        assertEquals("value", result, "then() should return the value when Boolean is true")
    }

    @Test
    fun `then returns null when Boolean is false`() {
        val result = false.then("value")

        assertNull(result, "then() should return null when Boolean is false")
    }

    @Test
    fun `then works with different types`() {
        assertEquals(42, true.then(42), "then() should work with Int")
        assertEquals(3.14, true.then(3.14), "then() should work with Double")
        assertEquals(listOf(1, 2, 3), true.then(listOf(1, 2, 3)), "then() should work with List")

        assertNull(false.then(42), "then() should return null for any type when false")
    }

    // ============================================================
    // T?.isNull() Tests
    // ============================================================

    @Test
    fun `isNull executes block when value is null`() {
        var executed = false
        val nullValue: String? = null

        val result = nullValue.isNull { executed = true }

        assertTrue(executed, "Block should be executed when value is null")
        assertNull(result, "isNull() should return the original null value")
    }

    @Test
    fun `isNull does not execute block when value is not null`() {
        var executed = false
        val value: String? = "hello"

        val result = value.isNull { executed = true }

        assertFalse(executed, "Block should not be executed when value is not null")
        assertEquals("hello", result, "isNull() should return the original value")
    }

    // ============================================================
    // T?.notNull() Tests
    // ============================================================

    @Test
    fun `notNull executes block with value when not null`() {
        var capturedValue: String? = null
        val value: String? = "hello"

        val result = value.notNull { capturedValue = it }

        assertEquals("hello", capturedValue, "Block should receive the non-null value")
        assertEquals("hello", result, "notNull() should return the original value")
    }

    @Test
    fun `notNull does not execute block when value is null`() {
        var executed = false
        val nullValue: String? = null

        val result = nullValue.notNull { executed = true }

        assertFalse(executed, "Block should not be executed when value is null")
        assertNull(result, "notNull() should return null")
    }

    // ============================================================
    // Collection.withEmpty() Tests
    // ============================================================

    @Test
    fun `withEmpty returns fallback when collection is empty`() {
        val emptyList = emptyList<Int>()
        val fallback = listOf(1, 2, 3)

        val result = emptyList.withEmpty { fallback }

        assertEquals(fallback, result, "withEmpty() should return fallback for empty collection")
    }

    @Test
    fun `withEmpty returns original collection when not empty`() {
        val original = listOf(1, 2, 3)
        val fallback = listOf(4, 5, 6)

        val result = original.withEmpty { fallback }

        assertEquals(original, result, "withEmpty() should return original collection when not empty")
    }

    @Test
    fun `withEmpty works with different collection types`() {
        val emptySet = emptySet<String>()
        val fallbackSet = setOf("a", "b")

        val result = emptySet.withEmpty { fallbackSet }

        assertEquals(fallbackSet, result, "withEmpty() should work with Set")
    }

    // ============================================================
    // Long.toLocalDatetime() Tests
    // ============================================================

    @Test
    fun `toLocalDatetime converts epoch milliseconds to LocalDateTime`() {
        // 2025-01-15T12:00:00Z = 1736942400000 milliseconds since epoch (UTC)
        val epochMillis = 1736942400000L

        val result = epochMillis.toLocalDatetime()

        assertNotNull(result, "toLocalDatetime() should return a non-null LocalDateTime")
        // Note: The exact values depend on the system's timezone, so we just verify it's a valid datetime
        assertTrue(result.year >= 2025, "Year should be 2025 or later (depending on timezone)")
    }

    @Test
    fun `toLocalDatetime handles zero epoch`() {
        val epochMillis = 0L

        val result = epochMillis.toLocalDatetime()

        assertNotNull(result, "toLocalDatetime() should handle epoch zero")
        // Epoch 0 is January 1, 1970, 00:00:00 UTC
        assertEquals(1970, result.year, "Year should be 1970 for epoch 0")
    }

    // ============================================================
    // LocalDateTime.removeSeconds() Tests
    // ============================================================

    @Test
    fun `removeSeconds creates new LocalDateTime without seconds`() {
        val original = LocalDateTime(2025, 1, 15, 14, 30, 45)

        val result = original.removeSeconds()

        assertEquals(2025, result.year, "Year should be preserved")
        assertEquals(1, result.monthNumber, "Month should be preserved")
        assertEquals(15, result.dayOfMonth, "Day should be preserved")
        assertEquals(14, result.hour, "Hour should be preserved")
        assertEquals(30, result.minute, "Minute should be preserved")
        assertEquals(0, result.second, "Seconds should be zero")
    }

    @Test
    fun `removeSeconds handles midnight`() {
        val original = LocalDateTime(2025, 1, 1, 0, 0, 59)

        val result = original.removeSeconds()

        assertEquals(0, result.hour, "Hour should remain 0")
        assertEquals(0, result.minute, "Minute should remain 0")
        assertEquals(0, result.second, "Seconds should be zero")
    }

    @Test
    fun `removeSeconds handles end of day`() {
        val original = LocalDateTime(2025, 12, 31, 23, 59, 59)

        val result = original.removeSeconds()

        assertEquals(23, result.hour, "Hour should remain 23")
        assertEquals(59, result.minute, "Minute should remain 59")
        assertEquals(0, result.second, "Seconds should be zero")
    }

    // ============================================================
    // String.addCharAtIndex() Tests
    // ============================================================

    @Test
    fun `addCharAtIndex inserts character at specified index`() {
        val result = "HELLO".addCharAtIndex('-', 2)

        assertEquals("HE-LLO", result, "Character should be inserted at index 2")
    }

    @Test
    fun `addCharAtIndex inserts at beginning`() {
        val result = "HELLO".addCharAtIndex('!', 0)

        assertEquals("!HELLO", result, "Character should be inserted at the beginning")
    }

    @Test
    fun `addCharAtIndex inserts at end`() {
        val result = "HELLO".addCharAtIndex('!', 5)

        assertEquals("HELLO!", result, "Character should be inserted at the end")
    }

    @Test
    fun `addCharAtIndex works with empty string`() {
        val result = "".addCharAtIndex('X', 0)

        assertEquals("X", result, "Character should be inserted into empty string")
    }

    // ============================================================
    // Chaining Tests
    // ============================================================

    @Test
    fun `Boolean extensions can be chained`() {
        var okExecuted = false
        var notExecuted = false

        val result = true
            .ok { okExecuted = true }
            .not { notExecuted = true }

        assertTrue(okExecuted, "ok() should execute for true")
        assertFalse(notExecuted, "not() should not execute for true")
        assertTrue(result, "Chained result should be the original value")
    }

    @Test
    fun `Nullable extensions can be chained`() {
        var isNullExecuted = false
        var notNullExecuted = false
        val value: String? = "test"

        val result = value
            .isNull { isNullExecuted = true }
            .notNull { notNullExecuted = true }

        assertFalse(isNullExecuted, "isNull() should not execute for non-null")
        assertTrue(notNullExecuted, "notNull() should execute for non-null")
        assertEquals("test", result, "Chained result should be the original value")
    }
}
