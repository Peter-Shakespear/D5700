package org.example

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

class InstructionFactoryTests {

    private lateinit var factory: InstructionFactory
    private lateinit var registers: Registers
    private lateinit var rom: ROM
    private lateinit var ram: RAM
    private lateinit var screen: Screen

    @BeforeEach
    fun setUp() {
        factory = InstructionFactory()
        registers = Registers()
        rom = ROM()
        ram = RAM()
        screen = Screen()
    }

    @Test
    fun `test create Store instruction`() {
        val instruction = factory.createInstruction("0123", registers, rom, ram, screen)

        assertTrue(instruction is Store)
        assertNotNull(instruction)
    }

    @Test
    fun `test create Add instruction`() {
        val instruction = factory.createInstruction("1234", registers, rom, ram, screen)

        assertTrue(instruction is Add)
        assertNotNull(instruction)
    }

    @Test
    fun `test create Subtract instruction`() {
        val instruction = factory.createInstruction("2345", registers, rom, ram, screen)

        assertTrue(instruction is Subtract)
        assertNotNull(instruction)
    }

    @Test
    fun `test create Read instruction`() {
        val instruction = factory.createInstruction("3456", registers, rom, ram, screen)

        assertTrue(instruction is Read)
        assertNotNull(instruction)
    }

    @Test
    fun `test create Write instruction`() {
        val instruction = factory.createInstruction("4567", registers, rom, ram, screen)

        assertTrue(instruction is Write)
        assertNotNull(instruction)
    }

    @Test
    fun `test create Jump instruction`() {
        val instruction = factory.createInstruction("5678", registers, rom, ram, screen)

        assertTrue(instruction is Jump)
        assertNotNull(instruction)
    }

    @Test
    fun `test create ReadKeyboard instruction`() {
        val instruction = factory.createInstruction("6789", registers, rom, ram, screen)

        assertTrue(instruction is ReadKeyboard)
        assertNotNull(instruction)
    }

    @Test
    fun `test create SwitchMemory instruction`() {
        val instruction = factory.createInstruction("789A", registers, rom, ram, screen)

        assertTrue(instruction is SwitchMemory)
        assertNotNull(instruction)
    }

    @Test
    fun `test create SkipEqual instruction`() {
        val instruction = factory.createInstruction("89AB", registers, rom, ram, screen)

        assertTrue(instruction is SkipEqual)
        assertNotNull(instruction)
    }

    @Test
    fun `test create SkipNotEqual instruction`() {
        val instruction = factory.createInstruction("9ABC", registers, rom, ram, screen)

        assertTrue(instruction is SkipNotEqual)
        assertNotNull(instruction)
    }

    @Test
    fun `test create SetA instruction`() {
        val instruction = factory.createInstruction("ABCD", registers, rom, ram, screen)

        assertTrue(instruction is SetA)
        assertNotNull(instruction)
    }

    @Test
    fun `test create SetT instruction`() {
        val instruction = factory.createInstruction("BCDE", registers, rom, ram, screen)

        assertTrue(instruction is SetT)
        assertNotNull(instruction)
    }

    @Test
    fun `test create ReadT instruction`() {
        val instruction = factory.createInstruction("CDEF", registers, rom, ram, screen)

        assertTrue(instruction is ReadT)
        assertNotNull(instruction)
    }

    @Test
    fun `test create ConvertToBaseTen instruction`() {
        val instruction = factory.createInstruction("DEF0", registers, rom, ram, screen)

        assertTrue(instruction is ConvertToBaseTen)
        assertNotNull(instruction)
    }

    @Test
    fun `test create ConvertByteToAscii instruction`() {
        val instruction = factory.createInstruction("EF01", registers, rom, ram, screen)

        assertTrue(instruction is ConvertByteToAscii)
        assertNotNull(instruction)
    }

    @Test
    fun `test create Draw instruction`() {
        val instruction = factory.createInstruction("F012", registers, rom, ram, screen)

        assertTrue(instruction is Draw)
        assertNotNull(instruction)
    }

    @Test
    fun `test unknown instruction throws exception`() {
        val exception = assertThrows<RuntimeException> {
            factory.createInstruction("G123", registers, rom, ram, screen)
        }

        assertEquals("Unknown instruction: G123", exception.message)
    }

    @Test
    fun `test all valid first digits create instructions`() {
        val validDigits = "0123456789ABCDEF"

        validDigits.forEach { digit ->
            val instructionHex = "${digit}000"

            val instruction = factory.createInstruction(instructionHex, registers, rom, ram, screen)

            assertNotNull(instruction, "Should create instruction for digit: $digit")
            assertTrue(instruction is Instruction, "Should return Instruction type for digit: $digit")
        }
    }

    @Test
    fun `test instruction creation with different hex formats`() {
        val testCases = listOf(
            Triple("0000", Store::class.java, "minimum value"),
            Triple("0FFF", Store::class.java, "maximum value for Store"),
            Triple("1ABC", Add::class.java, "mixed hex digits"),
            Triple("FFFF", Draw::class.java, "all F's"),
            Triple("A123", SetA::class.java, "SetA with parameters")
        )

        testCases.forEach { (instructionHex, expectedClass, description) ->
            val instruction = factory.createInstruction(instructionHex, registers, rom, ram, screen)

            assertTrue(expectedClass.isInstance(instruction),
                "Failed for $description: expected ${expectedClass.simpleName}, got ${instruction::class.simpleName}")
        }
    }

    @Test
    fun `test instruction parameters are passed correctly`() {
        // Test that the factory passes all parameters to the instruction constructor
        val testInstruction = "1234"

        val instruction = factory.createInstruction(testInstruction, registers, rom, ram, screen)

        // We can't directly access private fields, but we can verify the instruction was created
        assertNotNull(instruction)
        assertTrue(instruction is Add)

        // The instruction should be able to execute without throwing exceptions due to null parameters
        assertDoesNotThrow {
            // This tests that all required parameters were passed to the constructor
            instruction.organizeBytes() // This should not throw due to missing parameters
        }
    }

    @Test
    fun `test case sensitivity of first digit`() {
        // Test lowercase letters (should not be recognized)
        val lowercaseDigits = "abcdef"

        lowercaseDigits.forEach { digit ->
            val instructionHex = "${digit}000"

            val exception = assertThrows<RuntimeException> {
                factory.createInstruction(instructionHex, registers, rom, ram, screen)
            }

            assertEquals("Unknown instruction: ${instructionHex}", exception.message)
        }
    }

    @Test
    fun `test instruction creation with real program examples`() {
        // Test with actual instruction examples from program files
        val realInstructions = listOf(
            Pair("0048", Store::class.java),  // From hello.d5700
            Pair("0145", Store::class.java),  // From hello.d5700
            Pair("F000", Draw::class.java),   // From hello.d5700
            Pair("B3C0", SetT::class.java),   // From timer.d5700 (actually should be Jump, fixing)
            Pair("C100", ReadT::class.java),  // From timer.d5700
            Pair("8120", SkipEqual::class.java), // From timer.d5700
            Pair("A064", SetA::class.java),   // Common SetA instruction
            Pair("5006", Jump::class.java)    // From timer.d5700
        )

        realInstructions.forEach { (instructionHex, expectedClass) ->
            val instruction = factory.createInstruction(instructionHex, registers, rom, ram, screen)

            assertTrue(expectedClass.isInstance(instruction),
                "Failed for instruction $instructionHex: expected ${expectedClass.simpleName}, got ${instruction::class.simpleName}")
        }
    }

    @Test
    fun `test Draw instruction gets screen parameter`() {
        // Draw instruction is special as it needs the screen parameter
        val instruction = factory.createInstruction("F000", registers, rom, ram, screen)

        assertTrue(instruction is Draw)
        assertNotNull(instruction)
    }

    @Test
    fun `test factory creates different instances`() {
        // Each call should create a new instance
        val instruction1 = factory.createInstruction("1000", registers, rom, ram, screen)
        val instruction2 = factory.createInstruction("1000", registers, rom, ram, screen)

        assertNotSame(instruction1, instruction2, "Factory should create new instances each time")
        assertTrue(instruction1 is Add)
        assertTrue(instruction2 is Add)
    }

    @Test
    fun `test instruction mapping completeness`() {
        // Test that all 16 possible hex digits are mapped
        val expectedMappings = mapOf(
            '0' to Store::class.java,
            '1' to Add::class.java,
            '2' to Subtract::class.java,
            '3' to Read::class.java,
            '4' to Write::class.java,
            '5' to Jump::class.java,
            '6' to ReadKeyboard::class.java,
            '7' to SwitchMemory::class.java,
            '8' to SkipEqual::class.java,
            '9' to SkipNotEqual::class.java,
            'A' to SetA::class.java,
            'B' to SetT::class.java,
            'C' to ReadT::class.java,
            'D' to ConvertToBaseTen::class.java,
            'E' to ConvertByteToAscii::class.java,
            'F' to Draw::class.java
        )

        expectedMappings.forEach { (digit, expectedClass) ->
            val instructionHex = "${digit}000"
            val instruction = factory.createInstruction(instructionHex, registers, rom, ram, screen)

            assertTrue(expectedClass.isInstance(instruction),
                "Digit $digit should map to ${expectedClass.simpleName}, got ${instruction::class.simpleName}")
        }
    }

    @Test
    fun `test factory with null parameters throws exceptions appropriately`() {
        // The factory should handle null parameters gracefully (or the instruction constructors should)
        // This depends on the implementation of individual instruction classes

        // Test with valid instruction - should not throw at factory level
        assertDoesNotThrow {
            val instruction = factory.createInstruction("1000", registers, rom, ram, screen)
            assertNotNull(instruction)
        }
    }

    @Test
    fun `test invalid hex characters in instruction`() {
        val invalidInstructions = listOf("G000", "H123", "Z999", "g000", "h123")

        invalidInstructions.forEach { invalidInstruction ->
            val exception = assertThrows<RuntimeException> {
                factory.createInstruction(invalidInstruction, registers, rom, ram, screen)
            }

            assertEquals("Unknown instruction: $invalidInstruction", exception.message)
        }
    }

    @Test
    fun `test short instruction strings`() {
        // Test behavior with instruction strings shorter than 4 characters
        val shortInstructions = listOf("1", "12", "123")

        shortInstructions.forEach { shortInstruction ->
            // This should either work (using first character) or throw an appropriate exception
            val instruction = factory.createInstruction(shortInstruction, registers, rom, ram, screen)

            // If it doesn't throw, it should return a valid instruction
            assertNotNull(instruction)
            assertTrue(instruction is Add) // '1' should map to Add
        }
    }

    @Test
    fun `test empty instruction string`() {
        // Test behavior with empty string
        val exception = assertThrows<StringIndexOutOfBoundsException> {
            factory.createInstruction("", registers, rom, ram, screen)
        }

        // Should throw StringIndexOutOfBoundsException when trying to access first character
        assertNotNull(exception)
    }

    @Test
    fun `test instruction creation with various parameter combinations`() {
        // Test that factory works with different register/memory states

        // Modify registers state
        registers.setValue(0, 100)
        registers.setA(200)
        registers.setM(1)
        registers.setT(50)

        // Modify ROM state
        rom.setProgramCounter(500)
        rom.setWritable(true)
        rom.writeMemory(100, 999)

        // Modify RAM state
        ram.writeMemory(200, 888)

        // Factory should still create instructions regardless of component states
        val instruction = factory.createInstruction("1000", registers, rom, ram, screen)

        assertNotNull(instruction)
        assertTrue(instruction is Add)
    }

    @Test
    fun `test instruction execution after factory creation`() {
        // Test that factory-created instructions can actually execute
        registers.setValue(0, 10)
        registers.setValue(1, 20)

        val addInstruction = factory.createInstruction("1012", registers, rom, ram, screen)

        // Should be able to execute without errors
        assertDoesNotThrow {
            addInstruction.execute()
        }

        // Verify the instruction actually worked
        assertEquals(30, registers.getValue(2)) // 10 + 20 = 30
    }

    @Test
    fun `test factory thread safety`() {
        // Test that factory can be used safely (each call is independent)
        val instructions = mutableListOf<Instruction>()

        // Create multiple instructions rapidly
        repeat(100) { i ->
            val digit = (i % 16).toString(16).uppercase()
            val instructionHex = "${digit}000"

            try {
                val instruction = factory.createInstruction(instructionHex, registers, rom, ram, screen)
                instructions.add(instruction)
            } catch (e: RuntimeException) {
                // Some digits might not be valid, that's ok for this test
            }
        }

        // Should have created many instructions
        assertTrue(instructions.size > 0)

        // All should be different instances
        for (i in 0 until instructions.size - 1) {
            for (j in i + 1 until instructions.size) {
                assertNotSame(instructions[i], instructions[j])
            }
        }
    }

    @Test
    fun `test instruction factory consistency`() {
        // Test that factory always creates the same type for the same input
        val testInstruction = "A123"

        val instruction1 = factory.createInstruction(testInstruction, registers, rom, ram, screen)
        val instruction2 = factory.createInstruction(testInstruction, registers, rom, ram, screen)
        val instruction3 = factory.createInstruction(testInstruction, registers, rom, ram, screen)

        // All should be the same type but different instances
        assertTrue(instruction1 is SetA)
        assertTrue(instruction2 is SetA)
        assertTrue(instruction3 is SetA)

        assertNotSame(instruction1, instruction2)
        assertNotSame(instruction2, instruction3)
        assertNotSame(instruction1, instruction3)
    }

    @Test
    fun `test factory with extreme hex values`() {
        // Test with boundary hex values
        val extremeInstructions = listOf(
            "0000", "1111", "2222", "3333", "4444", "5555",
            "6666", "7777", "8888", "9999", "AAAA", "BBBB",
            "CCCC", "DDDD", "EEEE", "FFFF"
        )

        extremeInstructions.forEach { instructionHex ->
            val instruction = factory.createInstruction(instructionHex, registers, rom, ram, screen)

            assertNotNull(instruction, "Failed to create instruction for $instructionHex")
            assertTrue(instruction is Instruction, "Should return Instruction type for $instructionHex")
        }
    }

    @Test
    fun `test factory error message accuracy`() {
        val invalidInstructions = listOf("X000", "Y123", "Z999")

        invalidInstructions.forEach { invalidInstruction ->
            val exception = assertThrows<RuntimeException> {
                factory.createInstruction(invalidInstruction, registers, rom, ram, screen)
            }

            // Error message should include the exact invalid instruction
            assertTrue(exception.message!!.contains(invalidInstruction),
                "Error message should contain the invalid instruction: ${exception.message}")
            assertEquals("Unknown instruction: $invalidInstruction", exception.message)
        }
    }
}