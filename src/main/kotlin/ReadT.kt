package org.example

class ReadT(instruction: String, registers: Registers, rom: ROM, ram: RAM) : Instruction(instruction, registers, rom, ram) {

    private var rX: Int = 0

    override fun organizeBytes() {
        val hex = instruction.toInt(16)
        rX = (hex shr 8) and 0xF  // Second hex digit (register number)
    }

    override fun performOp() {
        val tValue = registers.T  // Get the value from T register
        registers.setValue(rX, tValue)  // Store it in register rX
    }

    override fun updatePC() {
        rom.incrementProgramCounter()
    }
}