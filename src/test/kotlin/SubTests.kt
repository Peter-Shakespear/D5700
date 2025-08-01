package org.example

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*

class SubTests {

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
    fun `test basic subtraction operation`() {
        // Set up: r0 = 20, r1 = 5
        registers.setValue(0, 20)
        registers.setValue(1, 5)

        // Execute: Subtract r1 from r0 and store in r2 (instruction 2012)
        val subInstruction = Subtract("2012", registers, rom, ram)
        subInstruction.execute()

        // Verify: r2 should contain 15 (20 - 5)
        assertEquals(15, registers.getValue(2))
        assertEquals(20, registers.getValue(0)) // r0 unchanged
        assertEquals(5, registers.getValue(1))  // r1 unchanged
    }

    @Test
    fun `test subtraction from program example`() {
        // From subtraction.d5700: 2010 subtracts r1 from r0 and stores in r0
        registers.setValue(0, 50)
        registers.setValue(1, 25)

        val subInstruction = Subtract("2010", registers, rom, ram)
        subInstruction.execute()

        // r0 should now contain 25 (50 - 25)
        assertEquals(25, registers.getValue(0))
        assertEquals(25, registers.getValue(1)) // r1 unchanged
    }

    @Test
    fun `test subtraction with zero`() {
        // Set up: r0 = 15, r1 = 0
        registers.setValue(0, 15)
        registers.setValue(1, 0)

        // Execute: Subtract r1 from r0 and store in r3 (instruction 2013)
        val subInstruction = Subtract("2013", registers, rom, ram)
        subInstruction.execute()

        // Verify: r3 should contain 15 (15 - 0)
        assertEquals(15, registers.getValue(3))
    }

    @Test
    fun `test subtraction resulting in zero`() {
        // Set up: r0 = 10, r1 = 10
        registers.setValue(0, 10)
        registers.setValue(1, 10)

        // Execute: Subtract r1 from r0 and store in r2 (instruction 2012)
        val subInstruction = Subtract("2012", registers, rom, ram)
        subInstruction.execute()

        // Verify: r2 should contain 0 (10 - 10)
        assertEquals(0, registers.getValue(2))
    }

    @Test
    fun `test subtraction storing in same register as operand`() {
        // Set up: r0 = 15, r1 = 7
        registers.setValue(0, 15)
        registers.setValue(1, 7)

        // Execute: Subtract r1 from r0 and store in r0 (instruction 2010)
        val subInstruction = Subtract("2010", registers, rom, ram)
        subInstruction.execute()

        // Verify: r0 should contain 8 (overwrites original value)
        assertEquals(8, registers.getValue(0))
        assertEquals(7, registers.getValue(1)) // r1 unchanged
    }

    @Test
    fun `test subtraction with same source registers`() {
        // Set up: r2 = 12
        registers.setValue(2, 12)

        // Execute: Subtract r2 from r2 and store in r1 (instruction 2221)
        val subInstruction = Subtract("2221", registers, rom, ram)
        subInstruction.execute()

        // Verify: r1 should contain 0 (12 - 12)
        assertEquals(0, registers.getValue(1))
        assertEquals(12, registers.getValue(2)) // r2 unchanged
    }

    @Test
    fun `test byte organization parsing`() {
        // Test instruction 2567: rX=5, rY=6, rZ=7
        val subInstruction = Subtract("2567", registers, rom, ram)

        // Set up: r5 = 30, r6 = 10
        registers.setValue(5, 30)
        registers.setValue(6, 10)

        subInstruction.execute()

        // Verify: r7 should contain 20 (30 - 10)
        assertEquals(20, registers.getValue(7))
    }

    @Test
    fun `test large numbers subtraction`() {
        // Set up: r0 = 255, r1 = 55
        registers.setValue(0, 255)
        registers.setValue(1, 55)

        // Execute: Subtract r1 from r0 and store in r2 (instruction 2012)
        val subInstruction = Subtract("2012", registers, rom, ram)
        subInstruction.execute()

        // Verify: r2 should contain 200
        assertEquals(200, registers.getValue(2))
    }

    @Test
    fun `test subtraction with negative result`() {
        // Set up: r0 = 5, r1 = 10 (should result in negative)
        registers.setValue(0, 5)
        registers.setValue(1, 10)

        // Execute: Subtract r1 from r0 and store in r2 (instruction 2012)
        val subInstruction = Subtract("2012", registers, rom, ram)
        subInstruction.execute()

        // Verify: result should be -5 (if negative numbers are supported)
        // or underflow behavior (depends on system implementation)
        val result = registers.getValue(2)
        assertTrue(result == -5 || result >= 250, // Underflow would wrap around
            "Result should handle negative appropriately, got: $result")
    }

    @Test
    fun `test subtraction doesn't affect other registers`() {
        // Set up: Initialize multiple registers
        registers.setValue(0, 100)
        registers.setValue(1, 30)
        registers.setValue(2, 50)
        registers.setValue(3, 75)

        // Execute: Subtract r1 from r0 and store in r2 (instruction 2012)
        val subInstruction = Subtract("2012", registers, rom, ram)
        subInstruction.execute()

        // Verify: Only r2 changed, others unchanged
        assertEquals(100, registers.getValue(0)) // Unchanged
        assertEquals(30, registers.getValue(1))  // Unchanged
        assertEquals(70, registers.getValue(2))  // Changed (100 - 30)
        assertEquals(75, registers.getValue(3))  // Unchanged
    }

    @Test
    fun `test program counter increment`() {
        val initialPC = rom.getProgramCounter()

        registers.setValue(0, 10)
        registers.setValue(1, 3)

        val subInstruction = Subtract("2012", registers, rom, ram)
        subInstruction.execute()

        // Verify PC was incremented
        assertEquals(initialPC + 1, rom.getProgramCounter())
    }

    @Test
    fun `test complete instruction execution flow`() {
        val initialPC = rom.getProgramCounter()

        // Set up: r5 = 50, r6 = 18
        registers.setValue(5, 50)
        registers.setValue(6, 18)

        // Execute: Subtract r6 from r5 and store in r7 (instruction 2567)
        val subInstruction = Subtract("2567", registers, rom, ram)
        subInstruction.execute()

        // Verify all aspects:
        assertEquals(32, registers.getValue(7))        // Subtraction result
        assertEquals(50, registers.getValue(5))        // Source unchanged
        assertEquals(18, registers.getValue(6))        // Source unchanged
        assertEquals(initialPC + 1, rom.getProgramCounter()) // PC incremented
    }

    @Test
    fun `test instruction parsing edge cases`() {
        // Test with minimum hex values - Subtract r0 from r0 and store in r0
        val subInstruction1 = Subtract("2000", registers, rom, ram)
        registers.setValue(0, 5)
        subInstruction1.execute()
        assertEquals(0, registers.getValue(0)) // 5 - 5 = 0

        // Test with maximum valid hex values for register addresses (0-7)
        // Subtract r7 from r7 and store in r7
        val subInstruction2 = Subtract("2777", registers, rom, ram)
        registers.setValue(7, 15)
        subInstruction2.execute()
        assertEquals(0, registers.getValue(7)) // 15 - 15 = 0
    }

    @Test
    fun `test subtraction boundary values`() {
        // Test minimum subtraction (0 - 0)
        registers.setValue(0, 0)
        registers.setValue(1, 0)
        val subInstruction1 = Subtract("2012", registers, rom, ram)
        subInstruction1.execute()
        assertEquals(0, registers.getValue(2))

        // Test maximum valid subtraction without underflow
        registers.setValue(3, 255)
        registers.setValue(4, 0)
        val subInstruction2 = Subtract("2342", registers, rom, ram)
        subInstruction2.execute()
        assertEquals(255, registers.getValue(2))
    }

    @Test
    fun `test subtraction from timer program pattern`() {
        // Similar to timer.d5700 pattern: decrementing ASCII values
        registers.setValue(0, 57) // ASCII '9'
        registers.setValue(1, 1)  // Decrement by 1

        val subInstruction = Subtract("2010", registers, rom, ram)
        subInstruction.execute()

        // Should now contain ASCII '8' (56)
        assertEquals(56, registers.getValue(0))
    }

    @Test
    fun `test negative numbers subtraction`() {
        // Test with negative numbers if supported
        registers.setValue(0, -5)
        registers.setValue(1, 3)

        val subInstruction = Subtract("2012", registers, rom, ram)
        subInstruction.execute()

        // Result should be -8 if negative numbers are supported
        assertEquals(-8, registers.getValue(2))
    }

    @Test
    fun `test subtraction with zero operands`() {
        // Test various zero scenarios
        val testCases = listOf(
            Triple(0, 10, -10),   // 0 - 10 = -10
            Triple(10, 0, 10),    // 10 - 0 = 10
            Triple(0, 0, 0)       // 0 - 0 = 0
        )

        testCases.forEachIndexed { index, (valueX, valueY, expected) ->
            registers.setValue(0, valueX)
            registers.setValue(1, valueY)

            val subInstruction = Subtract("2012", registers, rom, ram)
            subInstruction.execute()

            val result = registers.getValue(2)
            // Handle both signed and unsigned behavior
            assertTrue(result == expected ||
                    (expected < 0 && result > 200), // Underflow wrapping
                "Test case $index: expected $expected, got $result")

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test consecutive subtractions`() {
        // Test multiple subtraction operations
        registers.setValue(0, 100)
        registers.setValue(1, 10)

        // First subtraction: 100 - 10 = 90
        val subInstruction1 = Subtract("2010", registers, rom, ram)
        subInstruction1.execute()
        assertEquals(90, registers.getValue(0))

        // Second subtraction: 90 - 10 = 80
        val subInstruction2 = Subtract("2010", registers, rom, ram)
        subInstruction2.execute()
        assertEquals(80, registers.getValue(0))

        // Third subtraction: 80 - 10 = 70
        val subInstruction3 = Subtract("2010", registers, rom, ram)
        subInstruction3.execute()
        assertEquals(70, registers.getValue(0))
    }
}