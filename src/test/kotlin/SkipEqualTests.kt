package org.example

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*

class SkipEqualTests {

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
    fun `test skip when values are equal`() {
        val initialPC = rom.getProgramCounter()

        // Set up: r0 = 15, r1 = 15 (equal)
        registers.setValue(0, 15)
        registers.setValue(1, 15)

        // Execute: Skip if r0 == r1 (instruction 3010)
        val skipInstruction = SkipEqual("3010", registers, rom, ram)
        skipInstruction.execute()

        // Verify: PC should be incremented twice (skip + normal increment)
        assertEquals(initialPC + 2, rom.getProgramCounter())
    }

    @Test
    fun `test no skip when values are not equal`() {
        val initialPC = rom.getProgramCounter()

        // Set up: r0 = 10, r1 = 20 (not equal)
        registers.setValue(0, 10)
        registers.setValue(1, 20)

        // Execute: Skip if r0 == r1 (instruction 3010)
        val skipInstruction = SkipEqual("3010", registers, rom, ram)
        skipInstruction.execute()

        // Verify: PC should be incremented only once (normal increment)
        assertEquals(initialPC + 1, rom.getProgramCounter())
    }

    @Test
    fun `test skip with different register combinations`() {
        val testCases = listOf(
            Triple("3010", 0, 1),  // Compare r0 and r1
            Triple("3230", 2, 3),  // Compare r2 and r3
            Triple("3450", 4, 5),  // Compare r4 and r5
            Triple("3670", 6, 7)   // Compare r6 and r7
        )

        testCases.forEach { (instruction, regX, regY) ->
            val initialPC = rom.getProgramCounter()

            // Set up: same values
            registers.setValue(regX, 42)
            registers.setValue(regY, 42)

            val skipInstruction = SkipEqual(instruction, registers, rom, ram)
            skipInstruction.execute()

            // Should skip (PC + 2)
            assertEquals(initialPC + 2, rom.getProgramCounter())

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test byte organization parsing`() {
        // Test instruction 3XYZ: rX=(X), rY=(Y), Z ignored
        registers.setValue(5, 100)
        registers.setValue(6, 100)

        val initialPC = rom.getProgramCounter()

        // Test instruction 3567: should compare r5 and r6 (X=5, Y=6, Z ignored)
        val skipInstruction = SkipEqual("3567", registers, rom, ram)
        skipInstruction.execute()

        // Values are equal, should skip
        assertEquals(initialPC + 2, rom.getProgramCounter())
    }

    @Test
    fun `test skip with zero values`() {
        val initialPC = rom.getProgramCounter()

        // Set up: r0 = 0, r1 = 0 (equal)
        registers.setValue(0, 0)
        registers.setValue(1, 0)

        val skipInstruction = SkipEqual("3010", registers, rom, ram)
        skipInstruction.execute()

        // Should skip (values are equal)
        assertEquals(initialPC + 2, rom.getProgramCounter())
    }

    @Test
    fun `test no skip with zero and non-zero`() {
        val initialPC = rom.getProgramCounter()

        // Set up: r0 = 0, r1 = 5 (not equal)
        registers.setValue(0, 0)
        registers.setValue(1, 5)

        val skipInstruction = SkipEqual("3010", registers, rom, ram)
        skipInstruction.execute()

        // Should not skip (values are not equal)
        assertEquals(initialPC + 1, rom.getProgramCounter())
    }

    @Test
    fun `test skip doesn't affect registers`() {
        // Set up: Initialize registers
        registers.setValue(0, 10)
        registers.setValue(1, 10)
        registers.setValue(2, 30)
        registers.setValue(3, 40)
        registers.setA(100)
        registers.setM(1)
        registers.setT(99)

        val skipInstruction = SkipEqual("3010", registers, rom, ram)
        skipInstruction.execute()

        // Verify: All registers unchanged
        assertEquals(10, registers.getValue(0))
        assertEquals(10, registers.getValue(1))
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

        // Execute: Compare r2 with itself (instruction 3220)
        val skipInstruction = SkipEqual("3220", registers, rom, ram)
        skipInstruction.execute()

        // Should skip (same register, always equal)
        assertEquals(initialPC + 2, rom.getProgramCounter())
    }

    @Test
    fun `test large number comparison`() {
        val testCases = listOf(
            Triple(255, 255, true),   // Should skip
            Triple(255, 254, false),  // Should not skip
            Triple(1000, 1000, true), // Should skip
            Triple(1000, 999, false)  // Should not skip
        )

        testCases.forEach { (value1, value2, shouldSkip) ->
            val initialPC = rom.getProgramCounter()

            registers.setValue(0, value1)
            registers.setValue(1, value2)

            val skipInstruction = SkipEqual("3010", registers, rom, ram)
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
            Triple(-5, -5, true),     // Equal, should skip
            Triple(-5, -10, false),   // Not equal, should not skip
            Triple(-1, 1, false),     // Not equal, should not skip
            Triple(0, -1, false)      // Not equal, should not skip
        )

        testCases.forEach { (value1, value2, shouldSkip) ->
            val initialPC = rom.getProgramCounter()

            registers.setValue(0, value1)
            registers.setValue(1, value2)

            val skipInstruction = SkipEqual("3010", registers, rom, ram)
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
                val instruction = String.format("3%X%X0", regX, regY)

                // Set same values to ensure skip
                val testValue = 50
                registers.setValue(regX, testValue)
                registers.setValue(regY, testValue)

                val skipInstruction = SkipEqual(instruction, registers, rom, ram)
                skipInstruction.execute()

                // Should always skip since values are equal
                assertEquals(initialPC + 2, rom.getProgramCounter())

                // Reset for next test
                setUp()
            }
        }
    }

    @Test
    fun `test complete instruction execution flow`() {
        val initialPC = rom.getProgramCounter()

        // Set up: r3 = 42, r4 = 42 (equal)
        registers.setValue(3, 42)
        registers.setValue(4, 42)

        // Execute: Skip if r3 == r4 (instruction 3340)
        val skipInstruction = SkipEqual("3340", registers, rom, ram)
        skipInstruction.execute()

        // Verify all aspects:
        assertEquals(42, registers.getValue(3))                // Source unchanged
        assertEquals(42, registers.getValue(4))                // Source unchanged
        assertEquals(initialPC + 2, rom.getProgramCounter())   // PC incremented twice (skip)
    }

    @Test
    fun `test instruction parsing edge cases`() {
        // Test with minimum hex values (3000)
        registers.setValue(0, 5)

        val initialPC = rom.getProgramCounter()
        val skipInstruction1 = SkipEqual("3000", registers, rom, ram)
        skipInstruction1.execute()
        assertEquals(initialPC + 2, rom.getProgramCounter()) // Same register, should skip

        // Test with maximum valid hex values for registers (3770)
        setUp()
        registers.setValue(7, 10)

        val initialPC2 = rom.getProgramCounter()
        val skipInstruction2 = SkipEqual("3770", registers, rom, ram)
        skipInstruction2.execute()
        assertEquals(initialPC2 + 2, rom.getProgramCounter()) // Same register, should skip
    }

    @Test
    fun `test consecutive skip operations`() {
        // Test multiple skip operations in sequence
        val initialPC = rom.getProgramCounter()

        // First skip: r0=5, r1=5 (equal, should skip)
        registers.setValue(0, 5)
        registers.setValue(1, 5)
        val skipInstruction1 = SkipEqual("3010", registers, rom, ram)
        skipInstruction1.execute()
        assertEquals(initialPC + 2, rom.getProgramCounter())

        // Second skip: r2=10, r3=15 (not equal, should not skip)
        registers.setValue(2, 10)
        registers.setValue(3, 15)
        val skipInstruction2 = SkipEqual("3230", registers, rom, ram)
        skipInstruction2.execute()
        assertEquals(initialPC + 3, rom.getProgramCounter()) // +2 from first, +1 from second

        // Third skip: r4=20, r5=20 (equal, should skip)
        registers.setValue(4, 20)
        registers.setValue(5, 20)
        val skipInstruction3 = SkipEqual("3450", registers, rom, ram)
        skipInstruction3.execute()
        assertEquals(initialPC + 5, rom.getProgramCounter()) // +3 from previous, +2 from this
    }

    @Test
    fun `test boundary value comparisons`() {
        val boundaryTests = listOf(
            Triple(0, 0, true),      // Minimum vs minimum
            Triple(0, 1, false),     // Minimum vs minimum+1
            Triple(255, 255, true),  // Maximum vs maximum
            Triple(255, 254, false), // Maximum vs maximum-1
            Triple(100, 100, true),  // Same values
            Triple(100, 200, false)  // Different values
        )

        boundaryTests.forEach { (value1, value2, shouldSkip) ->
            val initialPC = rom.getProgramCounter()

            registers.setValue(0, value1)
            registers.setValue(1, value2)

            val skipInstruction = SkipEqual("3010", registers, rom, ram)
            skipInstruction.execute()

            val expectedPC = if (shouldSkip) initialPC + 2 else initialPC + 1
            assertEquals(expectedPC, rom.getProgramCounter(),
                "Failed for values $value1 and $value2")

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test skip equal vs skip not equal inverse behavior`() {
        val testValues = listOf(
            Pair(10, 10),   // Equal
            Pair(10, 20),   // Not equal
            Pair(0, 0),     // Both zero
            Pair(0, 1),     // Zero and non-zero
            Pair(255, 255), // Large equal
            Pair(255, 254)  // Large not equal
        )

        testValues.forEach { (value1, value2) ->
            val initialPC1 = rom.getProgramCounter()
            val initialPC2 = rom.getProgramCounter()

            // Test SkipEqual
            registers.setValue(0, value1)
            registers.setValue(1, value2)
            val skipEqualInstruction = SkipEqual("3010", registers, rom, ram)
            skipEqualInstruction.execute()
            val equalResult = rom.getProgramCounter()

            // Reset
            setUp()
            val currentPC = rom.getProgramCounter()

            // Test SkipNotEqual
            registers.setValue(0, value1)
            registers.setValue(1, value2)
            val skipNotEqualInstruction = SkipNotEqual("4010", registers, rom, ram)
            skipNotEqualInstruction.execute()
            val notEqualResult = rom.getProgramCounter()

            // Verify inverse behavior: exactly one should skip
            val equalSkipped = (equalResult == initialPC1 + 2)
            val notEqualSkipped = (notEqualResult == currentPC + 2)

            assertTrue(equalSkipped != notEqualSkipped,
                "For values $value1 and $value2: SkipEqual skipped=$equalSkipped, SkipNotEqual skipped=$notEqualSkipped")
        }
    }

    @Test
    fun `test program flow control with skip equal`() {
        // Simulate a simple loop condition check
        val initialPC = rom.getProgramCounter()

        // First iteration: counter = 0, target = 5 (not equal, don't skip)
        registers.setValue(0, 0)  // counter
        registers.setValue(1, 5)  // target
        val skipInstruction1 = SkipEqual("3010", registers, rom, ram)
        skipInstruction1.execute()
        assertEquals(initialPC + 1, rom.getProgramCounter()) // No skip

        // Later iteration: counter = 5, target = 5 (equal, skip)
        registers.setValue(0, 5)  // counter reached target
        val skipInstruction2 = SkipEqual("3010", registers, rom, ram)
        skipInstruction2.execute()
        assertEquals(initialPC + 3, rom.getProgramCounter()) // Skip (+1 from previous, +2 from skip)
    }
}