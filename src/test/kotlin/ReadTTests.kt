package org.example

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*

class ReadTTests {

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
    fun `test basic read T into register`() {
        // Set up: T register contains 123
        registers.setT(123)
        assertEquals(123, registers.T)

        // Execute: Read T into r0 (instruction 5000)
        val readTInstruction = ReadT("5000", registers, rom, ram)
        readTInstruction.execute()

        // Verify: r0 should contain 123
        assertEquals(123, registers.getValue(0))
        assertEquals(123, registers.T) // T unchanged
    }

    @Test
    fun `test read T into different registers`() {
        val testCases = listOf(
            Pair("5000", 0),  // Read T into r0
            Pair("5100", 1),  // Read T into r1
            Pair("5200", 2),  // Read T into r2
            Pair("5300", 3),  // Read T into r3
            Pair("5400", 4),  // Read T into r4
            Pair("5500", 5),  // Read T into r5
            Pair("5600", 6),  // Read T into r6
            Pair("5700", 7)   // Read T into r7
        )

        testCases.forEach { (instruction, targetRegister) ->
            // Set up: T contains test value
            registers.setT(42)

            val readTInstruction = ReadT(instruction, registers, rom, ram)
            readTInstruction.execute()

            assertEquals(42, registers.getValue(targetRegister))
            assertEquals(42, registers.T) // T unchanged

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test byte organization parsing`() {
        // Test instruction 5XYZ: rX=(X), YZ ignored
        registers.setT(99)

        // Test instruction 5567: should read into r5 (X=5, YZ ignored)
        val readTInstruction = ReadT("5567", registers, rom, ram)
        readTInstruction.execute()

        assertEquals(99, registers.getValue(5))
        // Other registers should be unaffected
        assertEquals(0, registers.getValue(6))
        assertEquals(0, registers.getValue(7))
    }

    @Test
    fun `test read T with zero value`() {
        // Set up: T register contains 0
        registers.setT(0)

        val readTInstruction = ReadT("5000", registers, rom, ram)
        readTInstruction.execute()

        // Verify: r0 should contain 0
        assertEquals(0, registers.getValue(0))
        assertEquals(0, registers.T)
    }

    @Test
    fun `test read T with maximum value`() {
        // Set up: T register contains maximum 8-bit value
        registers.setT(255)

        val readTInstruction = ReadT("5000", registers, rom, ram)
        readTInstruction.execute()

        // Verify: r0 should contain 255
        assertEquals(255, registers.getValue(0))
        assertEquals(255, registers.T)
    }

    @Test
    fun `test read T doesn't affect other registers`() {
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

        // Execute: Read T into r2
        val readTInstruction = ReadT("5200", registers, rom, ram)
        readTInstruction.execute()

        // Verify: Only r2 changed, all others unchanged
        assertEquals(10, registers.getValue(0))  // Unchanged
        assertEquals(20, registers.getValue(1))  // Unchanged
        assertEquals(99, registers.getValue(2))  // Changed to T value
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

        registers.setT(50)
        val readTInstruction = ReadT("5000", registers, rom, ram)
        readTInstruction.execute()

        // Verify PC was incremented
        assertEquals(initialPC + 1, rom.getProgramCounter())
    }

    @Test
    fun `test read T overwrites existing register value`() {
        // Set up: r3 initially has a value, T has different value
        registers.setValue(3, 77)
        registers.setT(88)
        assertEquals(77, registers.getValue(3))
        assertEquals(88, registers.T)

        // Execute: Read T into r3
        val readTInstruction = ReadT("5300", registers, rom, ram)
        readTInstruction.execute()

        // Verify: r3 should contain T value
        assertEquals(88, registers.getValue(3))
        assertEquals(88, registers.T) // T unchanged
    }

    @Test
    fun `test complete instruction execution flow`() {
        val initialPC = rom.getProgramCounter()

        // Set up: T contains test value
        registers.setT(156)

        // Execute: Read T into r4 (instruction 5400)
        val readTInstruction = ReadT("5400", registers, rom, ram)
        readTInstruction.execute()

        // Verify all aspects:
        assertEquals(156, registers.getValue(4))               // T value copied to r4
        assertEquals(156, registers.T)                         // T unchanged
        assertEquals(initialPC + 1, rom.getProgramCounter())   // PC incremented
    }

    @Test
    fun `test all valid registers`() {
        // Test reading T into all valid registers (0-7)
        for (register in 0..7) {
            val testValue = register * 10 + 5 // Unique value for verification
            registers.setT(testValue)

            val instruction = String.format("5%X00", register)

            val readTInstruction = ReadT(instruction, registers, rom, ram)
            readTInstruction.execute()

            assertEquals(testValue, registers.getValue(register))
            assertEquals(testValue, registers.T) // T should remain unchanged

            // Reset for next iteration
            setUp()
        }
    }

    @Test
    fun `test instruction parsing edge cases`() {
        // Test with minimum hex values (5000)
        registers.setT(15)
        val readTInstruction1 = ReadT("5000", registers, rom, ram)
        readTInstruction1.execute()
        assertEquals(15, registers.getValue(0))

        // Test with maximum valid hex values for register (5700)
        setUp()
        registers.setT(25)
        val readTInstruction2 = ReadT("5700", registers, rom, ram)
        readTInstruction2.execute()
        assertEquals(25, registers.getValue(7))
    }

    @Test
    fun `test consecutive read T operations`() {
        // Test multiple read T operations in sequence
        registers.setT(100)

        val registers_to_test = listOf(0, 1, 2, 3, 4, 5, 6, 7)

        registers_to_test.forEach { reg ->
            val instruction = String.format("5%X00", reg)
            val readTInstruction = ReadT(instruction, registers, rom, ram)
            readTInstruction.execute()

            assertEquals(100, registers.getValue(reg))
        }

        // All registers should now contain the T value
        for (i in 0..7) {
            assertEquals(100, registers.getValue(i))
        }
        assertEquals(100, registers.T) // T should still be unchanged
    }

    @Test
    fun `test read T with timer context`() {
        // Test in context of timer/countdown scenarios
        val timerValues = listOf(60, 30, 15, 10, 5, 1, 0)

        timerValues.forEach { timerValue ->
            registers.setT(timerValue)

            val readTInstruction = ReadT("5000", registers, rom, ram)
            readTInstruction.execute()

            assertEquals(timerValue, registers.getValue(0))
            assertEquals(timerValue, registers.T)

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test boundary values`() {
        val boundaryTests = listOf(
            Triple(0, "minimum timer value", "5000"),
            Triple(255, "maximum 8-bit timer value", "5000"),
            Triple(1, "minimum non-zero timer", "5000"),
            Triple(128, "middle 8-bit value", "5000")
        )

        boundaryTests.forEach { (timerValue, description, instruction) ->
            registers.setT(timerValue)

            val readTInstruction = ReadT(instruction, registers, rom, ram)
            readTInstruction.execute()

            assertEquals(timerValue, registers.getValue(0), description)
            assertEquals(timerValue, registers.T, "T should remain unchanged")

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test instruction format with ignored bits`() {
        // Test instruction 5XYZ: only X matters for register selection
        registers.setT(42)

        val testCases = listOf(
            "5012", "5034", "5056", "5078", "509A", "50BC", "50DE", "50FF"
        )

        testCases.forEach { instruction ->
            val readTInstruction = ReadT(instruction, registers, rom, ram)
            readTInstruction.execute()

            // Should always read into r0 (first nibble after 5)
            assertEquals(42, registers.getValue(0))
            assertEquals(42, registers.T)

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test read T preserves T register state`() {
        // Verify that reading T never modifies T itself
        val initialTValue = 123
        registers.setT(initialTValue)

        // Read T into multiple registers
        val targetRegisters = listOf(0, 1, 2, 3, 4, 5, 6, 7)

        targetRegisters.forEach { reg ->
            val instruction = String.format("5%X00", reg)
            val readTInstruction = ReadT(instruction, registers, rom, ram)
            readTInstruction.execute()

            // T should always remain the same
            assertEquals(initialTValue, registers.T, "T should never change during ReadT operations")
        }
    }

    @Test
    fun `test instruction format validation`() {
        // Test various valid instruction formats
        val validInstructions = listOf(
            "5000", "5100", "5200", "5300", "5400", "5500", "5600", "5700",
            "5012", "5123", "5234", "5345", "5456", "5567", "5678", "5789"
        )

        validInstructions.forEach { instruction ->
            registers.setT(50)

            val readTInstruction = ReadT(instruction, registers, rom, ram)

            assertDoesNotThrow {
                readTInstruction.execute()
            }

            // Extract expected register from instruction
            val expectedRegister = instruction[1].toString().toInt(16)
            assertEquals(50, registers.getValue(expectedRegister))
            assertEquals(50, registers.T)

            // Reset for next test
            setUp()
        }
    }
}