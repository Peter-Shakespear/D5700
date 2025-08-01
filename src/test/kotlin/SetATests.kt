
package org.example

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*

class SetATests {

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
    fun `test basic set A register`() {
        // Set up: A register initially 0
        assertEquals(0, registers.A)

        // Execute: Set A to 100 (instruction A064)
        val setAInstruction = SetA("A064", registers, rom, ram)
        setAInstruction.execute()

        // Verify: A should contain 100
        assertEquals(100, registers.A)
    }

    @Test
    fun `test set A to zero`() {
        // Set up: A register initially has some value
        registers.setA(500)
        assertEquals(500, registers.A)

        // Execute: Set A to 0 (instruction A000)
        val setAInstruction = SetA("A000", registers, rom, ram)
        setAInstruction.execute()

        // Verify: A should be 0
        assertEquals(0, registers.A)
    }

    @Test
    fun `test set A to maximum 12-bit value`() {
        // Execute: Set A to 4095 (0xFFF) (instruction AFFF)
        val setAInstruction = SetA("AFFF", registers, rom, ram)
        setAInstruction.execute()

        // Verify: A should contain 4095
        assertEquals(4095, registers.A)
    }

    @Test
    fun `test byte organization parsing`() {
        // Test instruction AXYZ: extracts 12-bit value (XYZ)
        val testCases = listOf(
            Pair("A000", 0),      // 0x000 = 0
            Pair("A001", 1),      // 0x001 = 1
            Pair("A010", 16),     // 0x010 = 16
            Pair("A100", 256),    // 0x100 = 256
            Pair("A064", 100),    // 0x064 = 100
            Pair("A1F4", 500),    // 0x1F4 = 500
            Pair("A3E8", 1000),   // 0x3E8 = 1000
            Pair("AFFF", 4095)    // 0xFFF = 4095
        )

        testCases.forEach { (instruction, expectedValue) ->
            val setAInstruction = SetA(instruction, registers, rom, ram)
            setAInstruction.execute()

            assertEquals(expectedValue, registers.A)

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test set A doesn't affect other registers`() {
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

        // Execute: Set A to 500
        val setAInstruction = SetA("A1F4", registers, rom, ram)
        setAInstruction.execute()

        // Verify: Only A register changed, all others unchanged
        assertEquals(10, registers.getValue(0))
        assertEquals(20, registers.getValue(1))
        assertEquals(30, registers.getValue(2))
        assertEquals(40, registers.getValue(3))
        assertEquals(50, registers.getValue(4))
        assertEquals(60, registers.getValue(5))
        assertEquals(70, registers.getValue(6))
        assertEquals(80, registers.getValue(7))
        assertEquals(500, registers.A) // Only this changed
        assertEquals(1, registers.M)   // Unchanged
        assertEquals(99, registers.T)  // Unchanged
    }

    @Test
    fun `test program counter increment`() {
        val initialPC = rom.getProgramCounter()

        val setAInstruction = SetA("A064", registers, rom, ram)
        setAInstruction.execute()

        // Verify PC was incremented
        assertEquals(initialPC + 1, rom.getProgramCounter())
    }

    @Test
    fun `test overwrite existing A value`() {
        // Set up: A register initially has a value
        registers.setA(1000)
        assertEquals(1000, registers.A)

        // Execute: Set A to new value 2000
        val setAInstruction = SetA("A7D0", registers, rom, ram)
        setAInstruction.execute()

        // Verify: A should contain new value
        assertEquals(2000, registers.A)
    }

    @Test
    fun `test complete instruction execution flow`() {
        val initialPC = rom.getProgramCounter()

        // Execute: Set A to 1234 (instruction A4D2)
        val setAInstruction = SetA("A4D2", registers, rom, ram)
        setAInstruction.execute()

        // Verify all aspects:
        assertEquals(1234, registers.A)                        // A set to value
        assertEquals(initialPC + 1, rom.getProgramCounter())   // PC incremented

        // Verify other registers unchanged
        for (i in 0..7) {
            assertEquals(0, registers.getValue(i))
        }
        assertEquals(0, registers.M)
        assertEquals(0, registers.T)
    }

    @Test
    fun `test address values for memory operations`() {
        // Test setting A to common memory addresses
        val memoryAddresses = listOf(0, 100, 500, 1000, 2048, 4095)

        memoryAddresses.forEach { address ->
            val instruction = String.format("A%03X", address)

            val setAInstruction = SetA(instruction, registers, rom, ram)
            setAInstruction.execute()

            assertEquals(address, registers.A)

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test hexadecimal parsing`() {
        val hexTestCases = listOf(
            Triple("A00A", 10, "0x00A = 10"),
            Triple("A014", 20, "0x014 = 20"),
            Triple("A032", 50, "0x032 = 50"),
            Triple("A064", 100, "0x064 = 100"),
            Triple("A12C", 300, "0x12C = 300"),
            Triple("A2BC", 700, "0x2BC = 700"),
            Triple("AAAA", 2730, "0xAAA = 2730"),
            Triple("ABCD", 2765, "0xBCD = 2765")
        )

        hexTestCases.forEach { (instruction, expectedValue, description) ->
            val setAInstruction = SetA(instruction, registers, rom, ram)
            setAInstruction.execute()

            assertEquals(expectedValue, registers.A, description)

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test boundary values`() {
        val boundaryTests = listOf(
            Triple(0, "minimum value", "A000"),
            Triple(4095, "maximum 12-bit value", "AFFF"),
            Triple(2048, "middle value", "A800"),
            Triple(1, "minimum non-zero", "A001"),
            Triple(4094, "maximum-1 value", "AFFE")
        )

        boundaryTests.forEach { (expectedValue, description, instruction) ->
            val setAInstruction = SetA(instruction, registers, rom, ram)
            setAInstruction.execute()

            assertEquals(expectedValue, registers.A, description)

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test consecutive set A operations`() {
        val values = listOf(100, 200, 500, 1000, 0, 4095)
        val initialPC = rom.getProgramCounter()

        values.forEachIndexed { index, value ->
            val instruction = String.format("A%03X", value)

            val setAInstruction = SetA(instruction, registers, rom, ram)
            setAInstruction.execute()

            assertEquals(value, registers.A)
            assertEquals(initialPC + index + 1, rom.getProgramCounter())
        }
    }

    @Test
    fun `test 12-bit mask application`() {
        // Test that only 12 bits are used (0xFFF mask)
        // If somehow a larger value were parsed, it should be masked
        val setAInstruction = SetA("AFFF", registers, rom, ram)
        setAInstruction.execute()

        // Should be exactly 4095 (0xFFF)
        assertEquals(4095, registers.A)
        assertTrue(registers.A <= 4095, "A register should not exceed 12-bit maximum")
    }

    @Test
    fun `test set A with program examples`() {
        // From hello_from_rom.d5700: A01E sets A to 0x1E (30)
        val setAInstruction1 = SetA("A01E", registers, rom, ram)
        setAInstruction1.execute()
        assertEquals(30, registers.A)

        // From hello_from_rom.d5700: A01F sets A to 0x1F (31)
        val setAInstruction2 = SetA("A01F", registers, rom, ram)
        setAInstruction2.execute()
        assertEquals(31, registers.A)

        // From hello_from_rom.d5700: A020 sets A to 0x20 (32)
        val setAInstruction3 = SetA("A020", registers, rom, ram)
        setAInstruction3.execute()
        assertEquals(32, registers.A)

        // From hello_from_rom.d5700: A021 sets A to 0x21 (33)
        val setAInstruction4 = SetA("A021", registers, rom, ram)
        setAInstruction4.execute()
        assertEquals(33, registers.A)
    }

    @Test
    fun `test instruction format validation`() {
        // Test various valid instruction formats
        val validInstructions = listOf(
            "A000", "A001", "A010", "A100", "A123", "A456",
            "A789", "AABC", "ADEF", "AFFF"
        )

        validInstructions.forEach { instruction ->
            val setAInstruction = SetA(instruction, registers, rom, ram)

            assertDoesNotThrow {
                setAInstruction.execute()
            }

            // Value should be within valid 12-bit range
            assertTrue(registers.A >= 0 && registers.A <= 4095)

            // Reset for next test
            setUp()
        }
    }
}