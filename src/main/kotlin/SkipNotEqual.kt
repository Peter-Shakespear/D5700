package org.example

class SkipNotEqual(instruction: String, registers: Registers, rom: ROM, ram: RAM) : Instruction(instruction, registers, rom, ram) {

    private var rX: Int = 0
    private var rY: Int = 0

    override fun organizeBytes() {
        val hex = instruction.toInt(16)
        rX = (hex shr 8) and 0xF   // Second hex digit (first register)
        rY = (hex shr 4) and 0xF   // Third hex digit (second register)
        // Fourth hex digit is always 0 for this instruction
    }

    override fun performOp() {
        val valueX = registers.getValue(rX)
        val valueY = registers.getValue(rY)

        if (valueX != valueY) {
            // Values are NOT equal, skip the next instruction
            rom.incrementProgramCounter()
        }
        // If values are equal, do nothing (just continue to next instruction)
    }

    override fun updatePC() {
        rom.incrementProgramCounter()
    }
}