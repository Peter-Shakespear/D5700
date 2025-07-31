
package org.example

class SetT(instruction: String, registers: Registers, rom: ROM, ram: RAM) : Instruction(instruction, registers, rom, ram) {

    private var value: Int = 0

    override fun organizeBytes() {
        val hex = instruction.toInt(16)
        value = hex and 0xFF
    }

    override fun performOp() {
        registers.setT(value)
    }

    override fun updatePC() {
        rom.incrementProgramCounter()
    }
}