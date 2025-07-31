package org.example

class Write(instruction: String, registers: Registers, rom: ROM, ram: RAM) : Instruction(instruction, registers, rom, ram) {

    private var rX: Int = 0

    override fun organizeBytes() {
        val hex = instruction.toInt(16)
        rX = (hex shr 8) and 0xF  // Second hex digit (register number)
    }

    override fun performOp() {
        val address = registers.A      // Get address from A register
        val value = registers.getValue(rX)  // Get value from register rX

        if (registers.M == 1) {
            // Write to ROM if M = 1 (may fail for most ROM chips)
            rom.writeMemory(address, value)
        } else {
            // Write to RAM if M = 0
            ram.writeMemory(address, value)
        }
    }

    override fun updatePC() {
        rom.incrementProgramCounter()
    }
}