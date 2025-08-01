package org.example

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class ROMTests {

    private lateinit var rom: ROM

    @BeforeEach
    fun setUp() {
        rom = ROM()
    }

    @Test
    fun `test initial ROM state`() {
        // ROM should be initialized with all zeros
        for (address in 0..100) {
            assertEquals(0, rom.readMemory(address))
        }

        // Program counter should start at 0
        assertEquals(0, rom.getProgramCounter())

        // ROM should not be writable initially
        assertFalse(rom.isWritable())

        // Memory size should be 4096
        assertEquals(4096, rom.getProgramSize())
    }

    @Test
    fun `test basic read operation`() {
        // Since memory starts as zeros
        assertEquals(0, rom.readMemory(100))
        assertEquals(0, rom.readMemory(0))
        assertEquals(0, rom.readMemory(4095))
    }

    @Test
    fun `test read with invalid addresses returns zero`() {
        // Test negative addresses
        assertEquals(0, rom.readMemory(-1))
        assertEquals(0, rom.readMemory(-100))

        // Test out-of-bounds addresses
        assertEquals(0, rom.readMemory(4096))
        assertEquals(0, rom.readMemory(5000))
        assertEquals(0, rom.readMemory(10000))
    }

    @Test
    fun `test program counter operations`() {
        // Test initial state
        assertEquals(0, rom.getProgramCounter())

        // Test increment
        rom.incrementProgramCounter()
        assertEquals(1, rom.getProgramCounter())

        // Test multiple increments
        rom.incrementProgramCounter()
        rom.incrementProgramCounter()
        assertEquals(3, rom.getProgramCounter())

        // Test set program counter
        rom.setProgramCounter(100)
        assertEquals(100, rom.getProgramCounter())

        rom.setProgramCounter(0)
        assertEquals(0, rom.getProgramCounter())

        rom.setProgramCounter(4095)
        assertEquals(4095, rom.getProgramCounter())
    }

    @Test
    fun `test write protection when not writable`() {
        // ROM should not be writable initially
        assertFalse(rom.isWritable())

        // Attempt to write should fail
        val result = rom.writeMemory(100, 42)
        assertFalse(result)

        // Memory should remain unchanged
        assertEquals(0, rom.readMemory(100))
    }

    @Test
    fun `test write when writable`() {
        // Enable writing
        rom.setWritable(true)
        assertTrue(rom.isWritable())

        // Write should succeed
        val result = rom.writeMemory(100, 42)
        assertTrue(result)

        // Memory should contain written value
        assertEquals(42, rom.readMemory(100))
    }

    @Test
    fun `test write protection toggle`() {
        // Start not writable
        assertFalse(rom.isWritable())
        assertFalse(rom.writeMemory(200, 123))
        assertEquals(0, rom.readMemory(200))

        // Enable writing
        rom.setWritable(true)
        assertTrue(rom.isWritable())
        assertTrue(rom.writeMemory(200, 123))
        assertEquals(123, rom.readMemory(200))

        // Disable writing again
        rom.setWritable(false)
        assertFalse(rom.isWritable())
        assertFalse(rom.writeMemory(200, 456))
        assertEquals(123, rom.readMemory(200)) // Should retain old value
    }

    @Test
    fun `test write with invalid addresses when writable`() {
        rom.setWritable(true)

        // Test negative addresses
        assertFalse(rom.writeMemory(-1, 100))
        assertFalse(rom.writeMemory(-10, 200))

        // Test out-of-bounds addresses
        assertFalse(rom.writeMemory(4096, 300))
        assertFalse(rom.writeMemory(5000, 400))

        // Verify invalid writes don't affect memory
        assertEquals(0, rom.readMemory(-1))
        assertEquals(0, rom.readMemory(4096))
    }

    @Test
    fun `test memory size`() {
        assertEquals(4096, rom.getProgramSize())
    }

    @Test
    fun `test boundary addresses for read operations`() {
        val boundaryTests = listOf(
            Triple(0, "minimum address", true),
            Triple(4095, "maximum address", true),
            Triple(-1, "negative address", false),
            Triple(4096, "just over maximum", false)
        )

        boundaryTests.forEach { (address, description, shouldWork) ->
            val result = rom.readMemory(address)
            if (shouldWork) {
                assertEquals(0, result, "$description should return 0")
            } else {
                assertEquals(0, result, "$description should return 0 (safe fallback)")
            }
        }
    }

    @Test
    fun `test boundary addresses for write operations`() {
        rom.setWritable(true)

        val boundaryTests = listOf(
            Triple(0, "minimum address", true),
            Triple(4095, "maximum address", true),
            Triple(-1, "negative address", false),
            Triple(4096, "just over maximum", false)
        )

        boundaryTests.forEach { (address, description, shouldWork) ->
            val result = rom.writeMemory(address, 42)
            assertEquals(shouldWork, result, "$description write result")

            if (shouldWork) {
                assertEquals(42, rom.readMemory(address), "$description should contain written value")
            } else {
                assertEquals(0, rom.readMemory(address), "$description should return 0")
            }
        }
    }

    @Test
    fun `test multiple write and read operations`() {
        rom.setWritable(true)

        val testData = mapOf(
            100 to 1000,
            200 to 2000,
            300 to 3000,
            1000 to 1111,
            2000 to 2222
        )

        // Write all values
        testData.forEach { (address, value) ->
            assertTrue(rom.writeMemory(address, value))
        }

        // Read and verify all values
        testData.forEach { (address, value) ->
            assertEquals(value, rom.readMemory(address))
        }
    }

    @Test
    fun `test program counter edge cases`() {
        // Test setting to boundary values
        rom.setProgramCounter(0)
        assertEquals(0, rom.getProgramCounter())

        rom.setProgramCounter(4095)
        assertEquals(4095, rom.getProgramCounter())

        // Test negative values (should still work as ROM doesn't validate)
        rom.setProgramCounter(-1)
        assertEquals(-1, rom.getProgramCounter())

        // Test large values
        rom.setProgramCounter(10000)
        assertEquals(10000, rom.getProgramCounter())
    }

    @Test
    fun `test program counter increment sequence`() {
        val initialPC = rom.getProgramCounter()
        assertEquals(0, initialPC)

        // Test sequence of increments
        for (i in 1..10) {
            rom.incrementProgramCounter()
            assertEquals(i, rom.getProgramCounter())
        }
    }

    @Test
    fun `test load program with test file`(@TempDir tempDir: Path) {
        // Create a test program file
        val testFile = tempDir.resolve("test_program.bin").toFile()

        // Create test data: two instructions (4 bytes total)
        // Instruction 1: 0x1234 (high=0x12, low=0x34)
        // Instruction 2: 0x5678 (high=0x56, low=0x78)
        val testData = byteArrayOf(0x12.toByte(), 0x34.toByte(), 0x56.toByte(), 0x78.toByte())
        testFile.writeBytes(testData)

        // Load the program
        rom.loadProgram(testFile.absolutePath)

        // Verify program counter is reset to 0
        assertEquals(0, rom.getProgramCounter())

        // Verify instructions are loaded correctly
        assertEquals(0x1234, rom.readMemory(0))
        assertEquals(0x5678, rom.readMemory(1))

        // Verify unloaded memory remains zero
        assertEquals(0, rom.readMemory(2))
        assertEquals(0, rom.readMemory(3))
    }

    @Test
    fun `test load program with odd number of bytes`(@TempDir tempDir: Path) {
        // Create test file with odd number of bytes
        val testFile = tempDir.resolve("odd_program.bin").toFile()

        // 3 bytes: one complete instruction + one incomplete
        val testData = byteArrayOf(0x12.toByte(), 0x34.toByte(), 0x56.toByte())
        testFile.writeBytes(testData)

        rom.loadProgram(testFile.absolutePath)

        // First instruction should be loaded
        assertEquals(0x1234, rom.readMemory(0))

        // Second location should remain zero (incomplete instruction)
        assertEquals(0, rom.readMemory(1))
    }

    @Test
    fun `test load empty program`(@TempDir tempDir: Path) {
        // Create empty test file
        val testFile = tempDir.resolve("empty_program.bin").toFile()
        testFile.writeBytes(byteArrayOf())

        rom.loadProgram(testFile.absolutePath)

        // Program counter should still be reset to 0
        assertEquals(0, rom.getProgramCounter())

        // Memory should remain all zeros
        assertEquals(0, rom.readMemory(0))
        assertEquals(0, rom.readMemory(1))
    }

    @Test
    fun `test load program resets program counter`() {
        // Set program counter to some value
        rom.setProgramCounter(100)
        assertEquals(100, rom.getProgramCounter())

        // Create and load a test program
        val testFile = File.createTempFile("test", ".bin")
        testFile.deleteOnExit()
        testFile.writeBytes(byteArrayOf(0x12.toByte(), 0x34.toByte()))

        rom.loadProgram(testFile.absolutePath)

        // Program counter should be reset to 0
        assertEquals(0, rom.getProgramCounter())
    }

    @Test
    fun `test byte processing in load program`(@TempDir tempDir: Path) {
        val testFile = tempDir.resolve("byte_test.bin").toFile()

        // Test with various byte values including negative ones
        val testData = byteArrayOf(
            0x00.toByte(), 0xFF.toByte(),  // 0x00FF
            0xFF.toByte(), 0x00.toByte(),  // 0xFF00
            0x80.toByte(), 0x7F.toByte(),  // 0x807F
            0x7F.toByte(), 0x80.toByte()   // 0x7F80
        )
        testFile.writeBytes(testData)

        rom.loadProgram(testFile.absolutePath)

        // Verify correct byte processing
        assertEquals(0x00FF, rom.readMemory(0))
        assertEquals(0xFF00, rom.readMemory(1))
        assertEquals(0x807F, rom.readMemory(2))
        assertEquals(0x7F80, rom.readMemory(3))
    }

    @Test
    fun `test load program overwrites existing data`() {
        // First, manually set some data in ROM
        rom.setWritable(true)
        rom.writeMemory(0, 9999)
        rom.writeMemory(1, 8888)
        assertEquals(9999, rom.readMemory(0))
        assertEquals(8888, rom.readMemory(1))

        // Create and load a new program
        val testFile = File.createTempFile("overwrite", ".bin")
        testFile.deleteOnExit()
        testFile.writeBytes(byteArrayOf(0x11.toByte(), 0x22.toByte(), 0x33.toByte(), 0x44.toByte()))

        rom.loadProgram(testFile.absolutePath)

        // Old data should be overwritten
        assertEquals(0x1122, rom.readMemory(0))
        assertEquals(0x3344, rom.readMemory(1))
    }

    @Test
    fun `test memory isolation between read and write operations`() {
        rom.setWritable(true)

        // Write to various addresses
        val testAddresses = listOf(0, 100, 500, 1000, 2000, 4095)
        testAddresses.forEachIndexed { index, address ->
            val value = (index + 1) * 100
            assertTrue(rom.writeMemory(address, value))
            assertEquals(value, rom.readMemory(address))
        }

        // Verify addresses between written ones remain zero
        assertEquals(0, rom.readMemory(50))
        assertEquals(0, rom.readMemory(250))
        assertEquals(0, rom.readMemory(750))
    }

    @Test
    fun `test ROM state after write protection changes`() {
        // Write some data when writable
        rom.setWritable(true)
        assertTrue(rom.writeMemory(300, 555))
        assertEquals(555, rom.readMemory(300))

        // Disable writing
        rom.setWritable(false)

        // Try to modify - should fail
        assertFalse(rom.writeMemory(300, 777))
        assertEquals(555, rom.readMemory(300)) // Should retain original value

        // Try to write to new address - should fail
        assertFalse(rom.writeMemory(400, 888))
        assertEquals(0, rom.readMemory(400))

        // Enable writing again
        rom.setWritable(true)

        // Should be able to modify existing data
        assertTrue(rom.writeMemory(300, 777))
        assertEquals(777, rom.readMemory(300))

        // Should be able to write to new addresses
        assertTrue(rom.writeMemory(400, 888))
        assertEquals(888, rom.readMemory(400))
    }

    @Test
    fun `test large program loading`(@TempDir tempDir: Path) {
        val testFile = tempDir.resolve("large_program.bin").toFile()

        // Create a program that fills a significant portion of ROM
        val programSize = 1000 // 1000 instructions = 2000 bytes
        val testData = ByteArray(programSize * 2)

        // Fill with predictable pattern
        for (i in 0 until programSize) {
            val instruction = i + 0x1000 // Ensure non-zero values
            testData[i * 2] = ((instruction shr 8) and 0xFF).toByte()
            testData[i * 2 + 1] = (instruction and 0xFF).toByte()
        }

        testFile.writeBytes(testData)
        rom.loadProgram(testFile.absolutePath)

        // Verify random sampling of loaded instructions
        val sampleIndices = listOf(0, 100, 500, 999)
        sampleIndices.forEach { i ->
            val expectedValue = i + 0x1000
            assertEquals(expectedValue, rom.readMemory(i))
        }

        // Verify unloaded portion remains zero
        assertEquals(0, rom.readMemory(1000))
        assertEquals(0, rom.readMemory(2000))
    }

    @Test
    fun `test concurrent read operations don't interfere`() {
        rom.setWritable(true)

        // Set up test data
        rom.writeMemory(100, 1111)
        rom.writeMemory(200, 2222)
        rom.writeMemory(300, 3333)

        // Multiple reads should all return consistent values
        repeat(10) {
            assertEquals(1111, rom.readMemory(100))
            assertEquals(2222, rom.readMemory(200))
            assertEquals(3333, rom.readMemory(300))
        }
    }

    @Test
    fun `test ROM program counter independence`() {
        // Program counter operations shouldn't affect memory
        rom.setWritable(true)
        rom.writeMemory(50, 999)

        val originalValue = rom.readMemory(50)

        // Perform various program counter operations
        rom.incrementProgramCounter()
        rom.setProgramCounter(1000)
        rom.incrementProgramCounter()
        rom.setProgramCounter(0)

        // Memory should be unchanged
        assertEquals(originalValue, rom.readMemory(50))
    }

    @Test
    fun `test write protection state persistence`() {
        // Test that write protection state persists across operations
        rom.setWritable(true)
        assertTrue(rom.isWritable())

        // Perform some operations
        rom.incrementProgramCounter()
        rom.readMemory(100)
        rom.setProgramCounter(50)

        // Write protection state should persist
        assertTrue(rom.isWritable())

        // Disable and test persistence
        rom.setWritable(false)
        assertFalse(rom.isWritable())

        // Perform operations
        rom.incrementProgramCounter()
        rom.readMemory(200)

        // Should still be not writable
        assertFalse(rom.isWritable())
    }

    @Test
    fun `test edge case values for write operations`() {
        rom.setWritable(true)

        val edgeValues = listOf(
            0,
            1,
            -1,
            255,
            256,
            -256,
            32767,
            -32768,
            65535,
            -65535,
            Integer.MAX_VALUE,
            Integer.MIN_VALUE
        )

        edgeValues.forEachIndexed { index, value ->
            val address = index * 100 // Spread addresses apart
            if (address < 4096) {
                assertTrue(rom.writeMemory(address, value))
                assertEquals(value, rom.readMemory(address), "Failed for value: $value")
            }
        }
    }

    @Test
    fun `test program loading preserves write protection state`() {
        // Set write protection to true
        rom.setWritable(true)
        assertTrue(rom.isWritable())

        // Load a program
        val testFile = File.createTempFile("protection_test", ".bin")
        testFile.deleteOnExit()
        testFile.writeBytes(byteArrayOf(0x12.toByte(), 0x34.toByte()))

        rom.loadProgram(testFile.absolutePath)

        // Write protection state should be preserved
        assertTrue(rom.isWritable())

        // Test with protection disabled
        rom.setWritable(false)
        assertFalse(rom.isWritable())

        rom.loadProgram(testFile.absolutePath)

        // Should still be not writable
        assertFalse(rom.isWritable())
    }
}