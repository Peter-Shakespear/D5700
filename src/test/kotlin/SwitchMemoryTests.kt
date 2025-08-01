package org.example

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*

class SwitchMemoryTests {

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
    fun `test switch memory from 0 to 1`() {
        // Set up: M register initially 0 (RAM mode)
        registers.setM(0)
        assertEquals(0, registers.M)

        // Execute: Switch memory mode (any instruction format works)
        val switchInstruction = SwitchMemory("E000", registers, rom, ram)
        switchInstruction.execute()

        // Verify: M should now be 1 (ROM mode)
        assertEquals(1, registers.M)
    }

    @Test
    fun `test switch memory from 1 to 0`() {
        // Set up: M register initially 1 (ROM mode)
        registers.setM(1)
        assertEquals(1, registers.M)

        // Execute: Switch memory mode
        val switchInstruction = SwitchMemory("E000", registers, rom, ram)
        switchInstruction.execute()

        // Verify: M should now be 0 (RAM mode)
        assertEquals(0, registers.M)
    }

    @Test
    fun `test consecutive switches`() {
        // Start with M = 0
        registers.setM(0)

        val switchInstruction = SwitchMemory("E000", registers, rom, ram)

        // First switch: 0 -> 1
        switchInstruction.execute()
        assertEquals(1, registers.M)

        // Second switch: 1 -> 0
        switchInstruction.execute()
        assertEquals(0, registers.M)

        // Third switch: 0 -> 1
        switchInstruction.execute()
        assertEquals(1, registers.M)

        // Fourth switch: 1 -> 0
        switchInstruction.execute()
        assertEquals(0, registers.M)
    }

    @Test
    fun `test switch doesn't affect other registers`() {
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
        registers.setT(99)
        registers.setM(0)

        // Execute: Switch memory mode
        val switchInstruction = SwitchMemory("E000", registers, rom, ram)
        switchInstruction.execute()

        // Verify: Only M register changed, all others unchanged
        assertEquals(10, registers.getValue(0))
        assertEquals(20, registers.getValue(1))
        assertEquals(30, registers.getValue(2))
        assertEquals(40, registers.getValue(3))
        assertEquals(50, registers.getValue(4))
        assertEquals(60, registers.getValue(5))
        assertEquals(70, registers.getValue(6))
        assertEquals(80, registers.getValue(7))
        assertEquals(100, registers.A)
        assertEquals(99, registers.T)
        assertEquals(1, registers.M) // Only this changed
    }

    @Test
    fun `test program counter increment`() {
        val initialPC = rom.getProgramCounter()

        val switchInstruction = SwitchMemory("E000", registers, rom, ram)
        switchInstruction.execute()

        // Verify PC was incremented
        assertEquals(initialPC + 1, rom.getProgramCounter())
    }

    @Test
    fun `test instruction format doesn't matter`() {
        // Test that different instruction formats all work the same
        val instructions = listOf("E000", "E123", "E456", "E789", "EABC", "EFFF")

        instructions.forEach { instruction ->
            // Reset M to known state
            registers.setM(0)

            val switchInstruction = SwitchMemory(instruction, registers, rom, ram)
            switchInstruction.execute()

            // Should always switch from 0 to 1
            assertEquals(1, registers.M)

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test organizeBytes does nothing`() {
        // Test that organizeBytes method doesn't crash or affect anything
        val switchInstruction = SwitchMemory("E000", registers, rom, ram)

        // This should not throw any exception
        assertDoesNotThrow {
            switchInstruction.organizeBytes()
        }

        // State should be unchanged
        assertEquals(0, registers.M)
    }

    @Test
    fun `test complete instruction execution flow`() {
        val initialPC = rom.getProgramCounter()
        registers.setM(0)

        // Execute: Switch memory mode
        val switchInstruction = SwitchMemory("E000", registers, rom, ram)
        switchInstruction.execute()

        // Verify all aspects:
        assertEquals(1, registers.M)                           // M switched from 0 to 1
        assertEquals(initialPC + 1, rom.getProgramCounter())   // PC incremented
    }

    @Test
    fun `test memory mode toggle pattern`() {
        // Test the toggle pattern over multiple switches
        val expectedValues = listOf(1, 0, 1, 0, 1, 0, 1, 0)

        registers.setM(0) // Start with 0

        expectedValues.forEach { expectedValue ->
            val switchInstruction = SwitchMemory("E000", registers, rom, ram)
            switchInstruction.execute()

            assertEquals(expectedValue, registers.M)
        }
    }

    @Test
    fun `test switch with memory operations context`() {
        // Test switching in context of memory operations
        // Set up some memory addresses
        ram.writeMemory(100, 42)
        rom.setWritable(true)
        rom.writeMemory(100, 84)
        rom.setWritable(false)

        registers.setA(100)
        registers.setM(0) // Start in RAM mode

        // Verify initial mode
        assertEquals(0, registers.M)

        // Switch to ROM mode
        val switchInstruction = SwitchMemory("E000", registers, rom, ram)
        switchInstruction.execute()

        // Verify switched to ROM mode
        assertEquals(1, registers.M)

        // Switch back to RAM mode
        switchInstruction.execute()

        // Verify back to RAM mode
        assertEquals(0, registers.M)
    }

    @Test
    fun `test all possible M register values`() {
        // Since M can only be 0 or 1, test both transitions

        // Test 0 -> 1
        registers.setM(0)
        val switchInstruction1 = SwitchMemory("E000", registers, rom, ram)
        switchInstruction1.execute()
        assertEquals(1, registers.M)

        // Test 1 -> 0
        val switchInstruction2 = SwitchMemory("E000", registers, rom, ram)
        switchInstruction2.execute()
        assertEquals(0, registers.M)
    }

    @Test
    fun `test switch memory instruction parsing`() {
        // Test that various hex formats are accepted
        val validInstructions = listOf(
            "E000", "E001", "E010", "E100", "E111", "E222",
            "E333", "E456", "E789", "EABC", "EDEF", "EFFF"
        )

        validInstructions.forEach { instruction ->
            registers.setM(0)

            val switchInstruction = SwitchMemory(instruction, registers, rom, ram)

            assertDoesNotThrow {
                switchInstruction.execute()
            }

            assertEquals(1, registers.M)

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test multiple switches with PC tracking`() {
        val initialPC = rom.getProgramCounter()
        val numSwitches = 5

        for (i in 1..numSwitches) {
            val switchInstruction = SwitchMemory("E000", registers, rom, ram)
            switchInstruction.execute()

            // Verify PC incremented correctly
            assertEquals(initialPC + i, rom.getProgramCounter())

            // Verify M alternates correctly (starts at 0)
            val expectedM = i % 2
            assertEquals(expectedM, registers.M)
        }
    }
}