package org.example

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*

class StoreTests {

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
    fun `test basic store operation`() {
        // Execute: Store value 42 (0x2A) in r0 (instruction 002A)
        val storeInstruction = Store("002A", registers, rom, ram)
        storeInstruction.execute()

        // Verify: r0 should contain 42
        assertEquals(42, registers.getValue(0))
    }

    @Test
    fun `test store from timer program example`() {
        // From timer.d5700: 0039 stores ASCII '9' (57) in r0
        val storeInstruction = Store("0039", registers, rom, ram)
        storeInstruction.execute()

        assertEquals(57, registers.getValue(0)) // 0x39 = 57 = ASCII '9'
    }

    @Test
    fun `test store from hello program examples`() {
        // From hello.d5700: 0048 stores ASCII 'H' (72) in r0
        val storeInstruction1 = Store("0048", registers, rom, ram)
        storeInstruction1.execute()
        assertEquals(72, registers.getValue(0)) // 0x48 = 72 = ASCII 'H'

        // From hello.d5700: 0145 stores ASCII 'E' (69) in r1
        val storeInstruction2 = Store("0145", registers, rom, ram)
        storeInstruction2.execute()
        assertEquals(69, registers.getValue(1)) // 0x45 = 69 = ASCII 'E'

        // From hello.d5700: 024C stores ASCII 'L' (76) in r2
        val storeInstruction3 = Store("024C", registers, rom, ram)
        storeInstruction3.execute()
        assertEquals(76, registers.getValue(2)) // 0x4C = 76 = ASCII 'L'
    }

    @Test
    fun `test store in different registers`() {
        // Test storing in various registers using correct 0XYZ format
        val testCases = listOf(
            Triple("0150", 1, 80),   // Store 80 (0x50) in r1
            Triple("02FF", 2, 255),  // Store 255 (0xFF) in r2
            Triple("0301", 3, 1),    // Store 1 in r3
            Triple("0700", 7, 0)     // Store 0 in r7
        )

        testCases.forEach { (instruction, register, value) ->
            val storeInstruction = Store(instruction, registers, rom, ram)
            storeInstruction.execute()

            assertEquals(value, registers.getValue(register))
        }
    }

    @Test
    fun `test store boundary values`() {
        // Test minimum value (0)
        val storeInstruction1 = Store("0000", registers, rom, ram)
        storeInstruction1.execute()
        assertEquals(0, registers.getValue(0))

        // Test maximum 8-bit value (255)
        val storeInstruction2 = Store("01FF", registers, rom, ram)
        storeInstruction2.execute()
        assertEquals(255, registers.getValue(1))
    }

    @Test
    fun `test store overwrites existing value`() {
        // Set up: r2 initially has a value
        registers.setValue(2, 100)
        assertEquals(100, registers.getValue(2)) // Verify initial value

        // Execute: Store new value 50 (0x32) in r2
        val storeInstruction = Store("0232", registers, rom, ram)
        storeInstruction.execute()

        // Verify: r2 should contain new value 50
        assertEquals(50, registers.getValue(2))
    }

    @Test
    fun `test store ASCII characters`() {
        // Test storing common ASCII values using correct format
        val asciiTestCases = listOf(
            Triple("0020", 0, 32),   // Space
            Triple("0141", 1, 65),   // 'A'
            Triple("0248", 2, 72),   // 'H'
            Triple("0330", 3, 48),   // '0'
            Triple("0439", 4, 57)    // '9'
        )

        asciiTestCases.forEach { (instruction, register, ascii) ->
            val storeInstruction = Store(instruction, registers, rom, ram)
            storeInstruction.execute()

            assertEquals(ascii, registers.getValue(register))
        }
    }

    @Test
    fun `test all valid registers`() {
        // Test storing in all valid registers (0-7) using correct format
        for (register in 0..7) {
            val value = register * 10 + 5 // Unique value for each register
            val instruction = String.format("0%X%02X", register, value)

            val storeInstruction = Store(instruction, registers, rom, ram)
            storeInstruction.execute()

            assertEquals(value, registers.getValue(register))
        }
    }

    @Test
    fun `test hexadecimal parsing`() {
        val hexTestCases = listOf(
            Triple("001A", 0, 26),   // 0x1A = 26
            Triple("013F", 1, 63),   // 0x3F = 63
            Triple("0280", 2, 128),  // 0x80 = 128
            Triple("03C0", 3, 192),  // 0xC0 = 192
            Triple("04AA", 4, 170),  // 0xAA = 170
            Triple("057F", 5, 127)   // 0x7F = 127
        )

        hexTestCases.forEach { (instruction, register, expectedValue) ->
            val storeInstruction = Store(instruction, registers, rom, ram)
            storeInstruction.execute()

            assertEquals(expectedValue, registers.getValue(register))
        }
    }

    @Test
    fun `test store doesn't affect other registers`() {
        // Set up: Initialize multiple registers
        registers.setValue(0, 10)
        registers.setValue(1, 20)
        registers.setValue(2, 30)
        registers.setValue(3, 40)

        // Execute: Store value in r1 only using correct format
        val storeInstruction = Store("0199", registers, rom, ram)
        storeInstruction.execute()

        // Verify: Only r1 changed, others unchanged
        assertEquals(10, registers.getValue(0))  // Unchanged
        assertEquals(153, registers.getValue(1)) // Changed (0x99 = 153)
        assertEquals(30, registers.getValue(2))  // Unchanged
        assertEquals(40, registers.getValue(3))  // Unchanged
    }

    @Test
    fun `test program counter increment`() {
        val initialPC = rom.getProgramCounter()

        val storeInstruction = Store("0042", registers, rom, ram)
        storeInstruction.execute()

        // Verify PC was incremented
        assertEquals(initialPC + 1, rom.getProgramCounter())
    }

    @Test
    fun `test complete instruction execution flow`() {
        val initialPC = rom.getProgramCounter()

        // Execute: Store 123 (0x7B) in r6 using correct format
        val storeInstruction = Store("067B", registers, rom, ram)
        storeInstruction.execute()

        // Verify all aspects:
        assertEquals(123, registers.getValue(6))     // Value stored
        assertEquals(initialPC + 1, rom.getProgramCounter()) // PC incremented
    }

    @Test
    fun `test instruction format validation`() {
        // Test that the instruction correctly extracts register and value
        // For instruction "05BC": register = 5, value = 0xBC = 188
        val storeInstruction = Store("05BC", registers, rom, ram)
        storeInstruction.execute()

        assertEquals(188, registers.getValue(5))

        // Verify other registers are unaffected (should still be 0)
        for (i in 0..7) {
            if (i != 5) {
                assertEquals(0, registers.getValue(i))
            }
        }
    }
}