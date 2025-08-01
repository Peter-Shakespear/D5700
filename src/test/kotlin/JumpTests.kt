
package org.example

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class JumpTests {

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
    fun `test basic jump operation`() {
        val initialPC = rom.getProgramCounter()

        // Execute: Jump to address 100 (instruction B064)
        val jumpInstruction = Jump("B064", registers, rom, ram)
        jumpInstruction.execute()

        // Verify: PC should be set to 100
        assertEquals(100, rom.getProgramCounter())
        assertNotEquals(initialPC + 1, rom.getProgramCounter()) // PC should not increment normally
    }

    @Test
    fun `test jump to address 0`() {
        // Set PC to some non-zero value first
        rom.setProgramCounter(500)

        // Execute: Jump to address 0 (instruction B000)
        val jumpInstruction = Jump("B000", registers, rom, ram)
        jumpInstruction.execute()

        // Verify: PC should be set to 0
        assertEquals(0, rom.getProgramCounter())
    }

    @Test
    fun `test jump to even addresses`() {
        val evenAddresses = listOf(0, 2, 10, 100, 500, 1000, 2048, 4094)

        evenAddresses.forEach { address ->
            val instruction = String.format("B%03X", address)

            val jumpInstruction = Jump(instruction, registers, rom, ram)
            jumpInstruction.execute()

            assertEquals(address, rom.getProgramCounter())

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test jump with odd address throws exception`() {
        // Test jumping to odd addresses (should throw exception)
        val oddAddresses = listOf(1, 3, 9, 99, 501, 1001, 2047, 4095)

        oddAddresses.forEach { address ->
            val instruction = String.format("B%03X", address)

            val jumpInstruction = Jump(instruction, registers, rom, ram)

            val exception = assertThrows<RuntimeException> {
                jumpInstruction.execute()
            }

            assertTrue(exception.message!!.contains("Invalid jump address: $address is not divisible by 2"))

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test jump with odd address prints error message`() {
        // Capture System.out to verify error message
        val originalOut = System.out
        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        try {
            val jumpInstruction = Jump("B003", registers, rom, ram) // Jump to address 3 (odd)

            assertThrows<RuntimeException> {
                jumpInstruction.execute()
            }

            val output = outputStream.toString()
            assertTrue(output.contains("ERROR: Jump address 3 (0x3) is not divisible by 2. Program terminated."))

        } finally {
            System.setOut(originalOut)
        }
    }

    @Test
    fun `test byte organization parsing`() {
        // Test instruction BXYZ: extracts 12-bit address (XYZ)
        val testCases = listOf(
            Pair("B000", 0),      // 0x000 = 0
            Pair("B064", 100),    // 0x064 = 100
            Pair("B1F4", 500),    // 0x1F4 = 500
            Pair("B3E8", 1000),   // 0x3E8 = 1000
            Pair("B800", 2048),   // 0x800 = 2048
            Pair("BFFE", 4094)    // 0xFFE = 4094
        )

        testCases.forEach { (instruction, expectedAddress) ->
            val jumpInstruction = Jump(instruction, registers, rom, ram)
            jumpInstruction.execute()

            assertEquals(expectedAddress, rom.getProgramCounter())

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test jump doesn't affect registers`() {
        // Set up: Initialize registers
        registers.setValue(0, 10)
        registers.setValue(1, 20)
        registers.setValue(2, 30)
        registers.setValue(3, 40)
        registers.setA(150)
        registers.setM(1)
        registers.setT(99)

        // Execute: Jump to address 200
        val jumpInstruction = Jump("B0C8", registers, rom, ram)
        jumpInstruction.execute()

        // Verify: All registers unchanged
        assertEquals(10, registers.getValue(0))
        assertEquals(20, registers.getValue(1))
        assertEquals(30, registers.getValue(2))
        assertEquals(40, registers.getValue(3))
        assertEquals(150, registers.A)
        assertEquals(1, registers.M)
        assertEquals(99, registers.T)

        // Verify: Only PC changed
        assertEquals(200, rom.getProgramCounter())
    }

    @Test
    fun `test maximum valid jump address`() {
        // Test jumping to maximum valid address (4094, which is 0xFFE)
        val jumpInstruction = Jump("BFFE", registers, rom, ram)
        jumpInstruction.execute()

        assertEquals(4094, rom.getProgramCounter())
    }

    @Test
    fun `test jump address extraction with 12-bit mask`() {
        // Test that only 12 bits are used for address (0xFFF mask)
        // Instruction BFFF should jump to 4095, but 4095 is odd so it should throw
        val jumpInstruction = Jump("BFFF", registers, rom, ram)

        val exception = assertThrows<RuntimeException> {
            jumpInstruction.execute()
        }

        assertTrue(exception.message!!.contains("Invalid jump address: 4095 is not divisible by 2"))
    }

    @Test
    fun `test consecutive jumps`() {
        val addresses = listOf(100, 200, 500, 1000)

        addresses.forEach { address ->
            val instruction = String.format("B%03X", address)

            val jumpInstruction = Jump(instruction, registers, rom, ram)
            jumpInstruction.execute()

            assertEquals(address, rom.getProgramCounter())
        }
    }

    @Test
    fun `test jump backward and forward`() {
        // Start at address 1000
        rom.setProgramCounter(1000)
        assertEquals(1000, rom.getProgramCounter())

        // Jump backward to 500
        val jumpBackward = Jump("B1F4", registers, rom, ram) // 0x1F4 = 500
        jumpBackward.execute()
        assertEquals(500, rom.getProgramCounter())

        // Jump forward to 2000
        val jumpForward = Jump("B7D0", registers, rom, ram) // 0x7D0 = 2000
        jumpForward.execute()
        assertEquals(2000, rom.getProgramCounter())

        // Jump to 0
        val jumpToZero = Jump("B000", registers, rom, ram)
        jumpToZero.execute()
        assertEquals(0, rom.getProgramCounter())
    }

    @Test
    fun `test updatePC does nothing`() {
        val initialPC = rom.getProgramCounter()

        // Execute jump to address 100
        val jumpInstruction = Jump("B064", registers, rom, ram)
        jumpInstruction.execute()

        // PC should be 100 (set by performOp)
        assertEquals(100, rom.getProgramCounter())

        // Call updatePC explicitly - should do nothing
        jumpInstruction.updatePC()

        // PC should still be 100 (updatePC doesn't change it)
        assertEquals(100, rom.getProgramCounter())
    }

    @Test
    fun `test jump instruction format validation`() {
        // Test various valid instruction formats
        val validInstructions = listOf(
            "B000",  // Minimum
            "B002",  // Small even
            "B064",  // 100
            "B3E8",  // 1000
            "B7D0",  // 2000
            "BFFE"   // Maximum even (4094)
        )

        validInstructions.forEach { instruction ->
            val jumpInstruction = Jump(instruction, registers, rom, ram)

            assertDoesNotThrow {
                jumpInstruction.execute()
            }

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test jump to boundary addresses`() {
        // Test boundary addresses
        val boundaryTests = listOf(
            Pair(0, "minimum address"),
            Pair(4094, "maximum even address"),
            Pair(2048, "middle address")
        )

        boundaryTests.forEach { (address, description) ->
            val instruction = String.format("B%03X", address)

            val jumpInstruction = Jump(instruction, registers, rom, ram)
            jumpInstruction.execute()

            assertEquals(address, rom.getProgramCounter(), description)

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test complete instruction execution flow`() {
        val initialPC = rom.getProgramCounter()

        // Execute: Jump to address 1234 (instruction B4D2)
        val jumpInstruction = Jump("B4D2", registers, rom, ram)
        jumpInstruction.execute()

        // Verify all aspects:
        assertEquals(1234, rom.getProgramCounter())  // PC set to jump target
        assertNotEquals(initialPC + 1, rom.getProgramCounter()) // Normal PC increment didn't happen

        // Verify registers are unchanged (all should still be 0)
        for (i in 0..7) {
            assertEquals(0, registers.getValue(i))
        }
        assertEquals(0, registers.A)
        assertEquals(0, registers.M)
        assertEquals(0, registers.T)
    }

    @Test
    fun `test jump with hexadecimal parsing`() {
        val hexTestCases = listOf(
            Triple("B00A", 10, "0x00A = 10"),
            Triple("B014", 20, "0x014 = 20"),
            Triple("B032", 50, "0x032 = 50"),
            Triple("B064", 100, "0x064 = 100"),
            Triple("B12C", 300, "0x12C = 300"),
            Triple("B2BC", 700, "0x2BC = 700")
        )

        hexTestCases.forEach { (instruction, expectedAddress, description) ->
            val jumpInstruction = Jump(instruction, registers, rom, ram)
            jumpInstruction.execute()

            assertEquals(expectedAddress, rom.getProgramCounter(), description)

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test jump address alignment validation`() {
        // Test that the validation specifically checks for divisibility by 2
        val alignmentTests = listOf(
            Pair(0, true),    // 0 % 2 == 0
            Pair(2, true),    // 2 % 2 == 0
            Pair(4, true),    // 4 % 2 == 0
            Pair(1, false),   // 1 % 2 != 0
            Pair(3, false),   // 3 % 2 != 0
            Pair(5, false)    // 5 % 2 != 0
        )

        alignmentTests.forEach { (address, shouldSucceed) ->
            val instruction = String.format("B%03X", address)
            val jumpInstruction = Jump(instruction, registers, rom, ram)

            if (shouldSucceed) {
                assertDoesNotThrow {
                    jumpInstruction.execute()
                }
                assertEquals(address, rom.getProgramCounter())
            } else {
                assertThrows<RuntimeException> {
                    jumpInstruction.execute()
                }
            }

            // Reset for next test
            setUp()
        }
    }

    @Test
    fun `test jump error message format`() {
        // Capture System.out to verify error message format
        val originalOut = System.out
        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        try {
            val jumpInstruction = Jump("B007", registers, rom, ram) // Jump to address 7 (odd)

            assertThrows<RuntimeException> {
                jumpInstruction.execute()
            }

            val output = outputStream.toString()
            // Verify error message contains address in both decimal and hex
            assertTrue(output.contains("ERROR: Jump address 7 (0x7) is not divisible by 2. Program terminated."))

        } finally {
            System.setOut(originalOut)
        }
    }

    @Test
    fun `test jump with large valid addresses`() {
        // Test jumping to larger valid addresses
        val largeAddresses = listOf(1000, 2000, 3000, 4000, 4094)

        largeAddresses.forEach { address ->
            val instruction = String.format("B%03X", address)

            val jumpInstruction = Jump(instruction, registers, rom, ram)
            jumpInstruction.execute()

            assertEquals(address, rom.getProgramCounter())

            // Reset for next test
            setUp()
        }
    }
}