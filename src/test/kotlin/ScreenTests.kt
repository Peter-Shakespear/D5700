package org.example

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class ScreenTests {

    private lateinit var screen: Screen

    @BeforeEach
    fun setUp() {
        screen = Screen()
    }

    @Test
    fun `test initial screen state`() {
        // Screen should be initialized with spaces (ASCII 32)
        for (y in 0 until 8) {
            for (x in 0 until 8) {
                assertEquals(32, screen.readCharacter(x, y), "Position ($x, $y) should be space initially")
            }
        }
    }

    @Test
    fun `test screen dimensions`() {
        // Screen should be 8x8 (64 characters total)
        // Valid positions should be (0,0) to (7,7)

        // Test valid boundary positions
        assertTrue(screen.readCharacter(0, 0) != -1) // Top-left
        assertTrue(screen.readCharacter(7, 7) != -1) // Bottom-right
        assertTrue(screen.readCharacter(0, 7) != -1) // Bottom-left
        assertTrue(screen.readCharacter(7, 0) != -1) // Top-right

        // Test invalid positions return 0
        assertEquals(0, screen.readCharacter(-1, 0))  // Out of bounds left
        assertEquals(0, screen.readCharacter(8, 0))   // Out of bounds right
        assertEquals(0, screen.readCharacter(0, -1))  // Out of bounds top
        assertEquals(0, screen.readCharacter(0, 8))   // Out of bounds bottom
    }

    @Test
    fun `test basic write and read character`() {
        // Write 'A' (ASCII 65) to position (0, 0)
        screen.writeCharacter(0, 0, 65)
        assertEquals(65, screen.readCharacter(0, 0))

        // Write 'B' (ASCII 66) to position (3, 4)
        screen.writeCharacter(3, 4, 66)
        assertEquals(66, screen.readCharacter(3, 4))

        // Other positions should remain unchanged (space)
        assertEquals(32, screen.readCharacter(1, 0))
        assertEquals(32, screen.readCharacter(0, 1))
    }

    @Test
    fun `test write to all valid positions`() {
        // Write different characters to all positions
        var asciiValue = 65 // Start with 'A'

        for (y in 0 until 8) {
            for (x in 0 until 8) {
                screen.writeCharacter(x, y, asciiValue)
                assertEquals(asciiValue, screen.readCharacter(x, y))
                asciiValue = if (asciiValue >= 90) 65 else asciiValue + 1 // Cycle A-Z
            }
        }
    }

    @Test
    fun `test write to invalid positions`() {
        val invalidPositions = listOf(
            Pair(-1, 0),   // Negative x
            Pair(0, -1),   // Negative y
            Pair(-1, -1),  // Both negative
            Pair(8, 0),    // x too large
            Pair(0, 8),    // y too large
            Pair(8, 8),    // Both too large
            Pair(100, 100) // Way out of bounds
        )

        invalidPositions.forEach { (x, y) ->
            // Writing to invalid position should not crash
            assertDoesNotThrow {
                screen.writeCharacter(x, y, 65)
            }

            // Reading from invalid position should return 0
            assertEquals(0, screen.readCharacter(x, y), "Invalid position ($x, $y) should return 0")
        }

        // Valid positions should still work and not be affected
        screen.writeCharacter(0, 0, 65)
        assertEquals(65, screen.readCharacter(0, 0))
    }

    @Test
    fun `test overwrite character`() {
        // Write initial character
        screen.writeCharacter(2, 3, 65) // 'A'
        assertEquals(65, screen.readCharacter(2, 3))

        // Overwrite with different character
        screen.writeCharacter(2, 3, 66) // 'B'
        assertEquals(66, screen.readCharacter(2, 3))

        // Overwrite with number
        screen.writeCharacter(2, 3, 48) // '0'
        assertEquals(48, screen.readCharacter(2, 3))

        // Overwrite with space
        screen.writeCharacter(2, 3, 32)
        assertEquals(32, screen.readCharacter(2, 3))
    }

    @Test
    fun `test clear screen`() {
        // Fill screen with various characters
        screen.writeCharacter(0, 0, 65) // 'A'
        screen.writeCharacter(1, 1, 66) // 'B'
        screen.writeCharacter(7, 7, 67) // 'C'
        screen.writeCharacter(3, 4, 88) // 'X'

        // Verify characters are written
        assertEquals(65, screen.readCharacter(0, 0))
        assertEquals(66, screen.readCharacter(1, 1))
        assertEquals(67, screen.readCharacter(7, 7))
        assertEquals(88, screen.readCharacter(3, 4))

        // Clear screen
        screen.clear()

        // All positions should now be spaces (ASCII 32)
        for (y in 0 until 8) {
            for (x in 0 until 8) {
                assertEquals(32, screen.readCharacter(x, y), "Position ($x, $y) should be space after clear")
            }
        }
    }

    @Test
    fun `test ASCII character values`() {
        val testCases = listOf(
            Pair(32, ' '),   // Space
            Pair(48, '0'),   // Zero
            Pair(57, '9'),   // Nine
            Pair(65, 'A'),   // A
            Pair(90, 'Z'),   // Z
            Pair(97, 'a'),   // a
            Pair(122, 'z'),  // z
            Pair(33, '!'),   // Exclamation
            Pair(64, '@'),   // At symbol
            Pair(126, '~')   // Tilde
        )

        testCases.forEachIndexed { index, (asciiValue, expectedChar) ->
            val x = index % 8
            val y = index / 8

            screen.writeCharacter(x, y, asciiValue)
            assertEquals(asciiValue, screen.readCharacter(x, y))
        }
    }

    @Test
    fun `test non-printable ASCII values`() {
        // Test various ASCII values including non-printable ones
        val testValues = listOf(0, 1, 7, 8, 10, 13, 31, 127, 128, 255, -1, 1000)

        testValues.forEachIndexed { index, asciiValue ->
            val x = index % 8
            val y = 0

            // Should not crash when writing non-printable values
            assertDoesNotThrow {
                screen.writeCharacter(x, y, asciiValue)
            }

            // Should read back the same value
            assertEquals(asciiValue, screen.readCharacter(x, y))
        }
    }

    @Test
    fun `test display output format`() {
        // Capture System.out to test display output
        val originalOut = System.out
        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        try {
            // Create a pattern on screen
            screen.writeCharacter(0, 0, 65) // 'A'
            screen.writeCharacter(1, 0, 66) // 'B'
            screen.writeCharacter(7, 7, 90) // 'Z'

            screen.display()

            val output = outputStream.toString()

            // Check for border characters
            assertTrue(output.contains("┌────────┐"), "Should contain top border")
            assertTrue(output.contains("└────────┘"), "Should contain bottom border")
            assertTrue(output.contains("│"), "Should contain side borders")

            // Check for content
            assertTrue(output.contains("AB"), "Should contain 'AB' at start of first line")

        } finally {
            System.setOut(originalOut)
        }
    }

    @Test
    fun `test display with non-printable characters`() {
        val originalOut = System.out
        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        try {
            // Write non-printable characters
            screen.writeCharacter(0, 0, 0)    // Null
            screen.writeCharacter(1, 0, 7)    // Bell
            screen.writeCharacter(2, 0, 127)  // DEL
            screen.writeCharacter(3, 0, 200)  // Extended ASCII

            screen.display()

            val output = outputStream.toString()

            // Non-printable characters should be displayed as '?'
            assertTrue(output.contains("????"), "Non-printable characters should appear as '?'")

        } finally {
            System.setOut(originalOut)
        }
    }

    @Test
    fun `test display with mixed characters`() {
        val originalOut = System.out
        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        try {
            // Create a test pattern
            screen.writeCharacter(0, 0, 72)  // 'H'
            screen.writeCharacter(1, 0, 101) // 'e'
            screen.writeCharacter(2, 0, 108) // 'l'
            screen.writeCharacter(3, 0, 108) // 'l'
            screen.writeCharacter(4, 0, 111) // 'o'

            screen.display()

            val output = outputStream.toString()

            // Should contain "Hello" in the output
            assertTrue(output.contains("Hello"), "Should display 'Hello' text")

        } finally {
            System.setOut(originalOut)
        }
    }

    @Test
    fun `test coordinate system`() {
        // Test that coordinate system works as expected (0,0 top-left)

        // Write to corners
        screen.writeCharacter(0, 0, 49) // '1' - top-left
        screen.writeCharacter(7, 0, 50) // '2' - top-right
        screen.writeCharacter(0, 7, 51) // '3' - bottom-left
        screen.writeCharacter(7, 7, 52) // '4' - bottom-right

        // Verify positions
        assertEquals(49, screen.readCharacter(0, 0)) // Top-left
        assertEquals(50, screen.readCharacter(7, 0)) // Top-right
        assertEquals(51, screen.readCharacter(0, 7)) // Bottom-left
        assertEquals(52, screen.readCharacter(7, 7)) // Bottom-right

        // Verify center positions are still spaces
        assertEquals(32, screen.readCharacter(3, 3))
        assertEquals(32, screen.readCharacter(4, 4))
    }

    @Test
    fun `test frame buffer integrity`() {
        // Test that writing to one position doesn't affect others

        // Fill entire screen with known values
        for (y in 0 until 8) {
            for (x in 0 until 8) {
                val value = (y * 8 + x) + 65 // Different ASCII value for each position
                screen.writeCharacter(x, y, value)
            }
        }

        // Verify all positions have correct values
        for (y in 0 until 8) {
            for (x in 0 until 8) {
                val expectedValue = (y * 8 + x) + 65
                assertEquals(expectedValue, screen.readCharacter(x, y),
                    "Position ($x, $y) should have value $expectedValue")
            }
        }

        // Change one position
        screen.writeCharacter(3, 4, 999)
        assertEquals(999, screen.readCharacter(3, 4))

        // Verify all other positions unchanged
        for (y in 0 until 8) {
            for (x in 0 until 8) {
                if (x != 3 || y != 4) {
                    val expectedValue = (y * 8 + x) + 65
                    assertEquals(expectedValue, screen.readCharacter(x, y),
                        "Position ($x, $y) should still have value $expectedValue")
                }
            }
        }
    }

    @Test
    fun `test edge case positions`() {
        val edgeCases = listOf(
            // Valid edge positions
            Triple(0, 0, true),
            Triple(7, 0, true),
            Triple(0, 7, true),
            Triple(7, 7, true),
            Triple(3, 0, true),
            Triple(3, 7, true),
            Triple(0, 3, true),
            Triple(7, 3, true),

            // Invalid edge positions
            Triple(-1, 0, false),
            Triple(8, 0, false),
            Triple(0, -1, false),
            Triple(0, 8, false),
            Triple(-1, -1, false),
            Triple(8, 8, false)
        )

        edgeCases.forEach { (x, y, shouldBeValid) ->
            if (shouldBeValid) {
                screen.writeCharacter(x, y, 88) // 'X'
                assertEquals(88, screen.readCharacter(x, y), "Valid position ($x, $y) should work")
            } else {
                // Writing should not crash
                assertDoesNotThrow { screen.writeCharacter(x, y, 88) }
                // Reading should return 0
                assertEquals(0, screen.readCharacter(x, y), "Invalid position ($x, $y) should return 0")
            }
        }
    }

    @Test
    fun `test clear preserves screen functionality`() {
        // Write some characters
        screen.writeCharacter(2, 2, 65)
        screen.writeCharacter(5, 6, 90)

        // Clear screen
        screen.clear()

        // Screen should still be functional
        screen.writeCharacter(1, 1, 77) // 'M'
        assertEquals(77, screen.readCharacter(1, 1))

        // All other positions should be spaces
        for (y in 0 until 8) {
            for (x in 0 until 8) {
                if (x != 1 || y != 1) {
                    assertEquals(32, screen.readCharacter(x, y))
                }
            }
        }
    }

    @Test
    fun `test multiple clear operations`() {
        // Write characters
        screen.writeCharacter(0, 0, 65)
        screen.writeCharacter(7, 7, 90)

        // Clear multiple times
        screen.clear()
        screen.clear()
        screen.clear()

        // Should still be all spaces
        for (y in 0 until 8) {
            for (x in 0 until 8) {
                assertEquals(32, screen.readCharacter(x, y))
            }
        }

        // Should still be functional
        screen.writeCharacter(3, 3, 88)
        assertEquals(88, screen.readCharacter(3, 3))
    }

    @Test
    fun `test screen isolation between instances`() {
        val screen2 = Screen()

        // Write to first screen
        screen.writeCharacter(0, 0, 65) // 'A'

        // Second screen should still be spaces
        assertEquals(32, screen2.readCharacter(0, 0))

        // Write to second screen
        screen2.writeCharacter(0, 0, 66) // 'B'

        // First screen should be unchanged
        assertEquals(65, screen.readCharacter(0, 0))
        assertEquals(66, screen2.readCharacter(0, 0))
    }

    @Test
    fun `test large ASCII values`() {
        // Test with various large ASCII values
        val largeValues = listOf(128, 255, 1000, 65535, Integer.MAX_VALUE)

        largeValues.forEachIndexed { index, value ->
            val x = index % 8
            val y = 0

            screen.writeCharacter(x, y, value)
            assertEquals(value, screen.readCharacter(x, y))
        }
    }

    @Test
    fun `test negative ASCII values`() {
        // Test with negative ASCII values
        val negativeValues = listOf(-1, -10, -100, -1000, Integer.MIN_VALUE)

        negativeValues.forEachIndexed { index, value ->
            val x = index % 8
            val y = 0

            screen.writeCharacter(x, y, value)
            assertEquals(value, screen.readCharacter(x, y))
        }
    }

    @Test
    fun `test screen state after operations`() {
        // Perform various operations and verify state consistency

        // Initial state - all spaces
        assertEquals(32, screen.readCharacter(0, 0))

        // Write and verify
        screen.writeCharacter(0, 0, 65)
        assertEquals(65, screen.readCharacter(0, 0))

        // Overwrite and verify
        screen.writeCharacter(0, 0, 66)
        assertEquals(66, screen.readCharacter(0, 0))

        // Clear and verify
        screen.clear()
        assertEquals(32, screen.readCharacter(0, 0))

        // Write after clear and verify
        screen.writeCharacter(0, 0, 67)
        assertEquals(67, screen.readCharacter(0, 0))
    }

    @Test
    fun `test concurrent read and write operations`() {
        // Test multiple rapid read/write operations
        repeat(100) { i ->
            val x = i % 8
            val y = (i / 8) % 8
            val value = (i % 95) + 32 // Printable ASCII range

            screen.writeCharacter(x, y, value)
            assertEquals(value, screen.readCharacter(x, y))
        }
    }

    @Test
    fun `test printable ASCII range in display`() {
        val originalOut = System.out
        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        try {
            // Write characters from printable ASCII range (32-126)
            var asciiValue = 32
            for (y in 0 until 8) {
                for (x in 0 until 8) {
                    screen.writeCharacter(x, y, asciiValue)
                    asciiValue = if (asciiValue >= 126) 32 else asciiValue + 1
                }
            }

            screen.display()

            val output = outputStream.toString()

            // Should not contain '?' characters (all should be printable)
            val contentLines = output.split("\n").filter { it.startsWith("│") && it.endsWith("│") }
            contentLines.forEach { line ->
                assertFalse(line.contains("?"), "Should not contain '?' for printable characters")
            }

        } finally {
            System.setOut(originalOut)
        }
    }
}