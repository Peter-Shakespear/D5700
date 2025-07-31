package org.example

class Read(instruction: String, registers: Registers, rom: ROM, ram: RAM) : Instruction(instruction, registers, rom, ram) {

    private var rX: Int = 0

    override fun organizeBytes() {
        val hex = instruction.toInt(16)
        rX = (hex shr 8) and 0xF  // Second hex digit (register number)
    }

    override fun performOp() {
        val address = registers.A  // Get address from A register
        val value = if (registers.M == 1) {
            // Read from ROM if M = 1
            rom.readMemory(address)
        } else {
            // Read from RAM if M = 0
            ram.readMemory(address)
        }
        registers.setValue(rX, value)
    }

    override fun updatePC() {
        rom.incrementProgramCounter()
    }
}