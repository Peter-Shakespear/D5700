package org.example

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*

class SetTTests {

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
    fun `test basic set T register`() {
        // Set up: T register initially 0
        assertEquals(0, registers.T)

        // Execute: Set T to 100 (instruction 6064)
        val setTInstruction = SetT("6064", registers, rom, ram)
        setTInstruction.execute()

        // Verify: T should contain 100
        assertEquals(100, registers.T)
    }

    @Test
    fun `test set T to zero`() {
        // Set up: T register initially has some value
        registers.setT(150)
        assertEquals(150, registers.T)

        // Execute: Set T to 0 (instruction 6000)
        val setTInstruction = SetT("6000", registers, rom, ram)
        setTInstruction.execute()

        // Verify: T should be 0
        assertEquals(0, registers.T)
    }

    @Test
    fun `test set T to maximum 8-bit value`() {
        // Execute: Set T to 255 (0xFF) (instruction 60FF)
        val setTInstruction = SetT("60FF", registers, rom, ram)
        setTInstruction.execute()

        // Verify: T should contain 255
        assertEquals(255, registers.T)
    }

    @Test
    fun `test byte organization parsing`() {
        // Test instruction 6XYZ: extracts 8-bit value (YZ), X ignored
        val testCases = listOf(
            Pair("6000", 0),      // 0x00 = 0
            Pair("6001", 1),      // 0x01 = 1
            Pair("6010", 16),     // 0x10 = 16
            Pair("6064", 100),    // 0x64 = 100
            Pair("607F", 127),    // 0x7F = 127
            Pair("6080", 128),    // 0x80 = 128
            Pair("60FF", 255),    // 0xFF = 255
            Pair("6A64", 100),    // X=A ignored, YZ=0x64 = 100
            Pair("6F80", 128)     // X=F ignored, YZ=0x80 = 128
        )

        testCases.forEach { (instruction, expectedValue) ->
            val setTInstruction = SetT(instruction, registers, rom, ram)
            setTInstruction.execute()

            assertEquals(expectedValue, registers.T)

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test set T doesn't affect other registers`() {
        // Set up: Initialize all registers
        registers.setValue(0, 10)
        registers.setValue(1, 20)
        registers.setValue(2, 30)
        registers.setValue(3, 40)
        registers.setValue(4, 50)
        registers.setValue(5, 60)
        registers.setValue(6, 70)
        registers.setValue(7, 80)
        registers.setA(100)
        registers.setM(1)
        registers.setT(99)

        // Execute: Set T to 200
        val setTInstruction = SetT("60C8", registers, rom, ram)
        setTInstruction.execute()

        // Verify: Only T register changed, all others unchanged
        assertEquals(10, registers.getValue(0))
        assertEquals(20, registers.getValue(1))
        assertEquals(30, registers.getValue(2))
        assertEquals(40, registers.getValue(3))
        assertEquals(50, registers.getValue(4))
        assertEquals(60, registers.getValue(5))
        assertEquals(70, registers.getValue(6))
        assertEquals(80, registers.getValue(7))
        assertEquals(100, registers.A)   // Unchanged
        assertEquals(1, registers.M)     // Unchanged
        assertEquals(200, registers.T)   // Only this changed
    }

    @Test
    fun `test program counter increment`() {
        val initialPC = rom.getProgramCounter()

        val setTInstruction = SetT("6064", registers, rom, ram)
        setTInstruction.execute()

        // Verify PC was incremented
        assertEquals(initialPC + 1, rom.getProgramCounter())
    }

    @Test
    fun `test overwrite existing T value`() {
        // Set up: T register initially has a value
        registers.setT(50)
        assertEquals(50, registers.T)

        // Execute: Set T to new value 150
        val setTInstruction = SetT("6096", registers, rom, ram)
        setTInstruction.execute()

        // Verify: T should contain new value
        assertEquals(150, registers.T)
    }

    @Test
    fun `test complete instruction execution flow`() {
        val initialPC = rom.getProgramCounter()

        // Execute: Set T to 123 (instruction 607B)
        val setTInstruction = SetT("607B", registers, rom, ram)
        setTInstruction.execute()

        // Verify all aspects:
        assertEquals(123, registers.T)                         // T set to value
        assertEquals(initialPC + 1, rom.getProgramCounter())   // PC incremented

        // Verify other registers unchanged
        for (i in 0..7) {
            assertEquals(0, registers.getValue(i))
        }
        assertEquals(0, registers.A)
        assertEquals(0, registers.M)
    }

    @Test
    fun `test 8-bit values`() {
        // Test various 8-bit values
        val testValues = listOf(0, 1, 15, 16, 31, 32, 63, 64, 127, 128, 191, 192, 255)

        testValues.forEach { value ->
            val instruction = String.format("60%02X", value)

            val setTInstruction = SetT(instruction, registers, rom, ram)
            setTInstruction.execute()

            assertEquals(value, registers.T)

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test hexadecimal parsing`() {
        val hexTestCases = listOf(
            Triple("600A", 10, "0x0A = 10"),
            Triple("6014", 20, "0x14 = 20"),
            Triple("6032", 50, "0x32 = 50"),
            Triple("6064", 100, "0x64 = 100"),
            Triple("607F", 127, "0x7F = 127"),
            Triple("6080", 128, "0x80 = 128"),
            Triple("60AA", 170, "0xAA = 170"),
            Triple("60FF", 255, "0xFF = 255")
        )

        hexTestCases.forEach { (instruction, expectedValue, description) ->
            val setTInstruction = SetT(instruction, registers, rom, ram)
            setTInstruction.execute()

            assertEquals(expectedValue, registers.T, description)

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test boundary values`() {
        val boundaryTests = listOf(
            Triple(0, "minimum 8-bit value", "6000"),
            Triple(255, "maximum 8-bit value", "60FF"),
            Triple(128, "middle 8-bit value", "6080"),
            Triple(1, "minimum non-zero", "6001"),
            Triple(254, "maximum-1 value", "60FE")
        )

        boundaryTests.forEach { (expectedValue, description, instruction) ->
            val setTInstruction = SetT(instruction, registers, rom, ram)
            setTInstruction.execute()

            assertEquals(expectedValue, registers.T, description)

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test consecutive set T operations`() {
        val values = listOf(10, 20, 50, 100, 200, 255, 0)
        val initialPC = rom.getProgramCounter()

        values.forEachIndexed { index, value ->
            val instruction = String.format("60%02X", value)

            val setTInstruction = SetT(instruction, registers, rom, ram)
            setTInstruction.execute()

            assertEquals(value, registers.T)
            assertEquals(initialPC + index + 1, rom.getProgramCounter())
        }
    }

    @Test
    fun `test 8-bit mask application`() {
        // Test that only 8 bits are used (0xFF mask)
        val setTInstruction = SetT("60FF", registers, rom, ram)
        setTInstruction.execute()

        // Should be exactly 255 (0xFF)
        assertEquals(255, registers.T)
        assertTrue(registers.T <= 255, "T register should not exceed 8-bit maximum")
    }

    @Test
    fun `test set T with program examples`() {
        // From keyboard.d5700 and addition.d5700: 6000 sets T to 0
        val setTInstruction1 = SetT("6000", registers, rom, ram)
        setTInstruction1.execute()
        assertEquals(0, registers.T)

        // From keyboard.d5700 and addition.d5700: 6100 sets T to 0 (only last 8 bits matter)
        val setTInstruction2 = SetT("6100", registers, rom, ram)
        setTInstruction2.execute()
        assertEquals(0, registers.T)

        // Test with a non-zero timer value
        val setTInstruction3 = SetT("605A", registers, rom, ram) // Set T to 90
        setTInstruction3.execute()
        assertEquals(90, registers.T)
    }

    @Test
    fun `test instruction format with different first nibbles`() {
        // Test that the first nibble after 6 is ignored (6XYZ format)
        val testCases = listOf(
            Pair("6050", 80),   // X=0, YZ=0x50 = 80
            Pair("6150", 80),   // X=1, YZ=0x50 = 80
            Pair("6250", 80),   // X=2, YZ=0x50 = 80
            Pair("6A50", 80),   // X=A, YZ=0x50 = 80
            Pair("6F50", 80)    // X=F, YZ=0x50 = 80
        )

        testCases.forEach { (instruction, expectedValue) ->
            val setTInstruction = SetT(instruction, registers, rom, ram)
            setTInstruction.execute()

            assertEquals(expectedValue, registers.T)

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test instruction format validation`() {
        // Test various valid instruction formats
        val validInstructions = listOf(
            "6000", "6001", "6010", "6050", "6080", "60AA",
            "60BB", "60CC", "60DD", "60EE", "60FF"
        )

        validInstructions.forEach { instruction ->
            val setTInstruction = SetT(instruction, registers, rom, ram)

            assertDoesNotThrow {
                setTInstruction.execute()
            }

            // Value should be within valid 8-bit range
            assertTrue(registers.T >= 0 && registers.T <= 255)

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test timer functionality context`() {
        // Test setting T for timer/countdown scenarios
        val timerValues = listOf(60, 30, 10, 5, 1) // Common countdown values

        timerValues.forEach { timerValue ->
            val instruction = String.format("60%02X", timerValue)

            val setTInstruction = SetT(instruction, registers, rom, ram)
            setTInstruction.execute()

            assertEquals(timerValue, registers.T)
            assertTrue(registers.T > 0, "Timer should be positive for countdown")

            // Reset for next test
            setUp()
        }
    }
}