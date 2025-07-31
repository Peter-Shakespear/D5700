package org.example

class ReadKeyboard(instruction: String, registers: Registers, rom: ROM, ram: RAM) : Instruction(instruction, registers, rom, ram) {
    private var rX: Int = 0

    override fun organizeBytes() {
        val hex = instruction.toInt(16)
        rX = (hex shr 8) and 0xF
    }

    override fun performOp() {
        print("Enter hexadecimal value: ")
        val input = readLine()?.trim()?.uppercase() ?: "0"

        val value = try {
            input.toInt(16)  // Parse as hexadecimal
        } catch (e: NumberFormatException) {
            println("Invalid hex input '$input', using 0")
            0
        }

        registers.setValue(rX, value)
    }

    override fun updatePC() {
        rom.incrementProgramCounter()
    }
}