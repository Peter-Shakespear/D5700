package org.example

class SwitchMemory(instruction: String, registers: Registers, rom: ROM, ram: RAM) : Instruction(instruction, registers, rom, ram) {

    override fun organizeBytes() {
        return
    }

    override fun performOp() {
        // Toggle the M register: 0 becomes 1, 1 becomes 0
        registers.setM(if (registers.M == 0) 1 else 0)
    }

    override fun updatePC() {
        rom.incrementProgramCounter()
    }
}