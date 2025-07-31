package org.example

class ConvertByteToAscii(instruction: String, registers: Registers, rom: ROM, ram: RAM) : Instruction(instruction, registers, rom, ram) {

    private var rX: Int = 0
    private var rY: Int = 0

    override fun organizeBytes() {
        val hex = instruction.toInt(16)
        rX = (hex shr 8) and 0xF   // Second hex digit (source register)
        rY = (hex shr 4) and 0xF   // Third hex digit (destination register)
        // Fourth hex digit is always 0 for this instruction
    }

    override fun performOp() {
        val digit = registers.getValue(rX)

        // Check if the value is a valid hex digit (0-15)
        if (digit > 15) {
            // Terminate program with error
            println("ERROR: Value in r$rX ($digit) is greater than F (15). Program terminated.")
            throw RuntimeException("Invalid hex digit: $digit > 15")
        }

        // Convert to ASCII
        val asciiValue = when (digit) {
            in 0..9 -> digit + 48   // ASCII '0' is 48, '1' is 49, etc.
            in 10..15 -> digit + 55 // ASCII 'A' is 65, so 10 + 55 = 65, etc.
            else -> throw RuntimeException("Unexpected digit value: $digit")
        }

        registers.setValue(rY, asciiValue)
    }

    override fun updatePC() {
        rom.incrementProgramCounter()
    }
}