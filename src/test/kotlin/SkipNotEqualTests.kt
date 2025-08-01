package org.example

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*

class SkipNotEqualTests {

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
    fun `test skip when values are not equal`() {
        val initialPC = rom.getProgramCounter()

        // Set up: r0 = 10, r1 = 20 (not equal)
        registers.setValue(0, 10)
        registers.setValue(1, 20)

        // Execute: Skip if r0 != r1 (instruction 4010)
        val skipInstruction = SkipNotEqual("4010", registers, rom, ram)
        skipInstruction.execute()

        // Verify: PC should be incremented twice (skip + normal increment)
        assertEquals(initialPC + 2, rom.getProgramCounter())
    }

    @Test
    fun `test no skip when values are equal`() {
        val initialPC = rom.getProgramCounter()

        // Set up: r0 = 15, r1 = 15 (equal)
        registers.setValue(0, 15)
        registers.setValue(1, 15)

        // Execute: Skip if r0 != r1 (instruction 4010)
        val skipInstruction = SkipNotEqual("4010", registers, rom, ram)
        skipInstruction.execute()

        // Verify: PC should be incremented only once (normal increment)
        assertEquals(initialPC + 1, rom.getProgramCounter())
    }

    @Test
    fun `test skip with different register combinations`() {
        val testCases = listOf(
            Triple("4010", 0, 1),  // Compare r0 and r1
            Triple("4230", 2, 3),  // Compare r2 and r3
            Triple("4450", 4, 5),  // Compare r4 and r5
            Triple("4670", 6, 7)   // Compare r6 and r7
        )

        testCases.forEach { (instruction, regX, regY) ->
            val initialPC = rom.getProgramCounter()

            // Set up: different values
            registers.setValue(regX, 10)
            registers.setValue(regY, 20)

            val skipInstruction = SkipNotEqual(instruction, registers, rom, ram)
            skipInstruction.execute()

            // Should skip (PC + 2)
            assertEquals(initialPC + 2, rom.getProgramCounter())

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test byte organization parsing`() {
        // Test instruction 4XYZ: rX=(X), rY=(Y), Z ignored
        registers.setValue(5, 100)
        registers.setValue(6, 200)

        val initialPC = rom.getProgramCounter()

        // Test instruction 4567: should compare r5 and r6 (X=5, Y=6, Z ignored)
        val skipInstruction = SkipNotEqual("4567", registers, rom, ram)
        skipInstruction.execute()

        // Values are different, should skip
        assertEquals(initialPC + 2, rom.getProgramCounter())
    }

    @Test
    fun `test skip with zero values`() {
        val initialPC = rom.getProgramCounter()

        // Set up: r0 = 0, r1 = 0 (equal)
        registers.setValue(0, 0)
        registers.setValue(1, 0)

        val skipInstruction = SkipNotEqual("4010", registers, rom, ram)
        skipInstruction.execute()

        // Should not skip (values are equal)
        assertEquals(initialPC + 1, rom.getProgramCounter())
    }

    @Test
    fun `test skip with zero and non-zero`() {
        val initialPC = rom.getProgramCounter()

        // Set up: r0 = 0, r1 = 5 (not equal)
        registers.setValue(0, 0)
        registers.setValue(1, 5)

        val skipInstruction = SkipNotEqual("4010", registers, rom, ram)
        skipInstruction.execute()

        // Should skip (values are not equal)
        assertEquals(initialPC + 2, rom.getProgramCounter())
    }

    @Test
    fun `test skip doesn't affect registers`() {
        // Set up: Initialize registers
        registers.setValue(0, 10)
        registers.setValue(1, 20)
        registers.setValue(2, 30)
        registers.setValue(3, 40)
        registers.setA(100)
        registers.setM(1)
        registers.setT(99)

        val skipInstruction = SkipNotEqual("4010", registers, rom, ram)
        skipInstruction.execute()

        // Verify: All registers unchanged
        assertEquals(10, registers.getValue(0))
        assertEquals(20, registers.getValue(1))
        assertEquals(30, registers.getValue(2))
        assertEquals(40, registers.getValue(3))
        assertEquals(100, registers.A)
        assertEquals(1, registers.M)
        assertEquals(99, registers.T)
    }

    @Test
    fun `test same register comparison`() {
        val initialPC = rom.getProgramCounter()

        // Set up: r2 = 15
        registers.setValue(2, 15)

        // Execute: Compare r2 with itself (instruction 4220)
        val skipInstruction = SkipNotEqual("4220", registers, rom, ram)
        skipInstruction.execute()

        // Should not skip (same register, always equal)
        assertEquals(initialPC + 1, rom.getProgramCounter())
    }

    @Test
    fun `test large number comparison`() {
        val testCases = listOf(
            Triple(255, 254, true),   // Should skip
            Triple(255, 255, false),  // Should not skip
            Triple(1000, 999, true),  // Should skip
            Triple(1000, 1000, false) // Should not skip
        )

        testCases.forEach { (value1, value2, shouldSkip) ->
            val initialPC = rom.getProgramCounter()

            registers.setValue(0, value1)
            registers.setValue(1, value2)

            val skipInstruction = SkipNotEqual("4010", registers, rom, ram)
            skipInstruction.execute()

            val expectedPC = if (shouldSkip) initialPC + 2 else initialPC + 1
            assertEquals(expectedPC, rom.getProgramCounter())

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test negative number comparison`() {
        val testCases = listOf(
            Triple(-5, -5, false),   // Equal, should not skip
            Triple(-5, -10, true),   // Not equal, should skip
            Triple(-1, 1, true),     // Not equal, should skip
            Triple(0, -1, true)      // Not equal, should skip
        )

        testCases.forEach { (value1, value2, shouldSkip) ->
            val initialPC = rom.getProgramCounter()

            registers.setValue(0, value1)
            registers.setValue(1, value2)

            val skipInstruction = SkipNotEqual("4010", registers, rom, ram)
            skipInstruction.execute()

            val expectedPC = if (shouldSkip) initialPC + 2 else initialPC + 1
            assertEquals(expectedPC, rom.getProgramCounter())

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test all register combinations`() {
        // Test all valid register combinations (0-7)
        for (regX in 0..7) {
            for (regY in 0..7) {
                val initialPC = rom.getProgramCounter()
                val instruction = String.format("4%X%X0", regX, regY)

                // Set different values to ensure skip
                registers.setValue(regX, regX + 10)
                registers.setValue(regY, regY + 20)

                val skipInstruction = SkipNotEqual(instruction, registers, rom, ram)
                skipInstruction.execute()

                if (regX == regY) {
                    // Same register, should not skip
                    assertEquals(initialPC + 1, rom.getProgramCounter())
                } else {
                    // Different registers with different values, should skip
                    assertEquals(initialPC + 2, rom.getProgramCounter())
                }

                // Reset for next test
                setUp()
            }
        }
    }

    @Test
    fun `test complete instruction execution flow`() {
        val initialPC = rom.getProgramCounter()

        // Set up: r3 = 42, r4 = 84 (not equal)
        registers.setValue(3, 42)
        registers.setValue(4, 84)

        // Execute: Skip if r3 != r4 (instruction 4340)
        val skipInstruction = SkipNotEqual("4340", registers, rom, ram)
        skipInstruction.execute()

        // Verify all aspects:
        assertEquals(42, registers.getValue(3))                // Source unchanged
        assertEquals(84, registers.getValue(4))                // Source unchanged
        assertEquals(initialPC + 2, rom.getProgramCounter())   // PC incremented twice (skip)
    }

    @Test
    fun `test instruction parsing edge cases`() {
        // Test with minimum hex values (4000)
        registers.setValue(0, 1)
        registers.setValue(0, 2) // This overwrites, so r0 = 2, comparison with itself

        val initialPC = rom.getProgramCounter()
        val skipInstruction1 = SkipNotEqual("4000", registers, rom, ram)
        skipInstruction1.execute()
        assertEquals(initialPC + 1, rom.getProgramCounter()) // Same register, no skip

        // Test with maximum valid hex values for registers (4770)
        setUp()
        registers.setValue(7, 10)
        registers.setValue(7, 10) // Same register

        val initialPC2 = rom.getProgramCounter()
        val skipInstruction2 = SkipNotEqual("4770", registers, rom, ram)
        skipInstruction2.execute()
        assertEquals(initialPC2 + 1, rom.getProgramCounter()) // Same register, no skip
    }

    @Test
    fun `test consecutive skip operations`() {
        // Test multiple skip operations in sequence
        val initialPC = rom.getProgramCounter()

        // First skip: r0=1, r1=2 (not equal, should skip)
        registers.setValue(0, 1)
        registers.setValue(1, 2)
        val skipInstruction1 = SkipNotEqual("4010", registers, rom, ram)
        skipInstruction1.execute()
        assertEquals(initialPC + 2, rom.getProgramCounter())

        // Second skip: r2=5, r3=5 (equal, should not skip)
        registers.setValue(2, 5)
        registers.setValue(3, 5)
        val skipInstruction2 = SkipNotEqual("4230", registers, rom, ram)
        skipInstruction2.execute()
        assertEquals(initialPC + 3, rom.getProgramCounter()) // +2 from first, +1 from second

        // Third skip: r4=10, r5=15 (not equal, should skip)
        registers.setValue(4, 10)
        registers.setValue(5, 15)
        val skipInstruction3 = SkipNotEqual("4450", registers, rom, ram)
        skipInstruction3.execute()
        assertEquals(initialPC + 5, rom.getProgramCounter()) // +3 from previous, +2 from this
    }

    @Test
    fun `test boundary value comparisons`() {
        val boundaryTests = listOf(
            Triple(0, 1, true),      // Minimum vs minimum+1
            Triple(0, 0, false),     // Minimum vs minimum
            Triple(255, 254, true),  // Near maximum
            Triple(255, 255, false), // Maximum vs maximum
            Triple(1, 0, true),      // Small difference
            Triple(100, 200, true)   // Large difference
        )

        boundaryTests.forEach { (value1, value2, shouldSkip) ->
            val initialPC = rom.getProgramCounter()

            registers.setValue(0, value1)
            registers.setValue(1, value2)

            val skipInstruction = SkipNotEqual("4010", registers, rom, ram)
            skipInstruction.execute()

            val expectedPC = if (shouldSkip) initialPC + 2 else initialPC + 1
            assertEquals(expectedPC, rom.getProgramCounter(),
                "Failed for values $value1 and $value2")

            // Reset for next test
            setUp()
        }
    }
}