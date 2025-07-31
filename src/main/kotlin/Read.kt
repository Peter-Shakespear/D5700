package org.example

class Read(instruction: String, registers: Registers, rom: ROM, ram: RAM) : Instruction(instruction, registers, rom, ram) {

    private var rX: Int = 0

    override fun organizeBytes() {
        val hex = instruction.toInt(16)
        rX = (hex shr 8) and 0xF
    }

    override fun performOp() {
        val address = registers.A
        val value = if (registers.M == 1) {
            rom.readMemory(address)
        } else {
            ram.readMemory(address)
        }
        registers.setValue(rX, value)
    }

    override fun updatePC() {
        rom.incrementProgramCounter()
    }
}