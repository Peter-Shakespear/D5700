package org.example

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

class DrawTests {

    private lateinit var registers: Registers
    private lateinit var rom: ROM
    private lateinit var ram: RAM
    private lateinit var screen: Screen

    @BeforeEach
    fun setUp() {
        registers = Registers()
        rom = ROM()
        ram = RAM()
        screen = Screen()
    }

    @Test
    fun `test basic draw operation`() {
        // Set up: r0 = 65 (ASCII 'A')
        registers.setValue(0, 65)

        // Execute: Draw r0 at position (0,0) - instruction F000
        val drawInstruction = Draw("F000", registers, rom, ram, screen)
        drawInstruction.execute()

        // Verify: character was written to screen
        assertEquals(65, screen.readCharacter(0, 0))
    }

    @Test
    fun `test draw at different positions`() {
        // Test drawing at various valid positions
        registers.setValue(1, 72) // ASCII 'H'

        // Draw at position (3, 2) - instruction F132
        val drawInstruction = Draw("F132", registers, rom, ram, screen)
        drawInstruction.execute()

        // Fix: Read from (column=2, row=3) not (3, 2)
        assertEquals(72, screen.readCharacter(2, 3))
    }

    @Test
    fun `test draw with different ASCII values`() {
        // Test various ASCII characters
        val testCases = listOf(
            Triple(65, "F000", Pair(0, 0)), // 'A'
            Triple(48, "F011", Pair(1, 1)), // '0'
            Triple(32, "F022", Pair(2, 2)), // Space
            Triple(127, "F033", Pair(3, 3)) // DEL (max valid ASCII)
        )

        testCases.forEachIndexed { index, (ascii, instruction, position) ->
            registers.setValue(0, ascii)
            val drawInstruction = Draw(instruction, registers, rom, ram, screen)
            drawInstruction.execute()

            assertEquals(ascii, screen.readCharacter(position.first, position.second))
        }
    }

    @Test
    fun `test byte organization parsing`() {
        // Test instruction F567: rX=5, row=6, column=7
        registers.setValue(5, 88) // ASCII 'X'

        val drawInstruction = Draw("F567", registers, rom, ram, screen)
        drawInstruction.execute()

        // Verify character was drawn at correct position
        assertEquals(88, screen.readCharacter(7, 6)) // Note: column, row order
    }

    @Test
    fun `test ASCII value boundary cases`() {
        // Test ASCII value 0 (null character)
        registers.setValue(0, 0)
        val drawInstruction1 = Draw("F000", registers, rom, ram, screen)
        drawInstruction1.execute()
        assertEquals(0, screen.readCharacter(0, 0))

        // Test ASCII value 127 (maximum valid)
        registers.setValue(1, 127)
        val drawInstruction2 = Draw("F111", registers, rom, ram, screen)
        drawInstruction2.execute()
        assertEquals(127, screen.readCharacter(1, 1))
    }

    @Test
    fun `test ASCII value too large throws exception`() {
        // Set up: r0 = 128 (exceeds 127 limit)
        registers.setValue(0, 128)

        val drawInstruction = Draw("F000", registers, rom, ram, screen)

        val exception = assertThrows<RuntimeException> {
            drawInstruction.execute()
        }

        assertTrue(exception.message!!.contains("DRAW error: ASCII value 128"))
        assertTrue(exception.message!!.contains("greater than 7F"))
    }

    @Test
    fun `test invalid row position throws exception`() {
        registers.setValue(0, 65)

        // Test row > 7 (instruction F080 = row 8, column 0)
        val drawInstruction1 = Draw("F080", registers, rom, ram, screen)
        val exception1 = assertThrows<RuntimeException> {
            drawInstruction1.execute()
        }
        assertTrue(exception1.message!!.contains("Screen position out of bounds"))
        assertTrue(exception1.message!!.contains("row=8"))
    }

    @Test
    fun `test invalid column position throws exception`() {
        registers.setValue(0, 65)

        // Test column > 7 (instruction F008 = row 0, column 8)
        val drawInstruction = Draw("F008", registers, rom, ram, screen)
        val exception = assertThrows<RuntimeException> {
            drawInstruction.execute()
        }
        assertTrue(exception.message!!.contains("Screen position out of bounds"))
        assertTrue(exception.message!!.contains("column=8"))
    }

    @Test
    fun `test all valid screen positions`() {
        registers.setValue(0, 42) // ASCII '*'

        // Test all 64 positions (8x8 grid)
        for (row in 0..7) {
            for (col in 0..7) {
                val instruction = String.format("F0%X%X", row, col)
                val drawInstruction = Draw(instruction, registers, rom, ram, screen)
                drawInstruction.execute()

                assertEquals(42, screen.readCharacter(col, row))
            }
        }
    }

    @Test
    fun `test program counter increment`() {
        val initialPC = rom.getProgramCounter()
        registers.setValue(0, 65)

        val drawInstruction = Draw("F000", registers, rom, ram, screen)
        drawInstruction.execute()

        assertEquals(initialPC + 1, rom.getProgramCounter())
    }

    @Test
    fun `test complete instruction execution flow`() {
        val initialPC = rom.getProgramCounter()
        registers.setValue(3, 33) // ASCII '!'

        // Draw r3 at position (4, 5) - instruction F354
        val drawInstruction = Draw("F354", registers, rom, ram, screen)
        drawInstruction.execute()

        // Verify all aspects:
        assertEquals(33, screen.readCharacter(4, 5))     // Character drawn
        assertEquals(33, registers.getValue(3))          // Register unchanged
        assertEquals(initialPC + 1, rom.getProgramCounter()) // PC incremented
    }

    @Test
    fun `test overwriting screen position`() {
        registers.setValue(0, 65) // ASCII 'A'
        registers.setValue(1, 66) // ASCII 'B'

        // Draw 'A' at position (0,0)
        val drawInstruction1 = Draw("F000", registers, rom, ram, screen)
        drawInstruction1.execute()
        assertEquals(65, screen.readCharacter(0, 0))

        // Overwrite with 'B' at same position
        val drawInstruction2 = Draw("F100", registers, rom, ram, screen)
        drawInstruction2.execute()
        assertEquals(66, screen.readCharacter(0, 0)) // Should be overwritten
    }

    @Test
    fun `test drawing with empty register`() {
        // Register defaults to 0, draw at position (column=3, row=2)
        val drawInstruction = Draw("F023", registers, rom, ram, screen)
        drawInstruction.execute()

        // Fix: Read from (column=3, row=2) not (2, 3)
        assertEquals(0, screen.readCharacter(3, 2))
    }

    @Test
    fun `test instruction parsing edge cases`() {
        // Test minimum values (F000)
        registers.setValue(0, 32) // ASCII space
        val drawInstruction1 = Draw("F000", registers, rom, ram, screen)
        drawInstruction1.execute()
        assertEquals(32, screen.readCharacter(0, 0))

        // Test maximum valid values (F777)
        registers.setValue(7, 126) // ASCII '~'
        val drawInstruction2 = Draw("F777", registers, rom, ram, screen)
        drawInstruction2.execute()
        assertEquals(126, screen.readCharacter(7, 7))
    }

    @Test
    fun `test ASCII control characters`() {
        val controlChars = listOf(
            1,   // SOH
            9,   // TAB
            10,  // LF
            13,  // CR
            27   // ESC
        )

        controlChars.forEachIndexed { index, ascii ->
            registers.setValue(0, ascii)
            val instruction = String.format("F0%d%d", index % 8, index % 8)
            val drawInstruction = Draw(instruction, registers, rom, ram, screen)

            assertDoesNotThrow {
                drawInstruction.execute()
            }

            assertEquals(ascii, screen.readCharacter(index % 8, index % 8))
        }
    }

    @Test
    fun `test negative ASCII values`() {
        // Test with negative values (if supported)
        registers.setValue(0, -1)

        val drawInstruction = Draw("F000", registers, rom, ram, screen)

        // Should not throw exception for negative values (they're < 127)
        assertDoesNotThrow {
            drawInstruction.execute()
        }
    }
}