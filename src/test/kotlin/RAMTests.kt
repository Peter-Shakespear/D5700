package org.example

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*

class RAMTests {

    private lateinit var ram: RAM

    @BeforeEach
    fun setUp() {
        ram = RAM()
    }

    @Test
    fun `test basic write and read operation`() {
        // Write value 42 to address 100
        ram.writeMemory(100, 42)

        // Read value from address 100
        val value = ram.readMemory(100)

        // Verify: Should read back the same value
        assertEquals(42, value)
    }

    @Test
    fun `test initial memory state is zero`() {
        // All memory locations should initially contain 0
        for (address in 0..100) {
            assertEquals(0, ram.readMemory(address))
        }
    }

    @Test
    fun `test write and read at address zero`() {
        // Test boundary case: address 0
        ram.writeMemory(0, 123)
        assertEquals(123, ram.readMemory(0))
    }

    @Test
    fun `test write and read at maximum address`() {
        // Test boundary case: maximum valid address (4095)
        ram.writeMemory(4095, 255)
        assertEquals(255, ram.readMemory(4095))
    }

    @Test
    fun `test memory size is 4096`() {
        // Test that valid addresses are 0-4095
        // Write to each boundary address
        ram.writeMemory(0, 10)
        ram.writeMemory(4095, 20)

        assertEquals(10, ram.readMemory(0))
        assertEquals(20, ram.readMemory(4095))
    }

    @Test
    fun `test write with negative address returns default`() {
        // Write to negative address (should be ignored)
        ram.writeMemory(-1, 100)

        // Reading from negative address should return 0
        assertEquals(0, ram.readMemory(-1))
    }

    @Test
    fun `test write with out of bounds address`() {
        // Write to address beyond memory size (should be ignored)
        ram.writeMemory(4096, 200)
        ram.writeMemory(5000, 300)

        // Memory should remain unchanged
        // Since write was ignored, these addresses should return 0
        assertEquals(0, ram.readMemory(4096))
        assertEquals(0, ram.readMemory(5000))
    }

    @Test
    fun `test read with negative address returns zero`() {
        // Reading from negative address should return 0
        assertEquals(0, ram.readMemory(-1))
        assertEquals(0, ram.readMemory(-100))
        assertEquals(0, ram.readMemory(-1000))
    }

    @Test
    fun `test read with out of bounds address returns zero`() {
        // Reading from address beyond memory size should return 0
        assertEquals(0, ram.readMemory(4096))
        assertEquals(0, ram.readMemory(5000))
        assertEquals(0, ram.readMemory(10000))
    }

    @Test
    fun `test overwrite existing value`() {
        // Write initial value
        ram.writeMemory(500, 111)
        assertEquals(111, ram.readMemory(500))

        // Overwrite with new value
        ram.writeMemory(500, 222)
        assertEquals(222, ram.readMemory(500))

        // Overwrite with zero
        ram.writeMemory(500, 0)
        assertEquals(0, ram.readMemory(500))
    }

    @Test
    fun `test multiple addresses don't interfere`() {
        // Write different values to different addresses
        ram.writeMemory(100, 10)
        ram.writeMemory(200, 20)
        ram.writeMemory(300, 30)
        ram.writeMemory(400, 40)

        // Verify each address maintains its own value
        assertEquals(10, ram.readMemory(100))
        assertEquals(20, ram.readMemory(200))
        assertEquals(30, ram.readMemory(300))
        assertEquals(40, ram.readMemory(400))

        // Verify unwritten addresses remain zero
        assertEquals(0, ram.readMemory(150))
        assertEquals(0, ram.readMemory(250))
        assertEquals(0, ram.readMemory(350))
    }

    @Test
    fun `test write zero value`() {
        // Write zero explicitly (different from default state)
        ram.writeMemory(600, 50)
        assertEquals(50, ram.readMemory(600))

        ram.writeMemory(600, 0)
        assertEquals(0, ram.readMemory(600))
    }

    @Test
    fun `test write negative values`() {
        // Test writing negative values
        ram.writeMemory(700, -10)
        assertEquals(-10, ram.readMemory(700))

        ram.writeMemory(701, -255)
        assertEquals(-255, ram.readMemory(701))
    }

    @Test
    fun `test write large positive values`() {
        // Test writing large positive values
        ram.writeMemory(800, 1000)
        assertEquals(1000, ram.readMemory(800))

        ram.writeMemory(801, 65535)
        assertEquals(65535, ram.readMemory(801))

        ram.writeMemory(802, Integer.MAX_VALUE)
        assertEquals(Integer.MAX_VALUE, ram.readMemory(802))
    }

    @Test
    fun `test sequential memory operations`() {
        // Test writing to sequential addresses
        for (i in 1000..1010) {
            ram.writeMemory(i, i * 2)
        }

        // Verify sequential reads
        for (i in 1000..1010) {
            assertEquals(i * 2, ram.readMemory(i))
        }
    }

    @Test
    fun `test memory isolation between instances`() {
        // Create a second RAM instance
        val ram2 = RAM()

        // Write to first RAM
        ram.writeMemory(900, 123)

        // Second RAM should still be zero at that address
        assertEquals(0, ram2.readMemory(900))

        // Write to second RAM
        ram2.writeMemory(900, 456)

        // First RAM should maintain its value
        assertEquals(123, ram.readMemory(900))
        assertEquals(456, ram2.readMemory(900))
    }

    @Test
    fun `test boundary addresses`() {
        val boundaryTests = listOf(
            Triple(0, "minimum address", 100),
            Triple(1, "minimum+1 address", 101),
            Triple(4094, "maximum-1 address", 200),
            Triple(4095, "maximum address", 201)
        )

        boundaryTests.forEach { (address, description, value) ->
            ram.writeMemory(address, value)
            assertEquals(value, ram.readMemory(address), description)
        }
    }

    @Test
    fun `test invalid boundary addresses`() {
        val invalidTests = listOf(
            Triple(-1, "negative address", 100),
            Triple(4096, "just over maximum", 200),
            Triple(5000, "well over maximum", 300),
            Triple(Integer.MAX_VALUE, "very large address", 400)
        )

        invalidTests.forEach { (address, description, value) ->
            // Write should be ignored
            ram.writeMemory(address, value)

            // Read should return 0
            assertEquals(0, ram.readMemory(address), description)
        }
    }

    @Test
    fun `test memory persistence within instance`() {
        // Write multiple values
        val testData = mapOf(
            10 to 100,
            20 to 200,
            30 to 300,
            1000 to 1111,
            2000 to 2222,
            3000 to 3333
        )

        // Write all values
        testData.forEach { (address, value) ->
            ram.writeMemory(address, value)
        }

        // Perform some other operations (shouldn't affect memory)
        ram.readMemory(50)
        ram.readMemory(100)
        ram.writeMemory(50, 999)

        // Verify all original values persist
        testData.forEach { (address, value) ->
            assertEquals(value, ram.readMemory(address))
        }
    }

    @Test
    fun `test zero value handling`() {
        // Explicitly test zero value operations
        ram.writeMemory(1500, 0)
        assertEquals(0, ram.readMemory(1500))

        // Write non-zero, then zero
        ram.writeMemory(1501, 42)
        assertEquals(42, ram.readMemory(1501))

        ram.writeMemory(1501, 0)
        assertEquals(0, ram.readMemory(1501))
    }

    @Test
    fun `test large scale memory operations`() {
        // Test writing to many addresses
        val addressValuePairs = mutableListOf<Pair<Int, Int>>()

        // Generate test data
        for (i in 0 until 1000 step 10) {
            addressValuePairs.add(Pair(i, i + 1000))
        }

        // Write all values
        addressValuePairs.forEach { (address, value) ->
            ram.writeMemory(address, value)
        }

        // Verify all values
        addressValuePairs.forEach { (address, value) ->
            assertEquals(value, ram.readMemory(address))
        }
    }

    @Test
    fun `test memory with byte values`() {
        // Test typical byte values (0-255)
        for (value in 0..255) {
            val address = value * 10 // Spread addresses apart
            if (address < 4096) { // Stay within bounds
                ram.writeMemory(address, value)
                assertEquals(value, ram.readMemory(address))
            }
        }
    }

    @Test
    fun `test memory address calculation correctness`() {
        // Test that address bounds checking works correctly
        val testCases = listOf(
            Triple(0, true, "address 0 should be valid"),
            Triple(4095, true, "address 4095 should be valid"),
            Triple(4096, false, "address 4096 should be invalid"),
            Triple(-1, false, "negative address should be invalid")
        )

        testCases.forEach { (address, shouldWork, description) ->
            ram.writeMemory(address, 123)

            if (shouldWork) {
                assertEquals(123, ram.readMemory(address), description)
            } else {
                assertEquals(0, ram.readMemory(address), description)
            }
        }
    }

    @Test
    fun `test memory content preservation across reads`() {
        // Write a value
        ram.writeMemory(2000, 555)

        // Read multiple times
        val firstRead = ram.readMemory(2000)
        val secondRead = ram.readMemory(2000)
        val thirdRead = ram.readMemory(2000)

        // All reads should return the same value
        assertEquals(555, firstRead)
        assertEquals(555, secondRead)
        assertEquals(555, thirdRead)
    }

    @Test
    fun `test random access pattern`() {
        // Test non-sequential access pattern
        val addresses = listOf(3000, 1500, 500, 3500, 100, 4000, 2500)
        val values = listOf(30, 15, 5, 35, 1, 40, 25)

        // Write in one order
        addresses.zip(values).forEach { (address, value) ->
            ram.writeMemory(address, value)
        }

        // Read in different order
        val shuffledPairs = addresses.zip(values).shuffled()
        shuffledPairs.forEach { (address, expectedValue) ->
            assertEquals(expectedValue, ram.readMemory(address))
        }
    }

    @Test
    fun `test memory after multiple overwrites`() {
        val address = 1234
        val values = listOf(1, 100, 50, 200, 0, 999, 42)

        // Write each value in sequence
        values.forEach { value ->
            ram.writeMemory(address, value)
            assertEquals(value, ram.readMemory(address))
        }

        // Final value should be the last one written
        assertEquals(42, ram.readMemory(address))
    }

    @Test
    fun `test edge case integer values`() {
        val address = 1000
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

        edgeValues.forEach { value ->
            ram.writeMemory(address, value)
            assertEquals(value, ram.readMemory(address), "Failed for value: $value")
        }
    }

    @Test
    fun `test memory operations don't affect unrelated addresses`() {
        // Set up initial state
        ram.writeMemory(100, 10)
        ram.writeMemory(200, 20)
        ram.writeMemory(300, 30)

        // Perform operations on unrelated addresses
        ram.writeMemory(150, 999)
        ram.readMemory(250)
        ram.writeMemory(350, -1)

        // Original addresses should be unchanged
        assertEquals(10, ram.readMemory(100))
        assertEquals(20, ram.readMemory(200))
        assertEquals(30, ram.readMemory(300))

        // New addresses should have expected values
        assertEquals(999, ram.readMemory(150))
        assertEquals(0, ram.readMemory(250)) // Never written to
        assertEquals(-1, ram.readMemory(350))
    }

    @Test
    fun `test memory consistency check`() {
        // Fill memory with a pattern
        for (i in 0 until 4096 step 100) {
            ram.writeMemory(i, i / 10)
        }

        // Verify the pattern
        for (i in 0 until 4096 step 100) {
            assertEquals(i / 10, ram.readMemory(i))
        }

        // Verify gaps in pattern remain zero
        for (i in 50 until 4096 step 100) {
            assertEquals(0, ram.readMemory(i))
        }
    }
}