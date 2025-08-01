package org.example

class Store(instruction: String, registers: Registers, rom: ROM, ram: RAM) : Instruction(instruction, registers, rom, ram) {

    private var rX: Int = 0
    private var value: Int = 0

    override fun organizeBytes() {
        val hex = instruction.toInt(16)
        rX = (hex shr 8) and 0xF
        value = hex and 0xFF
    }

    override fun performOp() {
        registers.setValue(rX, value)
    }

    override fun updatePC() {
        rom.incrementProgramCounter()
    }
}