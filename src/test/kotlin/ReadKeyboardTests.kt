package org.example

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.AfterEach
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class ReadKeyboardTests {

    private lateinit var registers: Registers
    private lateinit var rom: ROM
    private lateinit var ram: RAM
    private val originalSystemIn = System.`in`
    private val originalSystemOut = System.out
    private lateinit var outputStream: ByteArrayOutputStream

    @BeforeEach
    fun setUp() {
        registers = Registers()
        rom = ROM()
        ram = RAM()
        outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))
    }

    @AfterEach
    fun tearDown() {
        System.setIn(originalSystemIn)
        System.setOut(originalSystemOut)
    }

    private fun setInput(input: String) {
        System.setIn(ByteArrayInputStream(input.toByteArray()))
    }

    @Test
    fun `test basic keyboard read operation`() {
        // Set up: Simulate user input "A" (hex)
        setInput("A\n")

        // Execute: Read keyboard input into r0 (instruction 8000)
        val readKeyboardInstruction = ReadKeyboard("8000", registers, rom, ram)
        readKeyboardInstruction.execute()

        // Verify: r0 should contain 10 (0xA = 10 in decimal)
        assertEquals(10, registers.getValue(0))

        // Verify prompt was displayed
        assertTrue(outputStream.toString().contains("Enter hexadecimal value: "))
    }

    @Test
    fun `test read into different registers`() {
        val testCases = listOf(
            Triple("8000", 0, "F"),   // Read into r0, input "F"
            Triple("8100", 1, "1A"),  // Read into r1, input "1A"
            Triple("8200", 2, "FF"),  // Read into r2, input "FF"
            Triple("8700", 7, "C")    // Read into r7, input "C"
        )

        testCases.forEach { (instruction, register, input) ->
            setUp() // Reset for each test
            setInput("$input\n")

            val readKeyboardInstruction = ReadKeyboard(instruction, registers, rom, ram)
            readKeyboardInstruction.execute()

            val expectedValue = input.toInt(16)
            assertEquals(expectedValue, registers.getValue(register))
        }
    }

    @Test
    fun `test byte organization parsing`() {
        // Test instruction 8XYZ: only X matters (register), YZ are ignored
        setInput("7B\n")

        // Test instruction 8567: should read into r5 (X=5, YZ ignored)
        val readKeyboardInstruction = ReadKeyboard("8567", registers, rom, ram)
        readKeyboardInstruction.execute()

        assertEquals(123, registers.getValue(5)) // 0x7B = 123
        // Other registers should be unaffected
        assertEquals(0, registers.getValue(6))
        assertEquals(0, registers.getValue(7))
    }

    @Test
    fun `test valid hexadecimal inputs`() {
        val hexInputs = listOf(
            Pair("0", 0),
            Pair("1", 1),
            Pair("9", 9),
            Pair("A", 10),
            Pair("F", 15),
            Pair("10", 16),
            Pair("FF", 255),
            Pair("100", 256),
            Pair("1A3", 419)
        )

        hexInputs.forEach { (input, expectedValue) ->
            setUp() // Reset for each test
            setInput("$input\n")

            val readKeyboardInstruction = ReadKeyboard("8000", registers, rom, ram)
            readKeyboardInstruction.execute()

            assertEquals(expectedValue, registers.getValue(0))
        }
    }

    @Test
    fun `test lowercase hexadecimal input converted to uppercase`() {
        val lowercaseInputs = listOf(
            Pair("a", 10),
            Pair("f", 15),
            Pair("abc", 2748), // 0xABC = 2748
            Pair("def", 3567)  // 0xDEF = 3567
        )

        lowercaseInputs.forEach { (input, expectedValue) ->
            setUp() // Reset for each test
            setInput("$input\n")

            val readKeyboardInstruction = ReadKeyboard("8000", registers, rom, ram)
            readKeyboardInstruction.execute()

            assertEquals(expectedValue, registers.getValue(0))
        }
    }

    @Test
    fun `test invalid hexadecimal input defaults to zero`() {
        val invalidInputs = listOf(
            "G",     // Invalid hex character
            "XYZ",   // Invalid hex string
            "12G3",  // Mix of valid and invalid
            "hello", // Text
            "!@#",   // Special characters
            ""       // Empty string (should be handled by null/trim logic)
        )

        invalidInputs.forEach { input ->
            setUp() // Reset for each test
            setInput("$input\n")

            val readKeyboardInstruction = ReadKeyboard("8000", registers, rom, ram)
            readKeyboardInstruction.execute()

            assertEquals(0, registers.getValue(0))

            // Verify error message was printed
            val output = outputStream.toString()
            assertTrue(output.contains("Invalid hex input '$input', using 0") ||
                    output.contains("Invalid hex input '', using 0")) // Empty case
        }
    }

    @Test
    fun `test null input defaults to zero`() {
        // Simulate null input by closing the input stream
        System.setIn(ByteArrayInputStream(byteArrayOf()))

        val readKeyboardInstruction = ReadKeyboard("8000", registers, rom, ram)
        readKeyboardInstruction.execute()

        assertEquals(0, registers.getValue(0))
    }

    @Test
    fun `test whitespace handling`() {
        val whitespaceInputs = listOf(
            "  A  ",    // Spaces around input
            "\tF\t",    // Tabs around input
            " 1A ",     // Spaces around multi-char input
            "  FF  "    // Multiple spaces
        )

        val expectedValues = listOf(10, 15, 26, 255)

        whitespaceInputs.forEachIndexed { index, input ->
            setUp() // Reset for each test
            setInput("$input\n")

            val readKeyboardInstruction = ReadKeyboard("8000", registers, rom, ram)
            readKeyboardInstruction.execute()

            assertEquals(expectedValues[index], registers.getValue(0))
        }
    }

    @Test
    fun `test read doesn't affect other registers`() {
        // Set up: Initialize multiple registers
        registers.setValue(0, 10)
        registers.setValue(1, 20)
        registers.setValue(2, 30)
        registers.setValue(3, 40)

        setInput("AB\n")

        // Execute: Read into r1 only
        val readKeyboardInstruction = ReadKeyboard("8100", registers, rom, ram)
        readKeyboardInstruction.execute()

        // Verify: Only r1 changed, others unchanged
        assertEquals(10, registers.getValue(0))   // Unchanged
        assertEquals(171, registers.getValue(1))  // Changed (0xAB = 171)
        assertEquals(30, registers.getValue(2))   // Unchanged
        assertEquals(40, registers.getValue(3))   // Unchanged
    }

    @Test
    fun `test read overwrites existing register value`() {
        // Set up: r2 initially has a value
        registers.setValue(2, 55)
        assertEquals(55, registers.getValue(2)) // Verify initial value

        setInput("7F\n")

        // Execute: Read into r2
        val readKeyboardInstruction = ReadKeyboard("8200", registers, rom, ram)
        readKeyboardInstruction.execute()

        // Verify: r2 should contain new value from input
        assertEquals(127, registers.getValue(2)) // 0x7F = 127
    }

    @Test
    fun `test program counter increment`() {
        val initialPC = rom.getProgramCounter()

        setInput("42\n")

        val readKeyboardInstruction = ReadKeyboard("8000", registers, rom, ram)
        readKeyboardInstruction.execute()

        // Verify PC was incremented
        assertEquals(initialPC + 1, rom.getProgramCounter())
    }

    @Test
    fun `test complete instruction execution flow`() {
        val initialPC = rom.getProgramCounter()

        setInput("3E\n")

        // Execute: Read keyboard input into r5 (instruction 8500)
        val readKeyboardInstruction = ReadKeyboard("8500", registers, rom, ram)
        readKeyboardInstruction.execute()

        // Verify all aspects:
        assertEquals(62, registers.getValue(5))                    // Value from input (0x3E = 62)
        assertEquals(initialPC + 1, rom.getProgramCounter())       // PC incremented
        assertTrue(outputStream.toString().contains("Enter hexadecimal value: ")) // Prompt displayed
    }

    @Test
    fun `test all valid registers`() {
        // Test reading into all valid registers (0-7)
        for (register in 0..7) {
            setUp() // Reset for each iteration
            val inputValue = register + 10 // Unique hex value for each register
            val hexInput = inputValue.toString(16).uppercase()
            setInput("$hexInput\n")

            val instruction = String.format("8%X00", register)
            val readKeyboardInstruction = ReadKeyboard(instruction, registers, rom, ram)
            readKeyboardInstruction.execute()

            assertEquals(inputValue, registers.getValue(register))
        }
    }

    @Test
    fun `test instruction parsing edge cases`() {
        // Test with minimum hex values (8000)
        setInput("5\n")
        val readKeyboardInstruction1 = ReadKeyboard("8000", registers, rom, ram)
        readKeyboardInstruction1.execute()
        assertEquals(5, registers.getValue(0))

        // Test with maximum valid hex values for register (8700)
        setUp()
        setInput("B\n")
        val readKeyboardInstruction2 = ReadKeyboard("8700", registers, rom, ram)
        readKeyboardInstruction2.execute()
        assertEquals(11, registers.getValue(7))
    }

    @Test
    fun `test boundary hex values`() {
        val boundaryTests = listOf(
            Triple("0", 0, "minimum hex value"),
            Triple("F", 15, "maximum single hex digit"),
            Triple("FF", 255, "maximum byte value"),
            Triple("100", 256, "first three-digit hex"),
            Triple("FFF", 4095, "maximum three-digit hex")
        )

        boundaryTests.forEach { (input, expectedValue, description) ->
            setUp() // Reset for each test
            setInput("$input\n")

            val readKeyboardInstruction = ReadKeyboard("8000", registers, rom, ram)
            readKeyboardInstruction.execute()

            assertEquals(expectedValue, registers.getValue(0), description)
        }
    }

    @Test
    fun `test consecutive read operations`() {
        val inputs = listOf("A", "1F", "C8")
        val expectedValues = listOf(10, 31, 200)

        inputs.forEachIndexed { index, input ->
            setInput("$input\n")

            val readKeyboardInstruction = ReadKeyboard("8000", registers, rom, ram)
            readKeyboardInstruction.execute()

            assertEquals(expectedValues[index], registers.getValue(0))
        }
    }

    @Test
    fun `test special characters in invalid input`() {
        val specialInputs = listOf(
            "12#45",  // Contains special character
            "A-B",    // Contains dash
            "F+F",    // Contains plus
            "10.5"    // Contains decimal point
        )

        specialInputs.forEach { input ->
            setUp() // Reset for each test
            setInput("$input\n")

            val readKeyboardInstruction = ReadKeyboard("8000", registers, rom, ram)
            readKeyboardInstruction.execute()

            assertEquals(0, registers.getValue(0))
            assertTrue(outputStream.toString().contains("Invalid hex input '$input', using 0"))
        }
    }

    @Test
    fun `test mixed case input normalization`() {
        val mixedCaseInputs = listOf(
            Pair("aB", 171),   // 0xAB = 171
            Pair("Cd", 205),   // 0xCD = 205
            Pair("eF", 239),   // 0xEF = 239
            Pair("DeAd", 57005) // 0xDEAD = 57005
        )

        mixedCaseInputs.forEach { (input, expectedValue) ->
            setUp() // Reset for each test
            setInput("$input\n")

            val readKeyboardInstruction = ReadKeyboard("8000", registers, rom, ram)
            readKeyboardInstruction.execute()

            assertEquals(expectedValue, registers.getValue(0))
        }
    }

    @Test
    fun `test large hexadecimal values`() {
        val testCases = listOf(
            Pair("1000", 4096),
            Pair("FFFF", 65535),
            Pair("10000", 65536),
            Pair("ABCDE", 704478)
        )

        testCases.forEachIndexed { index, (input, expectedValue) ->
            setInput("$input\n")

            val readKeyboardInstruction = ReadKeyboard("8000", registers, rom, ram)
            readKeyboardInstruction.execute()

            assertEquals(expectedValue, registers.getValue(0), "Failed on test case $index: input '$input'")

            // Clear the register for next iteration
            registers.setValue(0, 0)
        }
    }

    @Test
    fun `test preserves special registers`() {
        // Set up special registers
        registers.setA(500)
        registers.setM(1)
        registers.setT(99)

        setInput("2A\n")

        val readKeyboardInstruction = ReadKeyboard("8000", registers, rom, ram)
        readKeyboardInstruction.execute()

        // Verify special registers are preserved
        assertEquals(500, registers.A) // A unchanged
        assertEquals(1, registers.M)   // M unchanged
        assertEquals(99, registers.T)  // T unchanged
        assertEquals(42, registers.getValue(0)) // r0 has input value (0x2A = 42)
    }

    @Test
    fun `test instruction format validation`() {
        // Test that the instruction correctly extracts register from 8XYZ format
        setInput("BC\n")

        // For instruction "8567": register = 5, YZ ignored
        val readKeyboardInstruction = ReadKeyboard("8567", registers, rom, ram)
        readKeyboardInstruction.execute()

        assertEquals(188, registers.getValue(5)) // 0xBC = 188

        // Verify other registers are unaffected (should still be 0)
        for (i in 0..7) {
            if (i != 5) {
                assertEquals(0, registers.getValue(i))
            }
        }
    }

    @Test
    fun `test prompt message format`() {
        setInput("1\n")

        val readKeyboardInstruction = ReadKeyboard("8000", registers, rom, ram)
        readKeyboardInstruction.execute()

        val output = outputStream.toString()
        assertEquals("Enter hexadecimal value: ", output)
    }

    @Test
    fun `test error message format for invalid input`() {
        setInput("INVALID\n")

        val readKeyboardInstruction = ReadKeyboard("8000", registers, rom, ram)
        readKeyboardInstruction.execute()

        val output = outputStream.toString()
        assertTrue(output.contains("Enter hexadecimal value: "))
        assertTrue(output.contains("Invalid hex input 'INVALID', using 0"))
    }
}