package org.example

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*

class ReadTests {

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
    fun `test basic read from RAM operation`() {
        // Set up: Write value 42 to RAM address 100
        ram.writeMemory(100, 42)

        // Set address register A to 100 and mode M to 0 (RAM)
        registers.setA(100)
        registers.setM(0)

        // Execute: Read from memory into r0 (instruction D000)
        val readInstruction = Read("D000", registers, rom, ram)
        readInstruction.execute()

        // Verify: r0 should contain 42
        assertEquals(42, registers.getValue(0))
    }

    @Test
    fun `test basic read from ROM operation`() {
        // Set up: ROM will have some data loaded
        rom.writeMemory(50, 123) // Assumes ROM is writable for testing
        rom.setWritable(true)
        rom.writeMemory(50, 123)
        rom.setWritable(false)

        // Set address register A to 50 and mode M to 1 (ROM)
        registers.setA(50)
        registers.setM(1)

        // Execute: Read from ROM into r1 (instruction D100)
        val readInstruction = Read("D100", registers, rom, ram)
        readInstruction.execute()

        // Verify: r1 should contain 123
        assertEquals(123, registers.getValue(1))
    }

    @Test
    fun `test read from program examples`() {
        // From program files: D000 appears to be a read operation
        ram.writeMemory(200, 85) // Store some test data

        registers.setA(200)
        registers.setM(0) // Read from RAM

        val readInstruction = Read("D000", registers, rom, ram)
        readInstruction.execute()

        assertEquals(85, registers.getValue(0))
    }

    @Test
    fun `test read into different registers`() {
        // Test reading into various registers using format DXYZ where X is register
        val testCases = listOf(
            Triple("D000", 0, 10),   // Read into r0
            Triple("D100", 1, 20),   // Read into r1
            Triple("D200", 2, 30),   // Read into r2
            Triple("D700", 7, 80)    // Read into r7
        )

        testCases.forEach { (instruction, register, value) ->
            // Set up memory with test value
            ram.writeMemory(100, value)
            registers.setA(100)
            registers.setM(0) // RAM mode

            val readInstruction = Read(instruction, registers, rom, ram)
            readInstruction.execute()

            assertEquals(value, registers.getValue(register))

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test byte organization parsing`() {
        // Test instruction DXYZ: only X matters (register), YZ are ignored
        ram.writeMemory(500, 99)
        registers.setA(500)
        registers.setM(0)

        // Test instruction D567: should read into r5 (X=5, YZ ignored)
        val readInstruction = Read("D567", registers, rom, ram)
        readInstruction.execute()

        assertEquals(99, registers.getValue(5))
        // Other registers should be unaffected
        assertEquals(0, registers.getValue(6))
        assertEquals(0, registers.getValue(7))
    }

    @Test
    fun `test read from different memory addresses`() {
        // Test reading from various memory addresses
        val testAddresses = listOf(0, 100, 1000, 2048, 4095) // Valid RAM addresses

        testAddresses.forEachIndexed { index, address ->
            val testValue = 50 + index

            ram.writeMemory(address, testValue)
            registers.setA(address)
            registers.setM(0) // RAM mode

            val readInstruction = Read("D000", registers, rom, ram)
            readInstruction.execute()

            assertEquals(testValue, registers.getValue(0))

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test M register controls memory source`() {
        val testValue1 = 111
        val testValue2 = 222
        val address = 300

        // Set up different values in RAM and ROM at same address
        ram.writeMemory(address, testValue1)
        rom.setWritable(true)
        rom.writeMemory(address, testValue2)
        rom.setWritable(false)

        registers.setA(address)

        // Test reading from RAM (M = 0)
        registers.setM(0)
        val readInstruction1 = Read("D000", registers, rom, ram)
        readInstruction1.execute()
        assertEquals(testValue1, registers.getValue(0)) // Should get RAM value

        // Reset register and test reading from ROM (M = 1)
        registers.setValue(0, 0) // Clear r0
        registers.setM(1)
        val readInstruction2 = Read("D000", registers, rom, ram)
        readInstruction2.execute()
        assertEquals(testValue2, registers.getValue(0)) // Should get ROM value
    }

    @Test
    fun `test read from invalid memory address returns zero`() {
        // Test reading from out-of-bounds address
        registers.setA(5000) // Beyond 4096 limit
        registers.setM(0)

        val readInstruction = Read("D000", registers, rom, ram)
        readInstruction.execute()

        assertEquals(0, registers.getValue(0)) // Should return 0 for invalid address
    }

    @Test
    fun `test read doesn't affect other registers`() {
        // Set up: Initialize multiple registers
        registers.setValue(0, 10)
        registers.setValue(1, 20)
        registers.setValue(2, 30)
        registers.setValue(3, 40)

        // Set up memory
        ram.writeMemory(150, 99)
        registers.setA(150)
        registers.setM(0)

        // Execute: Read into r1 only
        val readInstruction = Read("D100", registers, rom, ram)
        readInstruction.execute()

        // Verify: Only r1 changed, others unchanged
        assertEquals(10, registers.getValue(0))  // Unchanged
        assertEquals(99, registers.getValue(1))  // Changed
        assertEquals(30, registers.getValue(2))  // Unchanged
        assertEquals(40, registers.getValue(3))  // Unchanged
    }

    @Test
    fun `test A register provides address`() {
        // Test that A register value is used as memory address
        val testCases = listOf(
            Pair(0, 100),
            Pair(500, 200),
            Pair(1000, 300),
            Pair(2000, 400)
        )

        testCases.forEach { (address, value) ->
            ram.writeMemory(address, value)
            registers.setA(address) // Set address in A register
            registers.setM(0)

            val readInstruction = Read("D000", registers, rom, ram)
            readInstruction.execute()

            assertEquals(value, registers.getValue(0))

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test read overwrites existing register value`() {
        // Set up: r2 initially has a value
        registers.setValue(2, 55)
        assertEquals(55, registers.getValue(2)) // Verify initial value

        // Set up memory with different value
        ram.writeMemory(75, 77)
        registers.setA(75)
        registers.setM(0)

        // Execute: Read into r2
        val readInstruction = Read("D200", registers, rom, ram)
        readInstruction.execute()

        // Verify: r2 should contain new value from memory
        assertEquals(77, registers.getValue(2))
    }

    @Test
    fun `test program counter increment`() {
        val initialPC = rom.getProgramCounter()

        ram.writeMemory(100, 42)
        registers.setA(100)
        registers.setM(0)

        val readInstruction = Read("D000", registers, rom, ram)
        readInstruction.execute()

        // Verify PC was incremented
        assertEquals(initialPC + 1, rom.getProgramCounter())
    }

    @Test
    fun `test complete instruction execution flow`() {
        val initialPC = rom.getProgramCounter()

        // Set up memory with test data
        ram.writeMemory(1234, 88)
        registers.setA(1234)
        registers.setM(0)

        // Execute: Read from RAM into r5 (instruction D500)
        val readInstruction = Read("D500", registers, rom, ram)
        readInstruction.execute()

        // Verify all aspects:
        assertEquals(88, registers.getValue(5))                    // Value read from memory
        assertEquals(1234, registers.A)                           // A register unchanged
        assertEquals(0, registers.M)                              // M register unchanged
        assertEquals(initialPC + 1, rom.getProgramCounter())      // PC incremented
    }

    @Test
    fun `test all valid registers`() {
        // Test reading into all valid registers (0-7)
        for (register in 0..7) {
            val testValue = register * 10 + 5 // Unique value for each register
            val instruction = String.format("D%X00", register)

            ram.writeMemory(100, testValue)
            registers.setA(100)
            registers.setM(0)

            val readInstruction = Read(instruction, registers, rom, ram)
            readInstruction.execute()

            assertEquals(testValue, registers.getValue(register))

            // Reset for next iteration
            setUp()
        }
    }

    @Test
    fun `test instruction parsing edge cases`() {
        // Test with minimum hex values (D000)
        ram.writeMemory(50, 15)
        registers.setA(50)
        registers.setM(0)

        val readInstruction1 = Read("D000", registers, rom, ram)
        readInstruction1.execute()
        assertEquals(15, registers.getValue(0))

        // Test with maximum valid hex values for register (D700)
        setUp()
        ram.writeMemory(60, 25)
        registers.setA(60)
        registers.setM(0)

        val readInstruction2 = Read("D700", registers, rom, ram)
        readInstruction2.execute()
        assertEquals(25, registers.getValue(7))
    }

    @Test
    fun `test boundary memory addresses`() {
        // Test boundary addresses for both RAM and ROM
        val boundaryTests = listOf(
            Triple(0, 111, "minimum address"),
            Triple(4095, 222, "maximum address"),
            Triple(2048, 333, "middle address")
        )

        boundaryTests.forEach { (address, value, description) ->
            ram.writeMemory(address, value)
            registers.setA(address)
            registers.setM(0)

            val readInstruction = Read("D000", registers, rom, ram)
            readInstruction.execute()

            assertEquals(value, registers.getValue(0), description)

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test consecutive read operations`() {
        // Test multiple read operations in sequence
        val addresses = listOf(100, 200, 300)
        val values = listOf(10, 20, 30)

        // Set up memory
        addresses.forEachIndexed { index, address ->
            ram.writeMemory(address, values[index])
        }

        // Execute consecutive reads
        addresses.forEachIndexed { index, address ->
            registers.setA(address)
            registers.setM(0)

            val readInstruction = Read("D000", registers, rom, ram)
            readInstruction.execute()

            assertEquals(values[index], registers.getValue(0))
        }
    }

    @Test
    fun `test read with zero address`() {
        // Test reading from address 0
        ram.writeMemory(0, 123)
        registers.setA(0)
        registers.setM(0)

        val readInstruction = Read("D000", registers, rom, ram)
        readInstruction.execute()

        assertEquals(123, registers.getValue(0))
    }

    @Test
    fun `test read preserves special registers`() {
        // Set up special registers
        registers.setA(500)
        registers.setM(0)
        registers.setT(99)

        ram.writeMemory(500, 77)

        val readInstruction = Read("D000", registers, rom, ram)
        readInstruction.execute()

        // Verify special registers are preserved
        assertEquals(500, registers.A) // A unchanged
        assertEquals(0, registers.M)   // M unchanged
        assertEquals(99, registers.T)  // T unchanged
        assertEquals(77, registers.getValue(0)) // r0 has read value
    }
}