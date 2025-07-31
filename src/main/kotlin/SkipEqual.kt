package org.example

class SkipEqual(instruction: String, registers: Registers, rom: ROM, ram: RAM) : Instruction(instruction, registers, rom, ram) {

    private var rX: Int = 0
    private var rY: Int = 0

    override fun organizeBytes() {
        val hex = instruction.toInt(16)
        rX = (hex shr 8) and 0xF
        rY = (hex shr 4) and 0xF
    }

    override fun performOp() {
        val valueX = registers.getValue(rX)
        val valueY = registers.getValue(rY)

        if (valueX == valueY) {
            rom.incrementProgramCounter()
        }
    }

    override fun updatePC() {
        rom.incrementProgramCounter()
    }
}