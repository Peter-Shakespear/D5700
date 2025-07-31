package org.example

class ReadKeyboard(instruction: String, registers: Registers, rom: ROM, ram: RAM) : Instruction(instruction, registers, rom, ram) {

    private var rX: Int = 0

    override fun organizeBytes() {
        val hex = instruction.toInt(16)
        rX = (hex shr 8) and 0xF  // Second hex digit (register number)
        // Third and fourth hex digits should be 00
    }

    override fun performOp() {
        print("Enter hex digits (0-F, up to 2 digits): ")
        val input = readLine() ?: ""

        // Filter input to only valid hex characters and take first 2
        val validHexChars = input.uppercase()
            .filter { it in "0123456789ABCDEF" }
            .take(2)

        val value = if (validHexChars.isEmpty()) {
            0  // Store 0 if input is empty string
        } else {
            try {
                // Parse as hex number
                validHexChars.toInt(16)
            } catch (e: NumberFormatException) {
                0  // Fallback to 0 if parsing fails
            }
        }

        registers.setValue(rX, value)

        println("Stored value $value (0x${value.toString(16).uppercase()}) in r$rX")
    }

    override fun updatePC() {
        rom.incrementProgramCounter()
    }
}