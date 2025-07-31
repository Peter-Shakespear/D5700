package org.example

class Write(instruction: String, registers: Registers, rom: ROM, ram: RAM) : Instruction(instruction, registers, rom, ram) {

    private var rX: Int = 0

    override fun organizeBytes() {
        val hex = instruction.toInt(16)
        rX = (hex shr 8) and 0xF
    }

    override fun performOp() {
        val address = registers.A
        val value = registers.getValue(rX)

        if (registers.M == 1) {
            rom.writeMemory(address, value)
        } else {
            ram.writeMemory(address, value)
        }
    }

    override fun updatePC() {
        rom.incrementProgramCounter()
    }
}