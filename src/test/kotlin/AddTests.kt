package org.example

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*

class AddTests {

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
    fun `test basic addition operation`() {
        // Set up: r0 = 10, r1 = 20
        registers.setValue(0, 10)
        registers.setValue(1, 20)

        // Execute: Add r0 + r1 and store in r2 (instruction 1012)
        val addInstruction = Add("1012", registers, rom, ram)
        addInstruction.execute()

        // Verify: r2 should contain 30
        assertEquals(30, registers.getValue(2))
        assertEquals(10, registers.getValue(0)) // r0 unchanged
        assertEquals(20, registers.getValue(1)) // r1 unchanged
    }

    @Test
    fun `test addition with zero`() {
        // Set up: r0 = 0, r1 = 15
        registers.setValue(0, 0)
        registers.setValue(1, 15)

        // Execute: Add r0 + r1 and store in r3 (instruction 1013)
        val addInstruction = Add("1013", registers, rom, ram)
        addInstruction.execute()

        // Verify: r3 should contain 15
        assertEquals(15, registers.getValue(3))
    }

    @Test
    fun `test addition storing in same register as operand`() {
        // Set up: r0 = 5, r1 = 7
        registers.setValue(0, 5)
        registers.setValue(1, 7)

        // Execute: Add r0 + r1 and store in r0 (instruction 1010)
        val addInstruction = Add("1010", registers, rom, ram)
        addInstruction.execute()

        // Verify: r0 should contain 12 (overwrites original value)
        assertEquals(12, registers.getValue(0))
        assertEquals(7, registers.getValue(1)) // r1 unchanged
    }

    @Test
    fun `test addition with same source registers`() {
        // Set up: r2 = 8
        registers.setValue(2, 8)

        // Execute: Add r2 + r2 and store in r1 (instruction 1221)
        val addInstruction = Add("1221", registers, rom, ram)
        addInstruction.execute()

        // Verify: r1 should contain 16 (8 + 8)
        assertEquals(16, registers.getValue(1))
        assertEquals(8, registers.getValue(2)) // r2 unchanged
    }

    @Test
    fun `test byte organization parsing`() {
        // For instruction 1ABC: rX=A(10), rY=B(11), rZ=C(12)
        // But your registers only support 0-7, so let's use a valid instruction
        val addInstruction = Add("1567", registers, rom, ram)

        // Set up: r5 = 10, r6 = 20
        registers.setValue(5, 10)
        registers.setValue(6, 20)

        addInstruction.execute()

        // Verify: r7 should contain 30 (10 + 20)
        assertEquals(30, registers.getValue(7))
    }


    @Test
    fun `test large numbers addition`() {
        // Set up: r0 = 200, r1 = 55
        registers.setValue(0, 200)
        registers.setValue(1, 55)

        // Execute: Add r0 + r1 and store in r2 (instruction 1012)
        val addInstruction = Add("1012", registers, rom, ram)
        addInstruction.execute()

        // Verify: r2 should contain 255
        assertEquals(255, registers.getValue(2))
    }

    @Test
    fun `test addition with overflow`() {
        // Set up: r0 = 255, r1 = 1 (should overflow in 8-bit system)
        registers.setValue(0, 255)
        registers.setValue(1, 1)

        // Execute: Add r0 + r1 and store in r2 (instruction 1012)
        val addInstruction = Add("1012", registers, rom, ram)
        addInstruction.execute()

        // Verify: depends on how your system handles overflow
        // If it wraps around: should be 0 (256 % 256)
        // If it clamps: should be 255
        // If it allows: should be 256
        val result = registers.getValue(2)
        assertTrue(result == 256 || result == 0 || result == 255,
            "Result should handle overflow appropriately, got: $result")
    }

    @Test
    fun `test program counter increment`() {
        val initialPC = rom.getProgramCounter()

        registers.setValue(0, 5)
        registers.setValue(1, 3)

        val addInstruction = Add("1012", registers, rom, ram)
        addInstruction.execute()

        // Verify PC was incremented
        assertEquals(initialPC + 1, rom.getProgramCounter())
    }

    @Test
    fun `test complete instruction execution flow`() {
        val initialPC = rom.getProgramCounter()

        // Set up: r5 = 42, r6 = 18
        registers.setValue(5, 42)
        registers.setValue(6, 18)

        // Execute: Add r5 + r6 and store in r7 (instruction 1567)
        val addInstruction = Add("1567", registers, rom, ram)
        addInstruction.execute()

        // Verify all aspects:
        assertEquals(60, registers.getValue(7))        // Addition result
        assertEquals(42, registers.getValue(5))        // Source unchanged
        assertEquals(18, registers.getValue(6))        // Source unchanged
        assertEquals(initialPC + 1, rom.getProgramCounter()) // PC incremented
    }

    @Test
    fun `test negative numbers addition`() {
        // If your system supports negative numbers
        registers.setValue(0, -10)
        registers.setValue(1, 5)

        val addInstruction = Add("1012", registers, rom, ram)
        addInstruction.execute()

        // Result should be -5 if negative numbers are supported
        assertEquals(-5, registers.getValue(2))
    }

    @Test
    fun `test instruction parsing edge cases`() {
        // Test with minimum hex values - Add r0 + r0 and store in r0
        val addInstruction1 = Add("1000", registers, rom, ram)
        registers.setValue(0, 1)
        addInstruction1.execute()
        assertEquals(2, registers.getValue(0)) // 1 + 1 = 2

        // Test with maximum valid hex values for register addresses (0-7)
        // Add r7 + r7 and store in r7
        val addInstruction2 = Add("1777", registers, rom, ram)
        registers.setValue(7, 10)
        addInstruction2.execute()
        assertEquals(20, registers.getValue(7)) // 10 + 10 = 20
    }
}