package org.example

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*

class ConvertToBaseTenTests {

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
    fun `test basic convert to base ten in RAM`() {
        // Set up: r0 = 123, A = 100, M = 0 (RAM)
        registers.setValue(0, 123)
        registers.setA(100)
        registers.setM(0)

        // Execute: Convert r0 to base ten (instruction 9000)
        val convertInstruction = ConvertToBaseTen("9000", registers, rom, ram)
        convertInstruction.execute()

        // Verify: RAM should contain digits 1, 2, 3 at addresses 100, 101, 102
        assertEquals(1, ram.readMemory(100))  // hundreds
        assertEquals(2, ram.readMemory(101))  // tens
        assertEquals(3, ram.readMemory(102))  // ones
    }

    @Test
    fun `test basic convert to base ten in ROM`() {
        // Set up: r0 = 456, A = 200, M = 1 (ROM)
        registers.setValue(0, 456)
        registers.setA(200)
        registers.setM(1)
        rom.setWritable(true)

        // Execute: Convert r0 to base ten (instruction 9000)
        val convertInstruction = ConvertToBaseTen("9000", registers, rom, ram)
        convertInstruction.execute()

        // Verify: ROM should contain digits 4, 5, 6 at addresses 200, 201, 202
        assertEquals(4, rom.readMemory(200))  // hundreds
        assertEquals(5, rom.readMemory(201))  // tens
        assertEquals(6, rom.readMemory(202))  // ones

        rom.setWritable(false)
    }

    @Test
    fun `test convert single digit number`() {
        // Set up: r0 = 7
        registers.setValue(0, 7)
        registers.setA(50)
        registers.setM(0)

        val convertInstruction = ConvertToBaseTen("9000", registers, rom, ram)
        convertInstruction.execute()

        // Verify: Should be 0, 0, 7
        assertEquals(0, ram.readMemory(50))   // hundreds
        assertEquals(0, ram.readMemory(51))   // tens
        assertEquals(7, ram.readMemory(52))   // ones
    }

    @Test
    fun `test convert two digit number`() {
        // Set up: r0 = 42
        registers.setValue(0, 42)
        registers.setA(150)
        registers.setM(0)

        val convertInstruction = ConvertToBaseTen("9000", registers, rom, ram)
        convertInstruction.execute()

        // Verify: Should be 0, 4, 2
        assertEquals(0, ram.readMemory(150))  // hundreds
        assertEquals(4, ram.readMemory(151))  // tens
        assertEquals(2, ram.readMemory(152))  // ones
    }

    @Test
    fun `test convert three digit number`() {
        // Set up: r0 = 987
        registers.setValue(0, 987)
        registers.setA(300)
        registers.setM(0)

        val convertInstruction = ConvertToBaseTen("9000", registers, rom, ram)
        convertInstruction.execute()

        // Verify: Should be 9, 8, 7
        assertEquals(9, ram.readMemory(300))  // hundreds
        assertEquals(8, ram.readMemory(301))  // tens
        assertEquals(7, ram.readMemory(302))  // ones
    }

    @Test
    fun `test convert zero`() {
        // Set up: r0 = 0
        registers.setValue(0, 0)
        registers.setA(400)
        registers.setM(0)

        val convertInstruction = ConvertToBaseTen("9000", registers, rom, ram)
        convertInstruction.execute()

        // Verify: Should be 0, 0, 0
        assertEquals(0, ram.readMemory(400))  // hundreds
        assertEquals(0, ram.readMemory(401))  // tens
        assertEquals(0, ram.readMemory(402))  // ones
    }

    @Test
    fun `test convert with different registers`() {
        val testCases = listOf(
            Triple("9000", 0, 123),  // Convert r0
            Triple("9100", 1, 456),  // Convert r1
            Triple("9200", 2, 789),  // Convert r2
            Triple("9700", 7, 321)   // Convert r7
        )

        testCases.forEach { (instruction, sourceReg, value) ->
            registers.setValue(sourceReg, value)
            registers.setA(500)
            registers.setM(0)

            val convertInstruction = ConvertToBaseTen(instruction, registers, rom, ram)
            convertInstruction.execute()

            val expectedHundreds = value / 100
            val expectedTens = (value % 100) / 10
            val expectedOnes = value % 10

            assertEquals(expectedHundreds, ram.readMemory(500))
            assertEquals(expectedTens, ram.readMemory(501))
            assertEquals(expectedOnes, ram.readMemory(502))

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test byte organization parsing`() {
        // Test instruction 9XYZ: rX=(X), YZ ignored
        registers.setValue(5, 654)
        registers.setA(600)
        registers.setM(0)

        // Test instruction 9567: should convert r5 (X=5, YZ ignored)
        val convertInstruction = ConvertToBaseTen("9567", registers, rom, ram)
        convertInstruction.execute()

        assertEquals(6, ram.readMemory(600))  // hundreds
        assertEquals(5, ram.readMemory(601))  // tens
        assertEquals(4, ram.readMemory(602))  // ones
    }

    @Test
    fun `test memory mode switching`() {
        // Test same conversion in both RAM and ROM modes
        registers.setValue(0, 357)
        registers.setA(700)

        // First in RAM mode
        registers.setM(0)
        val convertInstruction1 = ConvertToBaseTen("9000", registers, rom, ram)
        convertInstruction1.execute()

        assertEquals(3, ram.readMemory(700))
        assertEquals(5, ram.readMemory(701))
        assertEquals(7, ram.readMemory(702))

        // Then in ROM mode
        registers.setM(1)
        rom.setWritable(true)
        val convertInstruction2 = ConvertToBaseTen("9000", registers, rom, ram)
        convertInstruction2.execute()

        assertEquals(3, rom.readMemory(700))
        assertEquals(5, rom.readMemory(701))
        assertEquals(7, rom.readMemory(702))

        rom.setWritable(false)
    }

    @Test
    fun `test large numbers`() {
        val testCases = listOf(
            100, 200, 300, 400, 500, 600, 700, 800, 900, 999
        )

        testCases.forEach { value ->
            registers.setValue(0, value)
            registers.setA(800)
            registers.setM(0)

            val convertInstruction = ConvertToBaseTen("9000", registers, rom, ram)
            convertInstruction.execute()

            val expectedHundreds = value / 100
            val expectedTens = (value % 100) / 10
            val expectedOnes = value % 10

            assertEquals(expectedHundreds, ram.readMemory(800), "Hundreds digit for $value")
            assertEquals(expectedTens, ram.readMemory(801), "Tens digit for $value")
            assertEquals(expectedOnes, ram.readMemory(802), "Ones digit for $value")

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test conversion doesn't affect registers`() {
        // Set up: Initialize registers
        registers.setValue(0, 123)
        registers.setValue(1, 20)
        registers.setValue(2, 30)
        registers.setValue(3, 40)
        registers.setA(900)
        registers.setM(0)
        registers.setT(99)

        val convertInstruction = ConvertToBaseTen("9000", registers, rom, ram)
        convertInstruction.execute()

        // Verify: All registers unchanged
        assertEquals(123, registers.getValue(0))  // Source unchanged
        assertEquals(20, registers.getValue(1))
        assertEquals(30, registers.getValue(2))
        assertEquals(40, registers.getValue(3))
        assertEquals(900, registers.A)
        assertEquals(0, registers.M)
        assertEquals(99, registers.T)
    }

    @Test
    fun `test program counter increment`() {
        val initialPC = rom.getProgramCounter()

        registers.setValue(0, 123)
        registers.setA(100)
        registers.setM(0)

        val convertInstruction = ConvertToBaseTen("9000", registers, rom, ram)
        convertInstruction.execute()

        // Verify PC was incremented
        assertEquals(initialPC + 1, rom.getProgramCounter())
    }

    @Test
    fun `test complete instruction execution flow`() {
        val initialPC = rom.getProgramCounter()

        // Set up: r3 = 456, A = 1000, M = 0
        registers.setValue(3, 456)
        registers.setA(1000)
        registers.setM(0)

        // Execute: Convert r3 to base ten (instruction 9300)
        val convertInstruction = ConvertToBaseTen("9300", registers, rom, ram)
        convertInstruction.execute()

        // Verify all aspects:
        assertEquals(4, ram.readMemory(1000))                   // Hundreds digit
        assertEquals(5, ram.readMemory(1001))                   // Tens digit
        assertEquals(6, ram.readMemory(1002))                   // Ones digit
        assertEquals(456, registers.getValue(3))                // Source unchanged
        assertEquals(1000, registers.A)                         // A unchanged
        assertEquals(0, registers.M)                            // M unchanged
        assertEquals(initialPC + 1, rom.getProgramCounter())    // PC incremented
    }

    @Test
    fun `test boundary memory addresses`() {
        registers.setValue(0, 123)
        registers.setM(0)

        val boundaryAddresses = listOf(0, 100, 1000, 2000, 4093) // 4093 allows +2 writes

        boundaryAddresses.forEach { address ->
            registers.setA(address)

            val convertInstruction = ConvertToBaseTen("9000", registers, rom, ram)
            convertInstruction.execute()

            assertEquals(1, ram.readMemory(address))
            assertEquals(2, ram.readMemory(address + 1))
            assertEquals(3, ram.readMemory(address + 2))

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test all valid registers`() {
        // Test converting from all valid registers (0-7)
        for (register in 0..7) {
            val testValue = register * 111 + 123 // Unique value for each register
            val clampedValue = testValue % 1000   // Keep within 3 digits

            registers.setValue(register, clampedValue)
            registers.setA(1100)
            registers.setM(0)

            val instruction = String.format("9%X00", register)
            val convertInstruction = ConvertToBaseTen(instruction, registers, rom, ram)
            convertInstruction.execute()

            val expectedHundreds = clampedValue / 100
            val expectedTens = (clampedValue % 100) / 10
            val expectedOnes = clampedValue % 10

            assertEquals(expectedHundreds, ram.readMemory(1100))
            assertEquals(expectedTens, ram.readMemory(1101))
            assertEquals(expectedOnes, ram.readMemory(1102))

            // Reset for next iteration
            setUp()
        }
    }

    @Test
    fun `test instruction parsing edge cases`() {
        // Test with minimum hex values (9000)
        registers.setValue(0, 123)
        registers.setA(1300)
        registers.setM(0)

        val convertInstruction1 = ConvertToBaseTen("9000", registers, rom, ram)
        convertInstruction1.execute()
        assertEquals(1, ram.readMemory(1300))
        assertEquals(2, ram.readMemory(1301))
        assertEquals(3, ram.readMemory(1302))

        // Test with maximum valid hex values for register (9700)
        setUp()
        registers.setValue(7, 987)
        registers.setA(1400)
        registers.setM(0)

        val convertInstruction2 = ConvertToBaseTen("9700", registers, rom, ram)
        convertInstruction2.execute()
        assertEquals(9, ram.readMemory(1400))
        assertEquals(8, ram.readMemory(1401))
        assertEquals(7, ram.readMemory(1402))
    }

    @Test
    fun `test ROM write protection`() {
        // Test behavior when ROM is not writable
        registers.setValue(0, 123)
        registers.setA(1500)
        registers.setM(1) // ROM mode
        rom.setWritable(false) // ROM not writable

        val convertInstruction = ConvertToBaseTen("9000", registers, rom, ram)
        convertInstruction.execute()

        // ROM writes should fail silently, memory should remain 0
        assertEquals(0, rom.readMemory(1500))
        assertEquals(0, rom.readMemory(1501))
        assertEquals(0, rom.readMemory(1502))
    }

    @Test
    fun `test instruction format with ignored bits`() {
        // Test instruction 9XYZ: only X matters for register selection
        registers.setValue(2, 246)
        registers.setA(1600)
        registers.setM(0)

        val testCases = listOf(
            "9212", "9234", "9256", "9278", "929A", "92BC", "92DE", "92FF"
        )

        testCases.forEach { instruction ->
            val convertInstruction = ConvertToBaseTen(instruction, registers, rom, ram)
            convertInstruction.execute()

            // Should always convert r2 (first nibble after 9)
            assertEquals(2, ram.readMemory(1600))
            assertEquals(4, ram.readMemory(1601))
            assertEquals(6, ram.readMemory(1602))

            // Reset for next test
            setUp()
            registers.setValue(2, 246)
            registers.setA(1600)
            registers.setM(0)
        }
    }
}