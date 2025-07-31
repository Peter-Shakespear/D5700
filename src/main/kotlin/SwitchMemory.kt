package org.example

class SwitchMemory(instruction: String, registers: Registers, rom: ROM, ram: RAM) : Instruction(instruction, registers, rom, ram) {

    override fun organizeBytes() {
        return
    }

    override fun performOp() {
        registers.setM(if (registers.M == 0) 1 else 0)
    }

    override fun updatePC() {
        rom.incrementProgramCounter()
    }
}