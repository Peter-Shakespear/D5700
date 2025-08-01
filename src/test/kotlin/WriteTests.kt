package org.example

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*

class WriteTests {

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
    fun `test basic write to RAM operation`() {
        // Set up: r0 = 42, address = 100, mode = RAM
        registers.setValue(0, 42)
        registers.setA(100)
        registers.setM(0) // RAM mode

        // Execute: Write r0 to memory (instruction C000)
        val writeInstruction = Write("C000", registers, rom, ram)
        writeInstruction.execute()

        // Verify: RAM at address 100 should contain 42
        assertEquals(42, ram.readMemory(100))
    }

    @Test
    fun `test basic write to ROM operation`() {
        // Set up: r1 = 123, address = 50, mode = ROM
        registers.setValue(1, 123)
        registers.setA(50)
        registers.setM(1) // ROM mode
        rom.setWritable(true) // Allow ROM writes for testing

        // Execute: Write r1 to ROM (instruction C100)
        val writeInstruction = Write("C100", registers, rom, ram)
        writeInstruction.execute()

        // Verify: ROM at address 50 should contain 123
        assertEquals(123, rom.readMemory(50))
    }

    @Test
    fun `test write from different registers`() {
        // Test writing from various registers using format CXYZ where X is register
        val testCases = listOf(
            Triple("C000", 0, 10),   // Write from r0
            Triple("C100", 1, 20),   // Write from r1
            Triple("C200", 2, 30),   // Write from r2
            Triple("C700", 7, 80)    // Write from r7
        )

        testCases.forEach { (instruction, register, value) ->
            // Set up register with test value
            registers.setValue(register, value)
            registers.setA(100)
            registers.setM(0) // RAM mode

            val writeInstruction = Write(instruction, registers, rom, ram)
            writeInstruction.execute()

            assertEquals(value, ram.readMemory(100))

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test byte organization parsing`() {
        // Test instruction CXYZ: only X matters (register), YZ are ignored
        registers.setValue(5, 99)
        registers.setA(500)
        registers.setM(0)

        // Test instruction C567: should write from r5 (X=5, YZ ignored)
        val writeInstruction = Write("C567", registers, rom, ram)
        writeInstruction.execute()

        assertEquals(99, ram.readMemory(500))
    }

    @Test
    fun `test write to different memory addresses`() {
        // Test writing to various memory addresses
        val testAddresses = listOf(0, 100, 1000, 2048, 4095) // Valid RAM addresses

        testAddresses.forEachIndexed { index, address ->
            val testValue = 50 + index

            registers.setValue(0, testValue)
            registers.setA(address)
            registers.setM(0) // RAM mode

            val writeInstruction = Write("C000", registers, rom, ram)
            writeInstruction.execute()

            assertEquals(testValue, ram.readMemory(address))

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test M register controls memory destination`() {
        val testValue = 111
        val address = 300

        registers.setValue(0, testValue)
        registers.setA(address)

        // Test writing to RAM (M = 0)
        registers.setM(0)
        val writeInstruction1 = Write("C000", registers, rom, ram)
        writeInstruction1.execute()
        assertEquals(testValue, ram.readMemory(address)) // Should be in RAM
        assertEquals(0, rom.readMemory(address)) // ROM should be unchanged

        // Test writing to ROM (M = 1)
        rom.setWritable(true) // Enable ROM writes
        registers.setM(1)
        val writeInstruction2 = Write("C000", registers, rom, ram)
        writeInstruction2.execute()
        assertEquals(testValue, rom.readMemory(address)) // Should be in ROM
    }

    @Test
    fun `test write to invalid memory address`() {
        // Test writing to out-of-bounds address
        registers.setValue(0, 42)
        registers.setA(5000) // Beyond 4096 limit
        registers.setM(0)

        val writeInstruction = Write("C000", registers, rom, ram)

        // Should not throw exception, but write should be ignored
        assertDoesNotThrow {
            writeInstruction.execute()
        }

        // Verify no data was written (read returns 0 for invalid address)
        assertEquals(0, ram.readMemory(5000))
    }

    @Test
    fun `test write doesn't affect registers`() {
        // Set up: Initialize registers
        registers.setValue(0, 10)
        registers.setValue(1, 20)
        registers.setValue(2, 30)
        registers.setValue(3, 40)
        registers.setA(150)
        registers.setM(0)

        // Execute: Write from r1
        val writeInstruction = Write("C100", registers, rom, ram)
        writeInstruction.execute()

        // Verify: All registers unchanged
        assertEquals(10, registers.getValue(0))
        assertEquals(20, registers.getValue(1))
        assertEquals(30, registers.getValue(2))
        assertEquals(40, registers.getValue(3))
        assertEquals(150, registers.A)
        assertEquals(0, registers.M)

        // Verify: Memory was written
        assertEquals(20, ram.readMemory(150))
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
            registers.setValue(0, value)
            registers.setA(address) // Set address in A register
            registers.setM(0)

            val writeInstruction = Write("C000", registers, rom, ram)
            writeInstruction.execute()

            assertEquals(value, ram.readMemory(address))

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test write overwrites existing memory value`() {
        // Set up: RAM at address 75 initially has a value
        ram.writeMemory(75, 55)
        assertEquals(55, ram.readMemory(75)) // Verify initial value

        // Set up write operation with different value
        registers.setValue(2, 77)
        registers.setA(75)
        registers.setM(0)

        // Execute: Write to same address
        val writeInstruction = Write("C200", registers, rom, ram)
        writeInstruction.execute()

        // Verify: Memory should contain new value
        assertEquals(77, ram.readMemory(75))
    }

    @Test
    fun `test program counter increment`() {
        val initialPC = rom.getProgramCounter()

        registers.setValue(0, 42)
        registers.setA(100)
        registers.setM(0)

        val writeInstruction = Write("C000", registers, rom, ram)
        writeInstruction.execute()

        // Verify PC was incremented
        assertEquals(initialPC + 1, rom.getProgramCounter())
    }

    @Test
    fun `test complete instruction execution flow`() {
        val initialPC = rom.getProgramCounter()

        // Set up register and address
        registers.setValue(5, 88)
        registers.setA(1234)
        registers.setM(0)

        // Execute: Write from r5 to RAM (instruction C500)
        val writeInstruction = Write("C500", registers, rom, ram)
        writeInstruction.execute()

        // Verify all aspects:
        assertEquals(88, ram.readMemory(1234))                 // Value written to memory
        assertEquals(88, registers.getValue(5))                // Register unchanged
        assertEquals(1234, registers.A)                        // A register unchanged
        assertEquals(0, registers.M)                          // M register unchanged
        assertEquals(initialPC + 1, rom.getProgramCounter())   // PC incremented
    }

    @Test
    fun `test all valid registers`() {
        // Test writing from all valid registers (0-7)
        for (register in 0..7) {
            val testValue = register * 10 + 5 // Unique value for each register
            val instruction = String.format("C%X00", register)

            registers.setValue(register, testValue)
            registers.setA(100)
            registers.setM(0)

            val writeInstruction = Write(instruction, registers, rom, ram)
            writeInstruction.execute()

            assertEquals(testValue, ram.readMemory(100))

            // Reset for next iteration
            setUp()
        }
    }

    @Test
    fun `test instruction parsing edge cases`() {
        // Test with minimum hex values (C000)
        registers.setValue(0, 15)
        registers.setA(50)
        registers.setM(0)

        val writeInstruction1 = Write("C000", registers, rom, ram)
        writeInstruction1.execute()
        assertEquals(15, ram.readMemory(50))

        // Test with maximum valid hex values for register (C700)
        setUp()
        registers.setValue(7, 25)
        registers.setA(60)
        registers.setM(0)

        val writeInstruction2 = Write("C700", registers, rom, ram)
        writeInstruction2.execute()
        assertEquals(25, ram.readMemory(60))
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
            registers.setValue(0, value)
            registers.setA(address)
            registers.setM(0)

            val writeInstruction = Write("C000", registers, rom, ram)
            writeInstruction.execute()

            assertEquals(value, ram.readMemory(address), description)

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test consecutive write operations`() {
        // Test multiple write operations in sequence
        val addresses = listOf(100, 200, 300)
        val values = listOf(10, 20, 30)

        // Execute consecutive writes
        addresses.forEachIndexed { index, address ->
            registers.setValue(0, values[index])
            registers.setA(address)
            registers.setM(0)

            val writeInstruction = Write("C000", registers, rom, ram)
            writeInstruction.execute()

            assertEquals(values[index], ram.readMemory(address))
        }
    }

    @Test
    fun `test write with zero address`() {
        // Test writing to address 0
        registers.setValue(0, 123)
        registers.setA(0)
        registers.setM(0)

        val writeInstruction = Write("C000", registers, rom, ram)
        writeInstruction.execute()

        assertEquals(123, ram.readMemory(0))
    }

    @Test
    fun `test write with zero value`() {
        // Test writing zero value
        registers.setValue(0, 0)
        registers.setA(100)
        registers.setM(0)

        val writeInstruction = Write("C000", registers, rom, ram)
        writeInstruction.execute()

        assertEquals(0, ram.readMemory(100))
    }

    @Test
    fun `test ROM write protection`() {
        // Test writing to ROM when not writable
        registers.setValue(0, 42)
        registers.setA(100)
        registers.setM(1) // ROM mode
        rom.setWritable(false) // ROM is protected

        val writeInstruction = Write("C000", registers, rom, ram)
        writeInstruction.execute()

        // ROM should not be written to (should remain 0)
        assertEquals(0, rom.readMemory(100))
    }

    @Test
    fun `test write boundary values`() {
        // Test minimum value (0)
        registers.setValue(0, 0)
        registers.setA(100)
        registers.setM(0)
        val writeInstruction1 = Write("C000", registers, rom, ram)
        writeInstruction1.execute()
        assertEquals(0, ram.readMemory(100))

        // Test maximum typical value (255)
        registers.setValue(1, 255)
        registers.setA(200)
        registers.setM(0)
        val writeInstruction2 = Write("C100", registers, rom, ram)
        writeInstruction2.execute()
        assertEquals(255, ram.readMemory(200))
    }

    @Test
    fun `test write and read cycle`() {
        // Test writing a value and then reading it back
        val testValue = 42
        val testAddress = 500

        // Write operation
        registers.setValue(3, testValue)
        registers.setA(testAddress)
        registers.setM(0)

        val writeInstruction = Write("C300", registers, rom, ram)
        writeInstruction.execute()

        // Verify write worked
        assertEquals(testValue, ram.readMemory(testAddress))

        // Read operation (reset register first)
        registers.setValue(4, 0)
        val readInstruction = Read("D400", registers, rom, ram)
        readInstruction.execute()

        // Verify read got the written value
        assertEquals(testValue, registers.getValue(4))
    }

    @Test
    fun `test special register preservation during write`() {
        // Set up special registers
        registers.setA(500)
        registers.setM(0)
        registers.setT(99)

        registers.setValue(0, 77)

        val writeInstruction = Write("C000", registers, rom, ram)
        writeInstruction.execute()

        // Verify special registers are preserved
        assertEquals(500, registers.A) // A unchanged
        assertEquals(0, registers.M)   // M unchanged
        assertEquals(99, registers.T)  // T unchanged
        assertEquals(77, registers.getValue(0)) // r0 unchanged
        assertEquals(77, ram.readMemory(500)) // Memory was written
    }
}