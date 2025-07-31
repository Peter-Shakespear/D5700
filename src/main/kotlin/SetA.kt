package org.example

class SetA(instruction: String, registers: Registers, rom: ROM, ram: RAM) : Instruction(instruction, registers, rom, ram) {

    private var value: Int = 0

    override fun organizeBytes() {
        val hex = instruction.toInt(16)
        value = hex and 0xFFF  // Last three hex digits (aaa)
    }

    override fun performOp() {
        registers.setA(value)
    }

    override fun updatePC() {
        rom.incrementProgramCounter()
    }
}