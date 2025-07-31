package org.example

class ConvertToBaseTen(instruction: String, registers: Registers, rom: ROM, ram: RAM) : Instruction(instruction, registers, rom, ram) {

    private var rX: Int = 0

    override fun organizeBytes() {
        val hex = instruction.toInt(16)
        rX = (hex shr 8) and 0xF
    }

    override fun performOp() {
        val value = registers.getValue(rX)
        val addressA = registers.A

        val hundreds = value / 100
        val tens = (value % 100) / 10
        val ones = value % 10

        if (registers.M == 1) {
            rom.writeMemory(addressA, hundreds)
            rom.writeMemory(addressA + 1, tens)
            rom.writeMemory(addressA + 2, ones)
        } else {
            ram.writeMemory(addressA, hundreds)
            ram.writeMemory(addressA + 1, tens)
            ram.writeMemory(addressA + 2, ones)
        }
    }

    override fun updatePC() {
        rom.incrementProgramCounter()
    }
}