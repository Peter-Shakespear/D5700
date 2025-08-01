
package org.example

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class ConvertByteToAsciiTests {

    private lateinit var registers: Registers
    private lateinit var rom: ROM
    private lateinit var ram: RAM

    @BeforeEach
    fun setUp() {
        registers = Registers()
        rom = ROM()
        ram = RAM()
    }

    @Test
    fun `test convert digit 0 to ASCII`() {
        // Set up: r0 = 0
        registers.setValue(0, 0)

        // Execute: Convert r0 to ASCII and store in r1 (instruction E010)
        val convertInstruction = ConvertByteToAscii("E010", registers, rom, ram)
        convertInstruction.execute()

        // Verify: r1 should contain ASCII '0' (48)
        assertEquals(48, registers.getValue(1)) // '0'.code = 48
        assertEquals(0, registers.getValue(0))  // Source unchanged
    }

    @Test
    fun `test convert digits 0-9 to ASCII`() {
        val testCases = listOf(
            Pair(0, 48),  // '0'
            Pair(1, 49),  // '1'
            Pair(2, 50),  // '2'
            Pair(3, 51),  // '3'
            Pair(4, 52),  // '4'
            Pair(5, 53),  // '5'
            Pair(6, 54),  // '6'
            Pair(7, 55),  // '7'
            Pair(8, 56),  // '8'
            Pair(9, 57)   // '9'
        )

        testCases.forEach { (input, expectedAscii) ->
            registers.setValue(0, input)

            val convertInstruction = ConvertByteToAscii("E010", registers, rom, ram)
            convertInstruction.execute()

            assertEquals(expectedAscii, registers.getValue(1))
            assertEquals(input, registers.getValue(0)) // Source unchanged

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test convert hex digits A-F to ASCII`() {
        val testCases = listOf(
            Pair(10, 65),  // 'A'
            Pair(11, 66),  // 'B'
            Pair(12, 67),  // 'C'
            Pair(13, 68),  // 'D'
            Pair(14, 69),  // 'E'
            Pair(15, 70)   // 'F'
        )

        testCases.forEach { (input, expectedAscii) ->
            registers.setValue(0, input)

            val convertInstruction = ConvertByteToAscii("E010", registers, rom, ram)
            convertInstruction.execute()

            assertEquals(expectedAscii, registers.getValue(1))
            assertEquals(input, registers.getValue(0)) // Source unchanged

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test convert with different register combinations`() {
        val testCases = listOf(
            Triple("E012", 0, 1),  // Convert r0 to r1
            Triple("E023", 0, 2),  // Convert r0 to r2
            Triple("E134", 1, 3),  // Convert r1 to r3
            Triple("E245", 2, 4),  // Convert r2 to r4
            Triple("E567", 5, 6)   // Convert r5 to r6
        )

        testCases.forEach { (instruction, sourceReg, targetReg) ->
            registers.setValue(sourceReg, 10) // hex A

            val convertInstruction = ConvertByteToAscii(instruction, registers, rom, ram)
            convertInstruction.execute()

            assertEquals(65, registers.getValue(targetReg)) // 'A' = 65
            assertEquals(10, registers.getValue(sourceReg)) // Source unchanged

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test byte organization parsing`() {
        // Test instruction EXYZ: rX=source, rY=target, rZ ignored
        registers.setValue(3, 12) // hex C

        // Test instruction E347: should convert r3 to r4 (X=3, Y=4, Z ignored)
        val convertInstruction = ConvertByteToAscii("E347", registers, rom, ram)
        convertInstruction.execute()

        assertEquals(67, registers.getValue(4)) // 'C' = 67
        assertEquals(12, registers.getValue(3)) // Source unchanged
        assertEquals(0, registers.getValue(7))  // Z register unaffected
    }

    @Test
    fun `test value greater than 15 throws exception`() {
        // Set up: r0 = 16 (greater than F)
        registers.setValue(0, 16)

        val convertInstruction = ConvertByteToAscii("E010", registers, rom, ram)

        val exception = assertThrows<RuntimeException> {
            convertInstruction.execute()
        }

        assertTrue(exception.message!!.contains("CONVERT_BYTE_TO_ASCII error: Value 16 in r0 is greater than F (15)"))
    }

    @Test
    fun `test various values greater than 15 throw exceptions`() {
        val invalidValues = listOf(16, 20, 50, 100, 255, 1000)

        invalidValues.forEach { value ->
            registers.setValue(0, value)

            val convertInstruction = ConvertByteToAscii("E010", registers, rom, ram)

            val exception = assertThrows<RuntimeException> {
                convertInstruction.execute()
            }

            assertTrue(exception.message!!.contains("CONVERT_BYTE_TO_ASCII error: Value $value in r0 is greater than F (15)"))

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test conversion doesn't affect other registers`() {
        // Set up: Initialize all registers
        registers.setValue(0, 5)   // Source
        registers.setValue(1, 20)  // Will be overwritten (target)
        registers.setValue(2, 30)
        registers.setValue(3, 40)
        registers.setValue(4, 50)
        registers.setValue(5, 60)
        registers.setValue(6, 70)
        registers.setValue(7, 80)
        registers.setA(100)
        registers.setM(1)
        registers.setT(99)

        // Execute: Convert r0 to r1
        val convertInstruction = ConvertByteToAscii("E010", registers, rom, ram)
        convertInstruction.execute()

        // Verify: Only r1 changed, all others unchanged
        assertEquals(5, registers.getValue(0))   // Source unchanged
        assertEquals(53, registers.getValue(1))  // Target changed to '5' (53)
        assertEquals(30, registers.getValue(2))  // Unchanged
        assertEquals(40, registers.getValue(3))  // Unchanged
        assertEquals(50, registers.getValue(4))  // Unchanged
        assertEquals(60, registers.getValue(5))  // Unchanged
        assertEquals(70, registers.getValue(6))  // Unchanged
        assertEquals(80, registers.getValue(7))  // Unchanged
        assertEquals(100, registers.A)           // Unchanged
        assertEquals(1, registers.M)             // Unchanged
        assertEquals(99, registers.T)            // Unchanged
    }

    @Test
    fun `test program counter increment`() {
        val initialPC = rom.getProgramCounter()

        registers.setValue(0, 7)
        val convertInstruction = ConvertByteToAscii("E010", registers, rom, ram)
        convertInstruction.execute()

        // Verify PC was incremented
        assertEquals(initialPC + 1, rom.getProgramCounter())
    }

    @Test
    fun `test complete instruction execution flow`() {
        val initialPC = rom.getProgramCounter()

        // Set up: r2 = 14 (hex E)
        registers.setValue(2, 14)

        // Execute: Convert r2 to r5 (instruction E250)
        val convertInstruction = ConvertByteToAscii("E250", registers, rom, ram)
        convertInstruction.execute()

        // Verify all aspects:
        assertEquals(69, registers.getValue(5))                // 'E' = 69
        assertEquals(14, registers.getValue(2))                // Source unchanged
        assertEquals(initialPC + 1, rom.getProgramCounter())   // PC incremented
    }

    @Test
    fun `test all valid hex values`() {
        // Test all valid hex values (0-15)
        val expectedAsciiValues = listOf(
            48, 49, 50, 51, 52, 53, 54, 55, 56, 57,  // '0'-'9'
            65, 66, 67, 68, 69, 70                    // 'A'-'F'
        )

        for (value in 0..15) {
            registers.setValue(0, value)

            val convertInstruction = ConvertByteToAscii("E010", registers, rom, ram)
            convertInstruction.execute()

            assertEquals(expectedAsciiValues[value], registers.getValue(1))

            // Reset for next iteration
            setUp()
        }
    }

    @Test
    fun `test same register as source and target`() {
        // Set up: r3 = 9
        registers.setValue(3, 9)

        // Execute: Convert r3 to r3 (overwrite source)
        val convertInstruction = ConvertByteToAscii("E330", registers, rom, ram)
        convertInstruction.execute()

        // Verify: r3 should now contain ASCII '9' (57)
        assertEquals(57, registers.getValue(3))
    }

    @Test
    fun `test boundary values`() {
        val boundaryTests = listOf(
            Triple(0, "minimum valid value", 48),   // '0'
            Triple(15, "maximum valid value", 70),  // 'F'
            Triple(9, "last decimal digit", 57),    // '9'
            Triple(10, "first hex letter", 65)      // 'A'
        )

        boundaryTests.forEach { (value, description, expectedAscii) ->
            registers.setValue(0, value)

            val convertInstruction = ConvertByteToAscii("E010", registers, rom, ram)
            convertInstruction.execute()

            assertEquals(expectedAscii, registers.getValue(1), description)

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test all valid register combinations`() {
        // Test all valid source and target register combinations (0-7)
        for (sourceReg in 0..7) {
            for (targetReg in 0..7) {
                val instruction = String.format("E%X%X0", sourceReg, targetReg)
                val testValue = sourceReg % 16 // Keep within valid range

                registers.setValue(sourceReg, testValue)

                val convertInstruction = ConvertByteToAscii(instruction, registers, rom, ram)
                convertInstruction.execute()

                val expectedAscii = if (testValue <= 9) {
                    48 + testValue // '0' + value
                } else {
                    65 + (testValue - 10) // 'A' + (value - 10)
                }

                assertEquals(expectedAscii, registers.getValue(targetReg))
                if (sourceReg != targetReg) {
                    assertEquals(testValue, registers.getValue(sourceReg)) // Source unchanged if different
                }

                // Reset for next test
                setUp()
            }
        }
    }

    @Test
    fun `test instruction parsing edge cases`() {
        // Test with minimum hex values (E000)
        registers.setValue(0, 5)
        val convertInstruction1 = ConvertByteToAscii("E000", registers, rom, ram)
        convertInstruction1.execute()
        assertEquals(53, registers.getValue(0)) // Overwrites source

        // Test with maximum valid hex values for registers (E770)
        setUp()
        registers.setValue(7, 12)
        val convertInstruction2 = ConvertByteToAscii("E770", registers, rom, ram)
        convertInstruction2.execute()
        assertEquals(67, registers.getValue(7)) // 'C' = 67
    }

    @Test
    fun `test consecutive conversions`() {
        // Test multiple conversions in sequence
        val values = listOf(0, 5, 10, 15)
        val expectedAscii = listOf(48, 53, 65, 70) // '0', '5', 'A', 'F'

        values.forEachIndexed { index, value ->
            registers.setValue(0, value)

            val convertInstruction = ConvertByteToAscii("E010", registers, rom, ram)
            convertInstruction.execute()

            assertEquals(expectedAscii[index], registers.getValue(1))

            // Reset for next iteration (except the last one)
            if (index < values.size - 1) {
                setUp()
            }
        }
    }

    @Test
    fun `test ASCII character validation`() {
        // Verify that the ASCII values produced are correct characters
        val testCases = listOf(
            Triple(0, 48, '0'),
            Triple(1, 49, '1'),
            Triple(5, 53, '5'),
            Triple(9, 57, '9'),
            Triple(10, 65, 'A'),
            Triple(11, 66, 'B'),
            Triple(15, 70, 'F')
        )

        testCases.forEach { (input, expectedAscii, expectedChar) ->
            registers.setValue(0, input)

            val convertInstruction = ConvertByteToAscii("E010", registers, rom, ram)
            convertInstruction.execute()

            val actualAscii = registers.getValue(1)
            assertEquals(expectedAscii, actualAscii)
            assertEquals(expectedChar.code, actualAscii)
            assertEquals(expectedChar, actualAscii.toChar())

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test instruction format with ignored Z parameter`() {
        // Test instruction EXYZ: Z parameter should be ignored
        registers.setValue(1, 8)

        val testCases = listOf(
            "E120", "E121", "E125", "E12A", "E12F"
        )

        testCases.forEach { instruction ->
            val convertInstruction = ConvertByteToAscii(instruction, registers, rom, ram)
            convertInstruction.execute()

            // Should always convert r1 to r2, regardless of Z value
            assertEquals(56, registers.getValue(2)) // '8' = 56
            assertEquals(8, registers.getValue(1))  // Source unchanged

            // Reset for next test
            setUp()
            registers.setValue(1, 8)
        }
    }

    @Test
    fun `test error handling for edge case values`() {
        // Test values right at the boundary
        val edgeCases = listOf(
            Triple(15, true, "maximum valid"),
            Triple(16, false, "minimum invalid"),
            Triple(17, false, "just over boundary"),
            Triple(255, false, "maximum byte value")
        )

        edgeCases.forEach { (value, shouldSucceed, description) ->
            registers.setValue(0, value)

            val convertInstruction = ConvertByteToAscii("E010", registers, rom, ram)

            if (shouldSucceed) {
                assertDoesNotThrow("$description should succeed") {
                    convertInstruction.execute()
                }
                assertEquals(70, registers.getValue(1)) // 'F' for value 15
            } else {
                val exception = assertThrows<RuntimeException>("$description should throw") {
                    convertInstruction.execute()
                }
                assertTrue(exception.message!!.contains("CONVERT_BYTE_TO_ASCII error"))
            }

            // Reset for next test
            setUp()
        }
    }
}